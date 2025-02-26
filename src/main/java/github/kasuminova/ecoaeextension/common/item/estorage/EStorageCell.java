package github.kasuminova.ecoaeextension.common.item.estorage;

import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.storage.data.IAEStack;
import appeng.items.AEBaseItem;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.util.Platform;
import github.kasuminova.ecoaeextension.common.block.ecotech.estorage.prop.DriveStorageLevel;
import github.kasuminova.ecoaeextension.common.core.CreativeTabNovaEng;
import github.kasuminova.ecoaeextension.common.estorage.EStorageCellHandler;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.List;

public abstract class EStorageCell<T extends IAEStack<T>> extends AEBaseItem implements IStorageCell<T> {
    protected final DriveStorageLevel level;
    protected final int totalBytes;
    protected final int byteMultiplier;

    public EStorageCell(DriveStorageLevel level, final int millionBytes, final int byteMultiplier) {
        this.level = level;
        this.totalBytes = (millionBytes * 1000) * 1024;
        this.byteMultiplier = byteMultiplier;
        this.setMaxStackSize(1);
        this.setCreativeTab(CreativeTabNovaEng.INSTANCE);
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("DataFlowIssue")
    protected void addCheckedInformation(final ItemStack stack, final World world, final List<String> lines, final ITooltipFlag advancedTooltips) {
        super.addCheckedInformation(stack, world, lines, advancedTooltips);
        AEApi.instance()
                .client()
                .addCellInformation(EStorageCellHandler.getHandler(stack).getCellInventory(stack, null, this.getChannel()), lines);
        lines.add(I18n.format("novaeng.estorage_cell.insert.tip"));
        lines.add(I18n.format("novaeng.estorage_cell.extract.tip"));
        if (level == DriveStorageLevel.B) {
            lines.add(I18n.format("novaeng.estorage_cell.l6.tip"));
        }
        if (level == DriveStorageLevel.C) {
            lines.add(I18n.format("novaeng.estorage_cell.l9.tip"));
        }
    }

    @Override
    public double getIdleDrain() {
        return (double) totalBytes / 1024 / 1024;
    }

    public DriveStorageLevel getLevel() {
        return level;
    }

    public int getByteMultiplier() {
        return byteMultiplier;
    }

    @Override
    public int getBytes(@Nonnull final ItemStack cellItem) {
        return totalBytes;
    }

    @Override
    public boolean isBlackListed(@Nonnull final ItemStack cellItem, @Nonnull final T requestedAddition) {
        return false;
    }

    @Override
    public boolean storableInStorageCell() {
        return false;
    }

    @Override
    public boolean isStorageCell(@Nonnull final ItemStack i) {
        return true;
    }

    @Override
    public boolean isEditable(final ItemStack is) {
        return true;
    }

    @Override
    public IItemHandler getUpgradesInventory(final ItemStack is) {
        return new CellUpgrades(is, 2);
    }

    @Override
    public IItemHandler getConfigInventory(final ItemStack is) {
        return new CellConfig(is);
    }

    @Override
    public FuzzyMode getFuzzyMode(final ItemStack is) {
        final String fz = Platform.openNbtData(is).getString("FuzzyMode");
        try {
            return FuzzyMode.valueOf(fz);
        } catch (final Throwable t) {
            return FuzzyMode.IGNORE_ALL;
        }
    }

    @Override
    public void setFuzzyMode(final ItemStack is, final FuzzyMode fzMode) {
        Platform.openNbtData(is).setString("FuzzyMode", fzMode.name());
    }
}
