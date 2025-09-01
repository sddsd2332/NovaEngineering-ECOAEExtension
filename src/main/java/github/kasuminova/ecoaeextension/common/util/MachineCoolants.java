package github.kasuminova.ecoaeextension.common.util;

import com.github.bsideup.jabel.Desugar;
import it.unimi.dsi.fastutil.objects.Reference2ObjectLinkedOpenHashMap;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

public class MachineCoolants {

    public static final MachineCoolants INSTANCE = new MachineCoolants();

    private final Map<Fluid, Coolant> coolants = new Reference2ObjectLinkedOpenHashMap<>();

    public void init() {
        Fluid water = FluidRegistry.getFluid("water");
        Fluid steam = FluidRegistry.getFluid("steam");
        if (steam != null) {
            coolants.put(water, new Coolant(new FluidStack(water, 1), new FluidStack(steam, 1), 15));
        }

        Fluid ice = FluidRegistry.getFluid("ice");
        if (ice != null) {
            coolants.put(ice, new Coolant(new FluidStack(ice, 1), new FluidStack(water, 1), 30));
        }

        Fluid ic2Coolant = FluidRegistry.getFluid("ic2coolant");
        Fluid ic2HotCoolant = FluidRegistry.getFluid("ic2hot_coolant");
        if (ic2Coolant != null && ic2HotCoolant != null) {
            coolants.put(ic2Coolant, new Coolant(new FluidStack(ic2Coolant, 1), new FluidStack(ic2HotCoolant, 1), 90));
        }

        Fluid liquidHelium = FluidRegistry.getFluid("liquid_helium");
        Fluid helium = FluidRegistry.getFluid("helium");
        if (liquidHelium != null) {
            coolants.put(liquidHelium, new Coolant(new FluidStack(liquidHelium, 1), new FluidStack(helium, 320), 170));
        }

        Fluid liquidNitrogen = FluidRegistry.getFluid("liquid_nitrogen");
        Fluid nitrogen = FluidRegistry.getFluid("nitrogen");
        if (liquidNitrogen != null) {
            coolants.put(liquidNitrogen, new Coolant(new FluidStack(liquidNitrogen, 1), new FluidStack(nitrogen, 320), 190));
        }

        Fluid cryotheum = FluidRegistry.getFluid("cryotheum");
        if (cryotheum != null) {
            coolants.put(cryotheum, new Coolant(new FluidStack(cryotheum, 1), null, 240));
        }
    }

    public Collection<Coolant> getCoolants() {
        return coolants.values();
    }

    public Coolant getCoolant(Fluid fluid) {
        return coolants.get(fluid);
    }

    @Desugar
    public record Coolant(FluidStack input, @Nullable FluidStack output, int coolantUnit) {

        public int maxCanConsume(final IFluidHandler inputHandler, final IFluidHandler outputHandler) {
            FluidStack drained = inputHandler.drain(new FluidStack(this.input.getFluid(), Integer.MAX_VALUE), false);
            if (drained == null) {
                return 0;
            }

            int filled = Integer.MAX_VALUE;
            if (output != null) {
                filled = outputHandler.fill(new FluidStack(this.output.getFluid(), drained.amount), false);
            }

            int inputMul = drained.amount / input.amount;
            if (output == null) {
                return inputMul;
            }

            int outputMul = filled / output.amount;
            return Math.min(inputMul, outputMul);
        }

        public int maxCanConsume(final int inputStored, final int outputCap, final FluidStack outputStack) {
            int inputMul = inputStored / input.amount;
            if (output == null) {
                return inputMul;
            }

            if (outputStack == null) {
                int outputMul = outputCap / output.amount;
                return Math.min(inputMul, outputMul);
            }

            if (outputStack.getFluid() != output.getFluid()) {
                return 0;
            }
            return Math.min(inputMul, (outputCap - outputStack.amount) / output.amount);
        }

    }

}
