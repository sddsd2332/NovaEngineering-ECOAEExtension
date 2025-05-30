package github.kasuminova.ecoaeextension.common.container;

import github.kasuminova.ecoaeextension.common.tile.ecotech.efabricator.EFabricatorController;
import hellfirepvp.modularmachinery.common.container.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;

public class ContainerEFabricatorPatternSearch extends ContainerBase<EFabricatorController> {

    public ContainerEFabricatorPatternSearch(final EFabricatorController owner, final EntityPlayer opening) {
        super(owner, opening);
    }

    @Override
    protected void addPlayerSlots(EntityPlayer opening) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlotToContainer(new Slot(opening.inventory, j + i * 9 + 9, 18 + j * 18, (119 + 11) + i * 18));
            }
        }
        for (int i = 0; i < 9; i++) {
            addSlotToContainer(new Slot(opening.inventory, i, 18 + i * 18, 177 + 11));
        }
    }

}
