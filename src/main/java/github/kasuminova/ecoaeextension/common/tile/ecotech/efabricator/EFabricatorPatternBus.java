package github.kasuminova.ecoaeextension.common.tile.ecotech.efabricator;

import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.me.GridAccessException;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;
import com.glodblock.github.util.FluidCraftingPatternDetails;
import github.kasuminova.mmce.common.util.PatternItemFilter;
import github.kasuminova.ecoaeextension.ECOAEExtension;
import github.kasuminova.ecoaeextension.common.container.ContainerEFabricatorPatternSearch;
import github.kasuminova.ecoaeextension.common.container.data.EFabricatorPatternData;
import github.kasuminova.ecoaeextension.common.network.PktEFabricatorPatternSearchGUIUpdate;
import hellfirepvp.modularmachinery.ModularMachinery;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class EFabricatorPatternBus extends EFabricatorPart implements IAEAppEngInventory {

    public static final int PATTERN_SLOTS = 12 * 6;

    protected final AppEngInternalInventory patterns = new AppEngInternalInventory(this, PATTERN_SLOTS, 1, PatternItemFilter.INSTANCE);
    protected final List<ICraftingPatternDetails> details = new ObjectArrayList<>(PATTERN_SLOTS);

    public EFabricatorPatternBus() {
        // Initialize details...
        IntStream.range(0, PATTERN_SLOTS).<ICraftingPatternDetails>mapToObj(i -> null).forEach(details::add);
    }

    protected void refreshPatterns() {
        for (int i = 0; i < PATTERN_SLOTS; i++) {
            refreshPattern(i);
        }
        notifyPatternChanged();
    }

    protected void refreshPattern(final int slot) {
        details.set(slot, null);

        ItemStack pattern = patterns.getStackInSlot(slot);
        Item item = pattern.getItem();
        if (pattern.isEmpty() || !(item instanceof ICraftingPatternItem patternItem)) {
            return;
        }

        ICraftingPatternDetails detail = patternItem.getPatternForItem(pattern, getWorld());
        if (detail != null && (detail.isCraftable() || detail instanceof FluidCraftingPatternDetails)) {
            details.set(slot, detail);
        }
    }

    public AppEngInternalInventory getPatterns() {
        return patterns;
    }

    public List<ICraftingPatternDetails> getDetails() {
        return details.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public int getValidPatterns() {
        return (int) details.stream().filter(Objects::nonNull).count();
    }

    @Override
    public void saveChanges() {
        markNoUpdateSync();
    }

    @Override
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc, final ItemStack removedStack, final ItemStack newStack) {
        refreshPattern(slot);
        notifyPatternChanged();
        sendPatternSearchGUIUpdateToClient(slot);
    }

    private void notifyPatternChanged() {
        if (this.partController == null) {
            return;
        }
        try {
            EFabricatorMEChannel channel = this.partController.getChannel();
            if (channel != null && channel.getProxy().isActive()) {
                channel.getProxy().getGrid().postEvent(new MENetworkCraftingPatternChange(channel, channel.getProxy().getNode()));
            }
        } catch (GridAccessException ignored) {
        }
        this.partController.recalculateEnergyUsage();
    }

    private void sendPatternSearchGUIUpdateToClient(final int slot) {
        if (this.partController == null) {
            return;
        }

        List<EntityPlayerMP> players = new ArrayList<>();
        world.playerEntities.stream()
                .filter(EntityPlayerMP.class::isInstance)
                .map(EntityPlayerMP.class::cast)
                .forEach(playerMP -> {
                    if (playerMP.openContainer instanceof ContainerEFabricatorPatternSearch efPatternSearch) {
                        if (efPatternSearch.getOwner() == this.partController) {
                            players.add(playerMP);
                        }
                    }
                });

        if (!players.isEmpty()) {
            PktEFabricatorPatternSearchGUIUpdate pktUpdate = new PktEFabricatorPatternSearchGUIUpdate(
                    PktEFabricatorPatternSearchGUIUpdate.UpdateType.SINGLE,
                    EFabricatorPatternData.of(
                            new EFabricatorPatternData.PatternData(getPos(), slot, patterns.getStackInSlot(slot))
                    )
            );
            players.forEach(player -> ECOAEExtension.NET_CHANNEL.sendTo(pktUpdate, player));
        }
    }

    @Override
    public void validate() {
        super.validate();
        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
            ModularMachinery.EXECUTE_MANAGER.addSyncTask(this::refreshPatterns);
        }
    }

    @Override
    public boolean hasCapability(@Nonnull final Capability<?> capability, @Nullable final EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull final Capability<T> capability, @Nullable final EnumFacing facing) {
        Capability<IItemHandler> cap = CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
        if (capability == cap) {
            return cap.cast(patterns);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void readCustomNBT(final NBTTagCompound compound) {
        super.readCustomNBT(compound);
        patterns.readFromNBT(compound.getCompoundTag("patterns"));
    }

    @Override
    public void writeCustomNBT(final NBTTagCompound compound) {
        super.writeCustomNBT(compound);
        patterns.writeToNBT(compound, "patterns");
    }

}
