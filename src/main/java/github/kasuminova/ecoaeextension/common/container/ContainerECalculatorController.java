package github.kasuminova.ecoaeextension.common.container;

import github.kasuminova.ecoaeextension.common.tile.ecotech.ecalculator.ECalculatorController;
import hellfirepvp.modularmachinery.common.container.ContainerBase;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.player.EntityPlayer;

@Setter
@Getter
public class ContainerECalculatorController extends ContainerBase<ECalculatorController> {

    protected int tickExisted = 0;

    public ContainerECalculatorController(final ECalculatorController owner, final EntityPlayer opening) {
        super(owner, opening);
    }


    @Override
    protected void addPlayerSlots(final EntityPlayer opening) {
        // No player slots
    }

}
