package github.kasuminova.ecoaeextension.mixin.ae2;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.networking.crafting.ICraftingMedium;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.ContainerNull;
import appeng.crafting.CraftingLink;
import appeng.crafting.MECraftingInventory;
import appeng.helpers.DualityInterface;
import appeng.me.cache.CraftingGridCache;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.helpers.MachineSource;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import co.neeve.nae2.common.crafting.patterntransform.PatternTransformWrapper;
import co.neeve.nae2.common.helpers.VirtualPatternDetails;
import co.neeve.nae2.common.interfaces.ICancellingCraftingMedium;
import com.circulation.random_complement.client.RCSettings;
import com.circulation.random_complement.client.buttonsetting.IntelligentBlocking;
import com.circulation.random_complement.common.interfaces.RCIConfigurableObject;
import com.glodblock.github.coremod.CoreModHooks;
import com.glodblock.github.inventory.FluidConvertingInventoryCrafting;
import com.glodblock.github.util.FluidCraftingPatternDetails;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import github.kasuminova.ecoaeextension.common.tile.MEPatternProviderNova;
import github.kasuminova.ecoaeextension.common.tile.ecotech.efabricator.EFabricatorMEChannel;
import github.kasuminova.ecoaeextension.common.tile.ecotech.efabricator.EFabricatorWorker;
import github.kasuminova.ecoaeextension.common.util.MediumType;
import github.kasuminova.mmce.common.tile.MEPatternProvider;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import lombok.val;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.stream.Collectors;

@Mixin(value = CraftingCPUCluster.class, remap = false ,priority = 0)
public abstract class MixinCraftingCPUClusterTwo {

    @Shadow
    protected abstract void postChange(IAEItemStack diff, IActionSource src);

    @Shadow
    protected abstract void postCraftingStatusChange(IAEItemStack diff);

    @Shadow
    private int remainingOperations;
    @Shadow
    private MachineSource machineSrc;
    @Shadow
    private MECraftingInventory inventory;

    @Shadow
    @Final
    private Map<ICraftingPatternDetails, AccessorTaskProgress> tasks;

    @Shadow
    protected abstract boolean canCraft(ICraftingPatternDetails details, IAEItemStack[] condensedInputs);

    @Shadow
    @Final
    private Map<ICraftingPatternDetails, Queue<ICraftingMedium>> visitedMediums;

    @Shadow
    protected abstract World getWorld();

    @Shadow
    private boolean somethingChanged;

    @Shadow
    private IItemList<IAEItemStack> waitingFor;

    @Shadow
    protected abstract void markDirty();

    @Shadow
    public abstract void cancel();

    @Shadow
    protected abstract void completeJob();

    @Shadow
    public abstract IAEItemStack injectItems(IAEItemStack input, Actionable type, IActionSource src);

    @Unique
    private boolean r$IgnoreParallel = false;

    @Unique
    private long r$craftingFrequency = 0;

