package github.kasuminova.ecoaeextension.common.container.data;

import com.github.bsideup.jabel.Desugar;
import github.kasuminova.ecoaeextension.common.block.ecotech.efabricator.prop.Levels;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Desugar
public record EFabricatorData(int length, boolean overclocked, boolean activeCooling,
                              int maxParallelism,
                              int coolant, int maxCoolant, int hotCoolant, int maxHotCoolant,
                              int energyStored, long totalCrafted,
                              Levels level,
                              List<WorkerStatus> workers) {

    public void write(final ByteBuf buf) {
        buf.writeInt(length);
        buf.writeBoolean(overclocked);
        buf.writeBoolean(activeCooling);
        buf.writeInt(maxParallelism);
        buf.writeInt(coolant);
        buf.writeInt(maxCoolant);
        buf.writeInt(hotCoolant);
        buf.writeInt(maxHotCoolant);
        buf.writeInt(energyStored);
        buf.writeLong(totalCrafted);
        buf.writeByte(level.ordinal());
        workers.forEach(worker -> {
            ByteBufUtils.writeItemStack(buf, worker.crafting);
            buf.writeInt(worker.queueLength);
        });
    }

    public static EFabricatorData read(final ByteBuf buf) {
        int len = buf.readInt();
        boolean overclocked = buf.readBoolean();
        boolean activeCooling = buf.readBoolean();
        int maxParallelism = buf.readInt();
        int coolant = buf.readInt();
        int maxCoolant = buf.readInt();
        int hotCoolant = buf.readInt();
        int maxHotCoolant = buf.readInt();
        int energyStored = buf.readInt();
        long totalCrafted = buf.readLong();
        Levels level = Levels.values()[buf.readByte()];
        List<WorkerStatus> workers = new ArrayList<>(len);
        IntStream.range(0, len)
                .mapToObj(i -> ByteBufUtils.readItemStack(buf))
                .forEach(crafting -> workers.add(new WorkerStatus(crafting, buf.readInt())));
        return new EFabricatorData(len, overclocked, activeCooling, maxParallelism, coolant, maxCoolant, hotCoolant, maxHotCoolant, energyStored, totalCrafted, level, workers);
    }

    @Desugar
    public record WorkerStatus(ItemStack crafting, int queueLength) {
    }

}
