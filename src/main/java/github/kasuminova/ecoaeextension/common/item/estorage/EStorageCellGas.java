package github.kasuminova.ecoaeextension.common.item.estorage;

import appeng.api.AEApi;
import appeng.api.storage.IStorageChannel;
import com.mekeng.github.common.me.data.IAEGasStack;
import com.mekeng.github.common.me.storage.IGasStorageChannel;
import github.kasuminova.ecoaeextension.ECOAEExtension;
import github.kasuminova.ecoaeextension.common.block.ecotech.estorage.prop.DriveStorageLevel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class EStorageCellGas extends EStorageCell<IAEGasStack> {

    public static final EStorageCellGas LEVEL_A = new EStorageCellGas(DriveStorageLevel.A, 16, 4);
    public static final EStorageCellGas LEVEL_B = new EStorageCellGas(DriveStorageLevel.B, 64, 16);
    public static final EStorageCellGas LEVEL_C = new EStorageCellGas(DriveStorageLevel.C,256, 64);

    public EStorageCellGas(final DriveStorageLevel level, final int millionBytes, final int byteMultiplier) {
        super(level, millionBytes, byteMultiplier);
        setRegistryName(new ResourceLocation(ECOAEExtension.MOD_ID, "estorage_cell_gas_" + millionBytes + "m"));
        setTranslationKey(ECOAEExtension.MOD_ID + '.' + "estorage_cell_gas_" + millionBytes + "m");
    }

    @Override
    public int getTotalTypes(@NotNull ItemStack itemStack) {
        return 25;
    }

    @Override
    public int getBytesPerType(@NotNull ItemStack itemStack) {
        return byteMultiplier * 1024;
    }


    @NotNull
    @Override
    public IStorageChannel<IAEGasStack> getChannel() {
        return AEApi.instance().storage().getStorageChannel(IGasStorageChannel.class);
    }
}