    /**
     * @author circulation
     * @reason 完全覆写样板发配方法
     */
    @Overwrite
    private void executeCrafting(IEnergyGrid eg, CraftingGridCache cc) {
        val voidSet = new ObjectArrayList<IAEItemStack>();
        Iterator<Map.Entry<ICraftingPatternDetails, AccessorTaskProgress>> i = this.tasks.entrySet().iterator();

        while (i.hasNext()) {
            Map.Entry<ICraftingPatternDetails, AccessorTaskProgress> e = i.next();
            var value = ((AccessorTaskProgress) e.getValue());
            if (value.getValue() <= 0L) {
                i.remove();
            } else {
                var key = e.getKey();

                long max = 0;
                var list = (key.isCraftable() || key instanceof FluidCraftingPatternDetails) ? key.getCondensedOutputs() : key.getCondensedInputs();
                for (IAEItemStack stack1 : list) {
                    long size = stack1.getStackSize();
                    if (size > max) max = size;
                }

                this.r$craftingFrequency = e.getValue().getValue();
                if (max * this.r$craftingFrequency > Integer.MAX_VALUE) {
                    this.r$craftingFrequency = Integer.MAX_VALUE / max;
                }

                if (this.canCraft(key, key.getCondensedInputs())) {
                    if (key instanceof VirtualPatternDetails) {
                        this.completeJob();
                        this.cancel();
                        return;
                    }

                    boolean didPatternCraft;
                    doWhileCraftingLoop:do {
                        InventoryCrafting ic = null;
                        didPatternCraft = false;

                        if (!this.visitedMediums.containsKey(key) || this.visitedMediums.get(key).isEmpty()) {
                            this.visitedMediums.put(key, new ArrayDeque<>(cc.getMediums(key).stream().filter(Objects::nonNull).collect(Collectors.toList())));
                        }

                        while (!this.visitedMediums.get(key).isEmpty()) {
                            ICraftingMedium m = this.visitedMediums.get(key).poll();
                            if (value.getValue() > 0L && m != null && !m.isBusy()) {
                                MediumType mediumType = r$specialMediumTreatment(m, key);
                                if (ic == null) {
                                    IAEItemStack[] input = key.getInputs();
                                    double sum = 0;

                                    for (IAEItemStack anInput : input) {
                                        if (anInput != null) {
                                            sum += (double) CoreModHooks.getFluidSize(anInput);
                                        }
                                    }

                                    double energy;
                                    long s = 1;
                                    if (mediumType != MediumType.NULL) {
                                        double sum1 = sum * this.r$craftingFrequency;
                                        double o = eg.extractAEPower(sum1, Actionable.SIMULATE, PowerMultiplier.CONFIG);
                                        if (o < sum1 - 0.01) {
                                            this.r$craftingFrequency = (long) (o / sum1 * this.r$craftingFrequency);
                                            s = Math.max(1, this.r$craftingFrequency);
                                        }
                                    }
                                    energy = eg.extractAEPower(sum * s, Actionable.MODULATE, PowerMultiplier.CONFIG);

                                    if (energy < sum - 0.01) {
                                        continue;
                                    }

                                    val l = key.isCraftable() ? 3 : 10;
                                    ic = new FluidConvertingInventoryCrafting(new ContainerNull(), l, l);

                                    boolean found = false;

                                    for (int x = 0; x < input.length; ++x) {
                                        if (input[x] != null) {
                                            found = false;
                                            if (key.isCraftable()) {
                                                Collection<IAEItemStack> itemList;
                                                if (key.canSubstitute()) {
                                                    List<IAEItemStack> substitutes = key.getSubstituteInputs(x);
                                                    itemList = new ObjectArrayList<>(substitutes.size());

                                                    for (IAEItemStack stack : substitutes) {
                                                        itemList.addAll(this.inventory.getItemList().findFuzzy(stack, FuzzyMode.IGNORE_ALL));
                                                    }
                                                } else {
                                                    IAEItemStack item = this.inventory.getItemList().findPrecise(input[x]);
                                                    if (item != null) {
                                                        itemList = ObjectLists.singleton(item);
                                                    } else if (input[x].getDefinition().getItem().isDamageable()) {
                                                        itemList = this.inventory.getItemList().findFuzzy(input[x], FuzzyMode.IGNORE_ALL);
                                                    } else {
                                                        itemList = ObjectLists.emptyList();
                                                    }
                                                }

                                                for (IAEItemStack fuzz : itemList) {
                                                    fuzz = fuzz.copy();
                                                    fuzz.setStackSize(input[x].getStackSize());
                                                    if (key.isValidItemForSlot(x, fuzz.createItemStack(), this.getWorld())) {
                                                        IAEItemStack ais = r$extractItemsR(this.inventory, fuzz, Actionable.MODULATE, this.machineSrc, mediumType);
                                                        ItemStack is = ais == null ? ItemStack.EMPTY : ais.createItemStack();
                                                        if (!is.isEmpty()) {
                                                            IAEItemStack receiver = AEItemStack.fromItemStack(is);
                                                            if (mediumType == MediumType.EF)
                                                                receiver = receiver.copy().setStackSize(receiver.getStackSize() * this.r$craftingFrequency);

                                                            this.postChange(receiver, this.machineSrc);
                                                            ic.setInventorySlotContents(x, is);
                                                            found = true;
                                                            break;
                                                        }
                                                    }
                                                }
                                            } else {
                                                IAEItemStack ais = r$extractItemsR(this.inventory, input[x].copy(), Actionable.MODULATE, this.machineSrc, mediumType);
                                                ItemStack is = ais == null ? ItemStack.EMPTY : ais.createItemStack();
                                                if (!is.isEmpty()) {
                                                    IAEItemStack receiver = input[x];
                                                    if (mediumType != MediumType.NULL)
                                                        receiver = receiver.copy().setStackSize(receiver.getStackSize() * this.r$craftingFrequency);
                                                    this.postChange(receiver, this.machineSrc);
                                                    ic.setInventorySlotContents(x, is);

                                                    var count = is.getCount();
                                                    if (mediumType != MediumType.NULL)
                                                        count /= (int) this.r$craftingFrequency;
                                                    if (count == input[x].getStackSize()) {
                                                        found = true;
                                                        continue;
                                                    }
                                                }
                                            }

                                            if (!found) {
                                                break;
                                            }
                                        }
                                    }

                                    if (!found) {
                                        for (int x = 0; x < ic.getSizeInventory(); ++x) {
                                            ItemStack is = CoreModHooks.removeFluidPackets(ic, x);
                                            if (!is.isEmpty()) {
                                                this.inventory.injectItems(
                                                        CoreModHooks.wrapFluidPacketStack(
                                                                AEItemStack.fromItemStack(is)
                                                        ),
                                                        Actionable.MODULATE,
                                                        this.machineSrc
                                                );
                                            }
                                        }

                                        ic = null;
                                        break;
                                    }
                                }

                                ICraftingPatternDetails newDetails;
                                if (key instanceof PatternTransformWrapper wrapper) {
                                    newDetails = wrapper.getDelegate();
                                } else {
                                    newDetails = key;
                                }
                                if (m.pushPattern(newDetails, ic)) {
                                    this.somethingChanged = true;
                                    if (mediumType != MediumType.NULL && !this.r$IgnoreParallel) {
                                        this.remainingOperations -= (int) this.r$craftingFrequency;
                                    } else --this.remainingOperations;

                                    val outputs = key.getCondensedOutputs();
                                    if (m instanceof ICancellingCraftingMedium medium) {
                                        if (medium.shouldAutoComplete()) {
                                            Collections.addAll(voidSet, outputs);
                                        }
                                    }

                                    for (IAEItemStack out : outputs) {
                                        var iaeStack = out.copy();
                                        if (mediumType != MediumType.NULL)
                                            iaeStack.setStackSize(iaeStack.getStackSize() * this.r$craftingFrequency);
                                        r$postProcessing(iaeStack);
                                    }

                                    if (key.isCraftable()) {
                                        for (int x = 0; x < ic.getSizeInventory(); ++x) {
                                            ItemStack output = Platform.getContainerItem(CoreModHooks.removeFluidPackets(ic, x));
                                            if (!output.isEmpty()) {
                                                IAEItemStack cItem = AEItemStack.fromItemStack(output);
                                                if (mediumType == MediumType.EF)
                                                    cItem.setStackSize(cItem.getStackSize() * this.r$craftingFrequency);
                                                r$postProcessing(cItem);
                                            }
                                        }
                                    }

                                    ic = null;
                                    didPatternCraft = true;
                                    this.markDirty();
                                    if (mediumType == MediumType.NULL) {
                                        value.setValue(value.getValue() - 1);
                                    } else {
                                        value.setValue(value.getValue() - (this.r$craftingFrequency));
                                    }

                                    if (value.getValue() <= 0) {
                                        break doWhileCraftingLoop;
                                    }

                                    if (value.getValue() > 0L && this.remainingOperations == 0) {
                                        for (IAEItemStack output : voidSet) {
                                            this.r$nae2$ghostInject(output);
                                        }
                                        return;
                                    }

                                    if (m instanceof DualityInterface di && (di.getConfigManager().getSetting(Settings.BLOCK) == YesNo.YES && ((RCIConfigurableObject) di).r$getConfigManager().getSetting(RCSettings.IntelligentBlocking) == IntelligentBlocking.CLOSE)
                                    )
                                        break;
                                }
                            }
                        }

                        if (ic != null) {
                            for (int x = 0; x < ic.getSizeInventory(); ++x) {
                                ItemStack is = CoreModHooks.removeFluidPackets(ic, x);
                                if (!is.isEmpty()) {
                                    this.inventory.injectItems(CoreModHooks.wrapFluidPacketStack(AEItemStack.fromItemStack(is)), Actionable.MODULATE, this.machineSrc);
                                }
                            }
                        }
                    } while (didPatternCraft);
                }
            }
        }
        for (IAEItemStack output : voidSet) {
            this.r$nae2$ghostInject(output);
        }
    }

