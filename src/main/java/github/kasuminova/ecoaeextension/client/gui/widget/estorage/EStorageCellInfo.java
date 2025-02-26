package github.kasuminova.ecoaeextension.client.gui.widget.estorage;

import github.kasuminova.mmce.client.gui.util.MousePos;
import github.kasuminova.mmce.client.gui.util.RenderPos;
import github.kasuminova.mmce.client.gui.util.RenderSize;
import github.kasuminova.mmce.client.gui.util.TextureProperties;
import github.kasuminova.mmce.client.gui.widget.MultiLineLabel;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;
import github.kasuminova.ecoaeextension.ECOAEExtension;
import github.kasuminova.ecoaeextension.client.gui.widget.SizedColumn;
import github.kasuminova.ecoaeextension.common.block.ecotech.estorage.prop.DriveStorageLevel;
import github.kasuminova.ecoaeextension.common.block.ecotech.estorage.prop.DriveStorageType;
import github.kasuminova.ecoaeextension.common.container.data.EStorageCellData;
import github.kasuminova.ecoaeextension.common.crafttweaker.util.NovaEngUtils;
import github.kasuminova.ecoaeextension.common.tile.ecotech.estorage.EStorageCellDrive;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;

public class EStorageCellInfo extends SizedColumn {

    public static final ResourceLocation BG_TEX_RES = new ResourceLocation(ECOAEExtension.MOD_ID, "textures/gui/estorage_controller_elements.png");

    public static final int CELL_BACKGROUND_TEXTURE_WIDTH = 67;
    public static final int CELL_BACKGROUND_TEXTURE_HEIGHT = 26;

    public static final int CELL_TYPE_BACKGROUND_WIDTH = 8;
    public static final int CELL_TYPE_BACKGROUND_HEIGHT = 26;

    public static final TextureProperties CELL_BACKGROUND_L4 = new TextureProperties(BG_TEX_RES,
            1, 1,
            CELL_BACKGROUND_TEXTURE_WIDTH, CELL_BACKGROUND_TEXTURE_HEIGHT
    );

    public static final TextureProperties CELL_BACKGROUND_L6 = new TextureProperties(BG_TEX_RES,
            1, 28,
            CELL_BACKGROUND_TEXTURE_WIDTH, CELL_BACKGROUND_TEXTURE_HEIGHT
    );

    public static final TextureProperties CELL_BACKGROUND_L9 = new TextureProperties(BG_TEX_RES,
            1, 55,
            CELL_BACKGROUND_TEXTURE_WIDTH, CELL_BACKGROUND_TEXTURE_HEIGHT
    );

    public static final TextureProperties CELL_TYPE_BACKGROUND_ITEM = new TextureProperties(BG_TEX_RES,
            19, 82,
            CELL_TYPE_BACKGROUND_WIDTH,
            CELL_TYPE_BACKGROUND_HEIGHT
    );

    public static final TextureProperties CELL_TYPE_BACKGROUND_FLUID = new TextureProperties(BG_TEX_RES,
            10, 82,
            CELL_TYPE_BACKGROUND_WIDTH, CELL_TYPE_BACKGROUND_HEIGHT
    );

    protected TextureProperties cellBackground = TextureProperties.EMPTY;
    protected TextureProperties cellTypeBackground = TextureProperties.EMPTY;

    protected final EStorageCellData data;

    public EStorageCellInfo(final EStorageCellData data) {
        this.data = data;
        setWidthHeight(67, 26);
        initBackground();
        initInfo();
        setMarginDown(2);
        setUseScissor(false);
    }

    @Override
    protected void preRenderInternal(final WidgetGui widgetGui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
        cellBackground.renderIfPresent(renderPos, widgetGui);
        cellTypeBackground.renderIfPresent(renderPos, widgetGui);
        super.preRenderInternal(widgetGui, renderSize, renderPos, mousePos);
    }

    protected void initBackground() {
        switch (data.level()) {
            case A -> cellBackground = CELL_BACKGROUND_L4;
            case B -> cellBackground = CELL_BACKGROUND_L6;
            case C -> cellBackground = CELL_BACKGROUND_L9;
        }
        switch (data.type()) {
            case ITEM -> cellTypeBackground = CELL_TYPE_BACKGROUND_ITEM;
            case FLUID -> cellTypeBackground = CELL_TYPE_BACKGROUND_FLUID;
        }
    }

    protected void initInfo() {
        DriveStorageType type = data.type();
        DriveStorageLevel level = data.level();
        int usedTypes = data.usedTypes();
        long usedBytes = data.usedBytes();
        int maxTypes = EStorageCellDrive.getMaxTypes(data);
        long maxBytes = EStorageCellDrive.getMaxBytes(data);

        String typeName = I18n.format("gui.estorage_controller.cell_info." + switch (type) {
            case EMPTY -> "unknown";
            case ITEM -> "item";
            case FLUID -> "fluid";
        });
        String levelName = switch (level) {
            case EMPTY -> "unknown";
            case A -> "L4";
            case B -> "L6";
            case C -> "L9";
        };

        getWidgets().clear();

        // Type
        addWidget(new MultiLineLabel(Collections.singletonList(
                I18n.format("gui.estorage_controller.cell_info.tip.0", typeName, levelName))
        ).setAutoWrap(false).setScale(.6F).setHeight(7).setMargin(10, 0, 2, 0));
        // StoredTypes / MaxTypes
        addWidget(new MultiLineLabel(Collections.singletonList(
                I18n.format("gui.estorage_controller.cell_info.tip.1", usedTypes, maxTypes))
        ).setAutoWrap(false).setScale(.6F).setHeight(7).setMargin(10, 0, 0, 0));
        // UsedBytes / MaxBytes
        addWidget(new MultiLineLabel(Collections.singletonList(
                I18n.format("gui.estorage_controller.cell_info.tip.2",
                        NovaEngUtils.formatNumber(usedBytes, 1),
                        NovaEngUtils.formatNumber(maxBytes, 1)
                ))
        ).setAutoWrap(false).setScale(.6F).setHeight(7).setMargin(10, 0, 0, 0));
    }

}
