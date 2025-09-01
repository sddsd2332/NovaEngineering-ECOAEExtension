package github.kasuminova.ecoaeextension.common.tile.ecotech.efabricator;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.me.helpers.MachineSource;
import appeng.util.Platform;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.common.item.fake.FakeItemRegister;
import com.glodblock.github.util.FluidCraftingPatternDetails;
import github.kasuminova.mmce.common.util.PatternItemFilter;
import github.kasuminova.ecoaeextension.common.block.ecotech.efabricator.BlockEFabricatorMEChannel;
import hellfirepvp.modularmachinery.ModularMachinery;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class EFabricatorMEChannel extends EFabricatorPart implements ICraftingProvider, IActionHost, IGridProxyable {

    protected final AENetworkProxy proxy = new AENetworkProxy(this, "channel", getVisualItemStack(), true);
    protected final IActionSource source = new MachineSource(this);

    private boolean wasActive = false;

    public EFabricatorMEChannel() {
        this.proxy.setIdlePowerUsage(1.0D);
        this.proxy.setFlags(GridFlags.REQUIRE_CHANNEL, GridFlags.DENSE_CAPACITY);
    }

    public IActionSource getSource() {
        return source;
    }

    public ItemStack getVisualItemStack() {
        EFabricatorController controller = getController();
        return new ItemStack(Item.getItemFromBlock(controller == null ? BlockEFabricatorMEChannel.INSTANCE : controller.getParentController()), 1, 0);
    }

    @MENetworkEventSubscribe
    public void stateChange(final MENetworkPowerStatusChange c) {
        postPatternChangeEvent();
    }

    @MENetworkEventSubscribe
    public void stateChange(final MENetworkChannelsChanged c) {
        postPatternChangeEvent();
    }

    protected void postPatternChangeEvent() {
        final boolean currentActive = this.proxy.isActive();
        if (this.wasActive != currentActive) {
            this.wasActive = currentActive;
            try {
                this.proxy.getGrid().postEvent(new MENetworkCraftingPatternChange(this, proxy.getNode()));
            } catch (final GridAccessException ignored) {
            }
        }
    }

    // Crafting Provider

    @Override
    public void provideCrafting(final ICraftingProviderHelper craftingTracker) {
        EFabricatorController controller = getController();
        if (controller == null) {
            return;
        }

        List<EFabricatorPatternBus> patternBuses = controller.getPatternBuses();
        patternBuses.stream()
                .flatMap(patternBus -> patternBus.getDetails().stream())
                .filter(details -> details.isCraftable() || details instanceof FluidCraftingPatternDetails)
                .forEach(details -> craftingTracker.addCraftingOption(this, details));
    }

    @Override
    public boolean pushPattern(final ICraftingPatternDetails pattern, final InventoryCrafting table) {
        if (isBusy()) {
            return false;
        }

        if (!pattern.isCraftable()) {
            if (pattern instanceof FluidCraftingPatternDetails f) {
                return pushFluidPattern(f,table);
            }
            return false;
        }

        ItemStack output = pattern.getOutput(table, this.getWorld());
        if (output.isEmpty()) {
            return false;
        }

        ItemStack[] remaining = new ItemStack[9];
        int size = 0;
        for (int i = 0; i < Math.min(table.getSizeInventory(), 9); ++i) {
            var item = table.getStackInSlot(i);
            if (item.isEmpty()){
                remaining[i] = ItemStack.EMPTY;
            } else {
                if (size == 0) {
                    size = item.getCount();
                }
                remaining[i] = getContainerItem(item);
            }
        }

        output.setCount(output.getCount() * size);

        return partController.offerWork(new EFabricatorWorker.CraftWork(remaining, output, size));
    }

    protected boolean pushFluidPattern(final FluidCraftingPatternDetails pattern,final InventoryCrafting table) {
        IAEItemStack[] outputs = pattern.getOutputs();
        ItemStack output = outputs[0] != null ? outputs[0].getCachedItemStack(outputs[0].getStackSize()) : ItemStack.EMPTY;

        if (output.isEmpty())return false;

        ItemStack[] remaining = new ItemStack[9];
        int size = 0;
        for (int i = 0; i < Math.min(table.getSizeInventory(), 9); ++i) {
            var item = table.getStackInSlot(i);
            if (item.isEmpty()){
                remaining[i] = ItemStack.EMPTY;
            } else {
                if (size == 0) {
                    size = item.getCount();
                    if (item.getItem() instanceof ItemFluidPacket) {
                        var amount = ((FluidStack) FakeItemRegister.getStack(item)).amount;
                        var pamount = ((FluidStack) FakeItemRegister.getStack(pattern.getInputs()[i])).amount;
                        size = amount/pamount;
                    }
                }
                remaining[i] = getContainerItem(item);
            }
        }

        output.setCount(output.getCount() * size);

        return partController.offerWork(new EFabricatorWorker.CraftWork(remaining, output, size));
    }

    private static ItemStack getContainerItem(ItemStack stackInSlot) {
        if (stackInSlot == null) {
            return ItemStack.EMPTY;
        } else {
            Item i = stackInSlot.getItem();
            if (i != null && i.hasContainerItem(stackInSlot)) {
                ItemStack ci = i.getContainerItem(stackInSlot);
                if (!ci.isEmpty() && ci.isItemStackDamageable() && ci.getItemDamage() == ci.getMaxDamage()) {
                    ci = ItemStack.EMPTY;
                }

                ci.setCount(stackInSlot.getCount());
                return ci;
            } else if (!stackInSlot.isEmpty()) {
                stackInSlot.setCount(0);
                return stackInSlot;
            } else return ItemStack.EMPTY;
        }
    }

    public boolean insertPattern(final ItemStack patternStack) {
        if (!PatternItemFilter.INSTANCE.allowInsert(null, -1, patternStack)) {
            return false;
        }
        if (partController != null) {
            return partController.insertPattern(patternStack);
        }
        return false;
    }

    @Override
    public boolean isBusy() {
        if (partController != null) {
            return partController.isQueueFull();
        }
        return true;
    }

    // Misc

    @Nonnull
    @Override
    public IGridNode getActionableNode() {
        return proxy.getNode();
    }

    @Nonnull
    @Override
    public AENetworkProxy getProxy() {
        return proxy;
    }

    @Nonnull
    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this);
    }

    @Override
    public void gridChanged() {
    }

    @Nullable
    @Override
    public IGridNode getGridNode(@Nonnull final AEPartLocation dir) {
        return proxy.getNode();
    }

    @Nonnull
    @Override
    public AECableType getCableConnectionType(@Nonnull final AEPartLocation dir) {
        return AECableType.DENSE_SMART;
    }

    @Override
    public void securityBreak() {
        getWorld().destroyBlock(getPos(), true);
    }

    @Override
    public void readCustomNBT(final NBTTagCompound compound) {
        super.readCustomNBT(compound);
        proxy.readFromNBT(compound);
    }

    @Override
    public void writeCustomNBT(final NBTTagCompound compound) {
        super.writeCustomNBT(compound);
        proxy.writeToNBT(compound);
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        proxy.onChunkUnload();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        proxy.invalidate();
    }

    @Override
    public void onAssembled() {
        super.onAssembled();
        proxy.setVisualRepresentation(getVisualItemStack());
        ModularMachinery.EXECUTE_MANAGER.addSyncTask(() -> {
            proxy.onReady();
            partController.recalculateEnergyUsage();
        });
    }

    @Override
    public void onDisassembled() {
        super.onDisassembled();
        proxy.setVisualRepresentation(getVisualItemStack());
        proxy.invalidate();
    }

}
