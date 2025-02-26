package github.kasuminova.ecoaeextension.common.item.estorage;

import github.kasuminova.ecoaeextension.common.block.ecotech.estorage.BlockEStorageEnergyCell;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;

public class ItemBlockEStorageEnergyCell extends ItemBlock {

    public static final ItemBlockEStorageEnergyCell L4 = new ItemBlockEStorageEnergyCell(BlockEStorageEnergyCell.L4);
    public static final ItemBlockEStorageEnergyCell L6 = new ItemBlockEStorageEnergyCell(BlockEStorageEnergyCell.L6);
    public static final ItemBlockEStorageEnergyCell L9 = new ItemBlockEStorageEnergyCell(BlockEStorageEnergyCell.L9);

    public ItemBlockEStorageEnergyCell(final Block block) {
        super(block);
    }

}
