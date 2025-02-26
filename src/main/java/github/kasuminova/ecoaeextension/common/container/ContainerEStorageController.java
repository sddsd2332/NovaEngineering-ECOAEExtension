package github.kasuminova.ecoaeextension.common.container;

import github.kasuminova.ecoaeextension.common.tile.ecotech.estorage.EStorageController;
import hellfirepvp.modularmachinery.common.container.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;

public class ContainerEStorageController extends ContainerBase<EStorageController> {
    
    protected int tickExisted = 0;

    public ContainerEStorageController(final EStorageController owner, final EntityPlayer opening) {
        super(owner, opening);
    }

    public int getTickExisted() {
        return tickExisted;
    }

    public void setTickExisted(final int tickExisted) {
        this.tickExisted = tickExisted;
    }

    @Override
    protected void addPlayerSlots(EntityPlayer opening) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlotToContainer(new Slot(opening.inventory, j + i * 9 + 9, 8 + j * 18, 125 + i * 18));
            }
        }
        for (int i = 0; i < 9; i++) {
            addSlotToContainer(new Slot(opening.inventory, i, 8 + i * 18, 183));
        }
    }

}