    @Unique
    private void r$postProcessing(IAEItemStack cItem) {
        this.postChange(cItem, this.machineSrc);
        this.waitingFor.add(cItem);
        this.postCraftingStatusChange(cItem);
    }

    @Unique
    @NotNull
    private MediumType r$specialMediumTreatment(ICraftingMedium m, ICraftingPatternDetails details) {
        if (m instanceof MEPatternProviderNova mep) {
            if (mep.getWorkMode() == MEPatternProvider.WorkModeSetting.DEFAULT
                    || mep.getWorkMode() == MEPatternProvider.WorkModeSetting.ENHANCED_BLOCKING_MODE) {

                for (IAEItemStack input : details.getCondensedInputs()) {
                    long size = input.getStackSize() * this.r$craftingFrequency;
                    var item = this.inventory.extractItems(input.copy().setStackSize(size), Actionable.SIMULATE, this.machineSrc);
                    if (item == null) continue;
                    if (item.getStackSize() < size) {
                        this.r$craftingFrequency = Math.max(1, item.getStackSize() / input.getStackSize());
                    }
                }

                if (mep.r$isIgnoreParallel()) {
                    this.r$IgnoreParallel = true;
                } else {
                    this.r$IgnoreParallel = false;
                    this.r$craftingFrequency = Math.min(this.remainingOperations, this.r$craftingFrequency);
                }
                return MediumType.MEPatternProvider;
            } else
                return MediumType.NULL;
        } else if (m instanceof EFabricatorMEChannel ef) {
            var max = 0;
            for (EFabricatorWorker worker : ef.getController().getWorkers()) {
                max += worker.getRemainingSpace();
            }
            for (IAEItemStack input : details.getInputs()) {
                if (input == null) continue;
                long size = this.r$craftingFrequency;
                var item = this.inventory.extractItems(input.copy().setStackSize(size), Actionable.SIMULATE, this.machineSrc);
                if (item == null) continue;
                if (item.getStackSize() < size) {
                    long size0 = item.getStackSize() / input.getStackSize();
                    if (size0 < 2) {
                        this.r$craftingFrequency = 1;
                    } else {
                        this.r$craftingFrequency = size0;
                    }
                }
            }
            this.r$craftingFrequency = Math.min(max, this.r$craftingFrequency);
            return MediumType.EF;
        } else
            return MediumType.NULL;
    }

