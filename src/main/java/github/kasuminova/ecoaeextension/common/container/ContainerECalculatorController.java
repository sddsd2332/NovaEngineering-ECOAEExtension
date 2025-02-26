package github.kasuminova.ecoaeextension.common.container;

import github.kasuminova.ecoaeextension.common.tile.ecotech.ecalculator.ECalculatorController;
import hellfirepvp.modularmachinery.common.container.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;

public class ContainerECalculatorController extends ContainerBase<ECalculatorController> {

    protected int tickExisted = 0;

    public ContainerECalculatorController(final ECalculatorController owner, final EntityPlayer opening) {
        super(owner, opening);
    }

    public int getTickExisted() {
        return tickExisted;
    }

    public void setTickExisted(final int tickExisted) {
        this.tickExisted = tickExisted;
    }

    @Override
    protected void addPlayerSlots(final EntityPlayer opening) {
        // No player slots
    }

}
