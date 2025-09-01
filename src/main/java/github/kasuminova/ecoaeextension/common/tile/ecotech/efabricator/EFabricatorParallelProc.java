package github.kasuminova.ecoaeextension.common.tile.ecotech.efabricator;

import github.kasuminova.ecoaeextension.common.crafttweaker.util.NovaEngUtils;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class EFabricatorParallelProc extends EFabricatorPart {

    protected final List<Modifier> modifiers = new ArrayList<>();
    protected final List<Modifier> overclockModifiers = new ArrayList<>();

    public EFabricatorParallelProc() {
    }

    public EFabricatorParallelProc(List<Modifier> modifiers, List<Modifier> overclockModifiers) {
        this.modifiers.addAll(modifiers);
        this.overclockModifiers.addAll(overclockModifiers);
    }

    public List<Modifier> getModifiers() {
        return modifiers;
    }

    public List<Modifier> getOverclockModifiers() {
        return overclockModifiers;
    }

    @Override
    public void readCustomNBT(final NBTTagCompound compound) {
        super.readCustomNBT(compound);
        modifiers.clear();
        compound.getTagList("modifiers", Constants.NBT.TAG_COMPOUND)
                .forEach(tag -> modifiers.add(Modifier.readFromNBT((NBTTagCompound) tag)));

        overclockModifiers.clear();
        compound.getTagList("overclockModifiers", Constants.NBT.TAG_COMPOUND)
                .forEach(tag -> overclockModifiers.add(Modifier.readFromNBT((NBTTagCompound) tag)));
    }

    @Override
    public void writeCustomNBT(final NBTTagCompound compound) {
        super.writeCustomNBT(compound);
        NBTTagList modifiersTag = new NBTTagList();
        modifiers.forEach(modifier -> modifiersTag.appendTag(modifier.writeToNBT()));
        compound.setTag("modifiers", modifiersTag);

        NBTTagList overclockModifiersTag = new NBTTagList();
        overclockModifiers.forEach(modifier -> overclockModifiersTag.appendTag(modifier.writeToNBT()));
        compound.setTag("overclockModifiers", overclockModifiersTag);
    }

    public static final class Modifier {

        private final Type type;
        private final double value;
        private final boolean debuff;

        public Modifier(final Type type, final double value, final boolean debuff) {
            this.type = type;
            this.value = value;
            this.debuff = debuff;
        }

        public Type getType() {
            return type;
        }

        public double apply(final double parallelism) {
            return type.apply(value, parallelism);
        }

        public boolean isBuff() {
            return !debuff;
        }

        public boolean isDebuff() {
            return debuff;
        }

        public NBTTagCompound writeToNBT() {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setByte("type", (byte) type.ordinal());
            tag.setDouble("value", value);
            tag.setBoolean("debuff", debuff);
            return tag;
        }

        public static Modifier readFromNBT(NBTTagCompound tag) {
            return new Modifier(Type.values()[tag.getByte("type")], tag.getDouble("value"), tag.getBoolean("debuff"));
        }

        @SideOnly(Side.CLIENT)
        public String getDesc() {
            switch (type) {
                case ADD -> {
                    return isBuff()
                            ? I18n.format("novaeng.efabricator_parallel_proc.modifier.add", value)
                            : I18n.format("novaeng.efabricator_parallel_proc.modifier.sub", Math.abs(value));
                }
                case MULTIPLY -> {
                    return isBuff()
                            ? I18n.format("novaeng.efabricator_parallel_proc.modifier.mul",
                            NovaEngUtils.formatDouble((1D - value) * 100, 1))
                            : I18n.format("novaeng.efabricator_parallel_proc.modifier.mul.debuff",
                            NovaEngUtils.formatDouble(Math.abs(1D - value) * 100, 1));
                }
            }
            return "";
        }
    }

    public enum Type {

        ADD(0),
        MULTIPLY(1);

        final int priority;

        Type(int priority) {
            this.priority = priority;
        }

        public int getPriority() {
            return priority;
        }

        public double apply(final double value, final double parallelism) {
            return switch (this) {
                case ADD -> value + parallelism;
                case MULTIPLY -> value * parallelism;
            };
        }

    }

}
