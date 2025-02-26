package github.kasuminova.ecoaeextension.common.network;

import github.kasuminova.ecoaeextension.ECOAEExtension;
import github.kasuminova.ecoaeextension.common.block.ecotech.efabricator.prop.WorkerStatus;
import github.kasuminova.ecoaeextension.common.tile.ecotech.efabricator.EFabricatorWorker;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PktEFabricatorWorkerStatusUpdate implements IMessage, IMessageHandler<PktEFabricatorWorkerStatusUpdate, IMessage> {

    private BlockPos pos = null;
    private WorkerStatus status = null;

    public PktEFabricatorWorkerStatusUpdate() {
    }

    public PktEFabricatorWorkerStatusUpdate(final BlockPos pos, final WorkerStatus status) {
        this.pos = pos;
        this.status = status;
    }

    @Override
    public void fromBytes(final ByteBuf buf) {
        try {
            pos = BlockPos.fromLong(buf.readLong());
            status = WorkerStatus.values()[buf.readByte()];
        } catch (Exception e) {
            ECOAEExtension.log.error("PktEFabricatorStatusUpdate read failed.", e);
        }
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        buf.writeLong(pos.toLong());
        buf.writeByte(status.ordinal());
    }

    @Override
    public IMessage onMessage(final PktEFabricatorWorkerStatusUpdate message, final MessageContext ctx) {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            processPacket(message);
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    protected static void processPacket(final PktEFabricatorWorkerStatusUpdate message) {
        BlockPos pos = message.pos;
        WorkerStatus status = message.status;
        if (pos == null) {
            return;
        }

        WorldClient world = Minecraft.getMinecraft().world;
        if (world == null) {
            return;
        }
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof EFabricatorWorker worker)) {
            return;
        }
        worker.setStatus(status);
        worker.markForUpdate();
    }

}