    @Unique
    private IAEItemStack r$extractItemsR(MECraftingInventory instance, IAEItemStack receiver, Actionable mode, IActionSource src, MediumType mediumType) {
        if (mediumType != MediumType.NULL)
            receiver.setStackSize(receiver.getStackSize() * this.r$craftingFrequency);
        return instance.extractItems(receiver, mode, src);
    }

    @Unique
    private boolean r$nae2$ghostInjecting;

    @Unique
    protected void r$nae2$ghostInject(IAEItemStack output) {
        this.r$nae2$ghostInjecting = true;
        try {
            this.injectItems(output, Actionable.MODULATE, this.machineSrc);
        } finally {
            this.r$nae2$ghostInjecting = false;
        }
    }

    @WrapOperation(
            method = {"injectItems"},
            at = {@At(
                    value = "INVOKE",
                    target = "Lappeng/crafting/CraftingLink;injectItems(Lappeng/api/storage/data/IAEItemStack;Lappeng/api/config/Actionable;)Lappeng/api/storage/data/IAEItemStack;"
            )}
    )
    protected IAEItemStack wrapInjectItems(CraftingLink link, IAEItemStack item, Actionable actionable, Operation<IAEItemStack> operation) {
        return this.r$nae2$ghostInjecting ? null : (IAEItemStack) operation.call(link, item, actionable);
    }

    @WrapOperation(
            method = {"injectItems"},
            at = {@At(
                    value = "INVOKE",
                    target = "Lappeng/crafting/MECraftingInventory;injectItems(Lappeng/api/storage/data/IAEItemStack;Lappeng/api/config/Actionable;Lappeng/api/networking/security/IActionSource;)Lappeng/api/storage/data/IAEItemStack;"
            )}
    )
    protected IAEItemStack wrapInjectItems(MECraftingInventory link, IAEItemStack item, Actionable actionable, IActionSource source, Operation<IAEItemStack> operation) {
        return this.r$nae2$ghostInjecting ? null : (IAEItemStack) operation.call(link, item, actionable, source);
    }

    @Mixin(targets = "appeng.me.cluster.implementations.CraftingCPUCluster$TaskProgress", remap = false)
    public interface AccessorTaskProgress {
        @Accessor
        long getValue();

        @Accessor
        void setValue(long value);
    }
}