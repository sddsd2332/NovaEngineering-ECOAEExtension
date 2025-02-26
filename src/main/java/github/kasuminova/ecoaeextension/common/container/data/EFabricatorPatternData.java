package github.kasuminova.ecoaeextension.common.container.data;

import appeng.tile.inventory.AppEngInternalInventory;
import com.github.bsideup.jabel.Desugar;
import github.kasuminova.ecoaeextension.common.tile.ecotech.efabricator.EFabricatorController;
import github.kasuminova.ecoaeextension.common.tile.ecotech.efabricator.EFabricatorPatternBus;
import github.kasuminova.ecoaeextension.common.util.BlockPos2ValueMap;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Desugar
public record EFabricatorPatternData(Map<BlockPos, Set<PatternData>> patterns) {

    public static EFabricatorPatternData ofFull(final EFabricatorController efController) {
        final Map<BlockPos, Set<PatternData>> patterns = new BlockPos2ValueMap<>();
        for (final EFabricatorPatternBus patternBus : efController.getPatternBuses()) {
            final AppEngInternalInventory patternInv = patternBus.getPatterns();
            final BlockPos pos = patternBus.getPos();
            for (int i = 0; i < patternInv.getSlots(); i++) {
                final ItemStack patternStack = patternInv.getStackInSlot(i);
                if (!patternStack.isEmpty()) {
                    patterns.computeIfAbsent(pos, key -> new ObjectLinkedOpenHashSet<>())
                            .add(new PatternData(pos, i, patternStack));
                }
            }
        }
        return new EFabricatorPatternData(patterns);
    }

    public static EFabricatorPatternData of(final PatternData data) {
        return new EFabricatorPatternData(Collections.singletonMap(data.pos(), Collections.singleton(data)));
    }

    public void writeTo(final ByteBuf buf) {
        buf.writeByte(patterns.size());
        patterns.forEach((pos, patternSet) -> {
            buf.writeLong(pos.toLong());
            buf.writeByte(patternSet.size());
            patternSet.forEach(pattern -> {
                buf.writeByte(pattern.slot());
                ByteBufUtils.writeItemStack(buf, pattern.pattern());
            });
        });
    }

    public static EFabricatorPatternData readFrom(final ByteBuf buf) {
        final int len = buf.readByte();
        Map<BlockPos, Set<PatternData>> patterns = new BlockPos2ValueMap<>();
        for (int i = 0; i < len; i++) {
            final BlockPos pos = BlockPos.fromLong(buf.readLong());
            final int size = buf.readByte();
            final Set<PatternData> patternSet = new ObjectLinkedOpenHashSet<>();
            for (int j = 0; j < size; j++) {
                PatternData patternData = new PatternData(pos, buf.readByte(), ByteBufUtils.readItemStack(buf));
                patternSet.add(patternData);
            }
            patterns.put(pos, patternSet);
        }

        return new EFabricatorPatternData(patterns);
    }

    @Desugar
    public record PatternData(BlockPos pos, int slot, ItemStack pattern) {
    }

}
