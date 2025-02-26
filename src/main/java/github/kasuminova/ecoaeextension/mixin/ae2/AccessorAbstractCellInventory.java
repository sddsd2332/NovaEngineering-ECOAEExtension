package github.kasuminova.ecoaeextension.mixin.ae2;

import appeng.me.storage.AbstractCellInventory;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = AbstractCellInventory.class, remap = false)
public interface AccessorAbstractCellInventory {

    @Accessor
    NBTTagCompound getTagCompound();

    @Accessor
    int getMaxItemTypes();

    @Accessor
    short getStoredItemTypes();

    @Accessor
    void setStoredItemTypes(short storedItemTypes);

    @Accessor
    long getStoredItemCount();

    @Accessor
    void setStoredItemCount(long storedItemCount);

    @Accessor
    void setIsPersisted(boolean isPersisted);

    @Accessor
    boolean getIsPersisted();

}
