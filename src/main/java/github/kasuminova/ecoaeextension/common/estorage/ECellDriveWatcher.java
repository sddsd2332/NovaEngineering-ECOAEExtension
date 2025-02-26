package github.kasuminova.ecoaeextension.common.estorage;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.storage.MEInventoryHandler;
import github.kasuminova.ecoaeextension.ECOAEExtension;
import github.kasuminova.ecoaeextension.common.tile.ecotech.estorage.EStorageCellDrive;
import github.kasuminova.ecoaeextension.common.tile.ecotech.estorage.EStorageMEChannel;
import io.netty.util.internal.ThrowableUtil;

import java.util.Collections;
import java.util.List;

public class ECellDriveWatcher<T extends IAEStack<T>> extends MEInventoryHandler<T> {

    protected final EStorageCellDrive drive;

    public ECellDriveWatcher(final IMEInventory<T> i, final IStorageChannel<T> channel, final EStorageCellDrive drive) {
        super(i, channel);
        this.drive = drive;
    }

    @Override
    public T injectItems(final T input, final Actionable type, final IActionSource src) {
        final long size = input.getStackSize();
        final T remainder = super.injectItems(input, type, src);

        if (type == Actionable.MODULATE && (remainder == null || remainder.getStackSize() != size)) {
            EStorageMEChannel channel = this.drive.getController().getChannel();
            AENetworkProxy proxy = channel.getProxy();
            if (proxy.isActive()) {
                try {
                    List<T> changed = Collections.singletonList(input.copy().setStackSize(input.getStackSize() - (remainder == null ? 0 : remainder.getStackSize())));
                    proxy.getStorage().postAlterationOfStoredItems(this.getChannel(), changed, channel.getSource());
                } catch (GridAccessException e) {
                    ECOAEExtension.log.warn(ThrowableUtil.stackTraceToString(e));
                }
            }
            this.drive.onWriting();
        }

        return remainder;
    }

    @Override
    public T extractItems(final T request, final Actionable type, final IActionSource src) {
        final T extractable = super.extractItems(request, type, src);

        if (type == Actionable.MODULATE && extractable != null) {
            EStorageMEChannel channel = this.drive.getController().getChannel();
            AENetworkProxy proxy = channel.getProxy();
            if (proxy.isActive()) {
                try {
                    List<T> changed = Collections.singletonList(request.copy().setStackSize(-extractable.getStackSize()));
                    proxy.getStorage().postAlterationOfStoredItems(this.getChannel(), changed, channel.getSource());
                } catch (GridAccessException e) {
                    ECOAEExtension.log.warn(ThrowableUtil.stackTraceToString(e));
                }
            }
            this.drive.onWriting();
        }

        return extractable;
    }

    @Override
    public boolean isSticky() {
        if (this.getInternal() instanceof ICellInventoryHandler<?> cellInventoryHandler) {
            return cellInventoryHandler.isSticky();
        }

        return super.isSticky();
    }

}
