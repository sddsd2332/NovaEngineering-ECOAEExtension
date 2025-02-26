package github.kasuminova.ecoaeextension.common.network;

import github.kasuminova.ecoaeextension.client.gui.GuiEFabricatorController;
import github.kasuminova.ecoaeextension.common.container.data.EFabricatorData;
import github.kasuminova.ecoaeextension.common.tile.ecotech.efabricator.EFabricatorController;
import github.kasuminova.ecoaeextension.common.tile.ecotech.efabricator.EFabricatorWorker;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.stream.Collectors;

public class PktEFabricatorGUIData implements IMessage, IMessageHandler<PktEFabricatorGUIData, IMessage> {

    private EFabricatorData data = null;

    public PktEFabricatorGUIData(final EFabricatorController controller) {
        data = new EFabricatorData(
                controller.getLength(),
                controller.isOverclocked(),
                controller.isActiveCooling(),
                controller.getParallelism(),
                controller.getCoolantInputFluids(),
                controller.getCoolantInputCap(),
                controller.getCoolantOutputFluids(),
                controller.getCoolantOutputCap(),
                controller.getEnergyStored(),
                controller.getTotalCrafted(),
                controller.getLevel(),
                controller.getWorkers().stream()
                        .map(EFabricatorWorker::getQueue)
                        .map(queue -> {
                            EFabricatorWorker.CraftWork peek = queue.peek();
                            return new EFabricatorData.WorkerStatus(peek != null ? peek.getOutput() : ItemStack.EMPTY, queue.size());
                        })
                        .collect(Collectors.toList())
        );
    }

    public PktEFabricatorGUIData() {
    }

    @Override
    public void fromBytes(final ByteBuf buf) {
        data = EFabricatorData.read(buf);
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        data.write(buf);
    }

    @Override
    public IMessage onMessage(final PktEFabricatorGUIData message, final MessageContext ctx) {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            Minecraft.getMinecraft().addScheduledTask(() -> processPacket(message));
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    protected static void processPacket(final PktEFabricatorGUIData message) {
        EFabricatorData data = message.data;
        GuiScreen cur = Minecraft.getMinecraft().currentScreen;
        if (!(cur instanceof GuiEFabricatorController efGUI)) {
            return;
        }
        if (data == null) {
            return;
        }
        efGUI.onDataUpdate(data);
    }

}
