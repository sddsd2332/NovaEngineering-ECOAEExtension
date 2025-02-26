package github.kasuminova.ecoaeextension.common.tile.ecotech.ecalculator;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkCraftingCpuChange;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.me.GridAccessException;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.me.helpers.MachineSource;
import github.kasuminova.ecoaeextension.common.block.ecotech.ecalculator.BlockECalculatorMEChannel;
import hellfirepvp.modularmachinery.ModularMachinery;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class ECalculatorMEChannel extends ECalculatorPart implements IActionHost, IGridProxyable {

    protected final AENetworkProxy proxy = new AENetworkProxy(this, "channel", getVisualItemStack(), true);
    protected final IActionSource source = new MachineSource(this);

    private boolean wasActive = false;

    public ECalculatorMEChannel() {
        this.proxy.setIdlePowerUsage(1.0D);
        this.proxy.setFlags(GridFlags.REQUIRE_CHANNEL, GridFlags.DENSE_CAPACITY);
    }

    public IActionSource getSource() {
        return source;
    }

    public ItemStack getVisualItemStack() {
        ECalculatorController controller = getController();
        return new ItemStack(Item.getItemFromBlock(controller == null ? BlockECalculatorMEChannel.INSTANCE : controller.getParentController()), 1, 0);
    }

    @MENetworkEventSubscribe
    public void stateChange(final MENetworkPowerStatusChange c) {
        final boolean currentActive = this.proxy.isActive();
        if (this.wasActive != currentActive) {
            this.wasActive = currentActive;
            postCPUClusterChangeEvent();
        }
    }

    @MENetworkEventSubscribe
    public void stateChange(final MENetworkChannelsChanged c) {
        final boolean currentActive = this.proxy.isActive();
        if (this.wasActive != currentActive) {
            this.wasActive = currentActive;
            postCPUClusterChangeEvent();
        }
    }

    protected void postCPUClusterChangeEvent() {
        if (this.proxy.isActive()) {
            try {
                this.proxy.getGrid().postEvent(new MENetworkCraftingCpuChange(this.proxy.getNode()));
            } catch (final GridAccessException ignored) {
            }
        }
    }

    // Clusters

    public List<CraftingCPUCluster> getCPUs() {
        final boolean currentActive = this.proxy.isActive();
        if (!currentActive || !isAssembled()) {
            return Collections.emptyList();
        }
        ECalculatorController controller = getController();
        if (controller == null) {
            return Collections.emptyList();
        }
        return controller.getClusterList();
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
        ModularMachinery.EXECUTE_MANAGER.addSyncTask(proxy::onReady);
    }

    @Override
    public void onDisassembled() {
        super.onDisassembled();
        proxy.setVisualRepresentation(getVisualItemStack());
        proxy.invalidate();
    }


}
