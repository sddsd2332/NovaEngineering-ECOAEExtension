package github.kasuminova.ecoaeextension.common.item.ecalculator;

import github.kasuminova.ecoaeextension.ECOAEExtension;
import github.kasuminova.ecoaeextension.common.block.ecotech.ecalculator.prop.DriveStorageLevel;
import github.kasuminova.ecoaeextension.common.core.CreativeTabNovaEng;
import github.kasuminova.ecoaeextension.common.crafttweaker.util.NovaEngUtils;
import lombok.Getter;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;
import java.util.List;

public class ECalculatorCell extends Item {

    public static final ECalculatorCell L4 = new ECalculatorCell(DriveStorageLevel.A, 64);
    public static final ECalculatorCell L6 = new ECalculatorCell(DriveStorageLevel.B, 1024);
    public static final ECalculatorCell L9 = new ECalculatorCell(DriveStorageLevel.C, 16384);

    @Getter
    protected final DriveStorageLevel level;
    @Getter
    protected final long totalBytes;

    public ECalculatorCell(DriveStorageLevel level, final long millionBytes) {
        this.level = level;
        this.totalBytes = (millionBytes * 1000) * 1024;
        this.setMaxStackSize(1);
        this.setCreativeTab(CreativeTabNovaEng.INSTANCE);
        this.setRegistryName(new ResourceLocation(ECOAEExtension.MOD_ID, "ecalculator_cell_" + millionBytes + "m"));
        this.setTranslationKey(ECOAEExtension.MOD_ID + '.' + "ecalculator_cell_" + millionBytes + "m");
    }

    @Override
    public void addInformation(@Nonnull final ItemStack stack, @Nullable final World worldIn, final List<String> tooltip, @Nonnull final ITooltipFlag flagIn) {
        tooltip.add(I18n.format("novaeng.ecalculator_cell.insert.tip"));
        tooltip.add(I18n.format("novaeng.ecalculator_cell.extract.tip"));
        tooltip.add(I18n.format("novaeng.ecalculator_cell.tip.0"));
        tooltip.add(I18n.format("novaeng.ecalculator_cell.tip.1"));
        tooltip.add(I18n.format("novaeng.ecalculator_cell.tip.2"));
        final ECalculatorCell cell = (ECalculatorCell) stack.getItem();
        final boolean shiftPressed = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
        tooltip.add(I18n.format("novaeng.ecalculator_cell.tip.3",
                shiftPressed ? NovaEngUtils.formatNumber(cell.totalBytes) : NovaEngUtils.formatDecimal(cell.totalBytes))
        );
        if (cell == L6) {
            tooltip.add(I18n.format("novaeng.ecalculator_cell.l6.tip"));
        } else if (cell == L9) {
            tooltip.add(I18n.format("novaeng.ecalculator_cell.l9.tip"));
        }
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }


}
