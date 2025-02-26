package github.kasuminova.ecoaeextension.client.gui.widget.efabricator;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import github.kasuminova.mmce.client.gui.util.*;
import github.kasuminova.mmce.client.gui.widget.base.DynamicWidget;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;
import github.kasuminova.mmce.client.gui.widget.container.Row;
import github.kasuminova.mmce.client.gui.widget.container.ScrollingColumn;
import github.kasuminova.mmce.client.gui.widget.event.GuiEvent;
import github.kasuminova.ecoaeextension.ECOAEExtension;
import github.kasuminova.ecoaeextension.client.gui.GuiEFabricatorPatternSearch;
import github.kasuminova.ecoaeextension.client.gui.widget.SizedColumn;
import github.kasuminova.ecoaeextension.client.gui.widget.efabricator.event.EFPatternSearchContentUpdateEvent;
import github.kasuminova.ecoaeextension.client.gui.widget.efabricator.event.EFPatternSearchGUIUpdateEvent;
import github.kasuminova.ecoaeextension.common.container.data.EFabricatorPatternData;
import github.kasuminova.ecoaeextension.common.network.PktEFabricatorPatternSearchGUIAction;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import mezz.jei.search.GeneralizedSuffixTree;
import mezz.jei.search.ISearchStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public class PatternPanel extends SizedColumn {

    public static final int WIDTH = 229;
    public static final int HEIGHT = 86;

    public static final int INTERNAL_WIDTH = 224;
    public static final int INTERNAL_HEIGHT = 80;

    public static final TextureProperties BACKGROUND = TextureProperties.of(
            GuiEFabricatorPatternSearch.TEXTURES_ELEMENTS,
            1, 25, WIDTH, HEIGHT
    );
    public static final int INTERNAL_OFFSET = 3;

    public static final int PATTERNS_PER_ROW = 12;

    private static final Pattern COLOR_CODE_PATTERN = Pattern.compile("§.");

    private final Map<BlockPos, Int2ObjectMap<PatternSlot>> patterns = new Object2ObjectLinkedOpenHashMap<>();
    private ISearchStorage<PatternSlot> inputSearchStorage = new GeneralizedSuffixTree<>();
    private ISearchStorage<PatternSlot> outputSearchStorage = new GeneralizedSuffixTree<>();

    private String inputSearchContent = "";
    private String outputSearchContent = "";

    private final InternalColumn internal = new InternalColumn();

    public PatternPanel() {
        setWidthHeight(WIDTH, HEIGHT);
        addWidget(internal);
    }

    @Override
    public void initWidget(final WidgetGui gui) {
        super.initWidget(gui);
    }

    @Override
    protected void renderInternal(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
        BACKGROUND.render(renderPos, gui);
        super.renderInternal(gui, renderSize, renderPos, mousePos);
    }

    @Override
    public boolean onGuiEvent(final GuiEvent event) {
        if (event instanceof EFPatternSearchGUIUpdateEvent efGUIUpdateEvent) {
            GuiEFabricatorPatternSearch efGui = efGUIUpdateEvent.getEFGui();
            EFabricatorPatternData data = efGui.getData();
            boolean fullUpdate = efGUIUpdateEvent.isFullUpdate();
            if (fullUpdate) {
                patterns.clear();
            }

            final AtomicBoolean somethingRemoved = new AtomicBoolean(fullUpdate);
            final AtomicBoolean rebuildWidget = new AtomicBoolean(fullUpdate);
            data.patterns().forEach((pos, patternSet) -> {
                if (patternSet.isEmpty()) {
                    return;
                }

                patternSet.forEach(pattern -> {
                    final ItemStack patternStack = pattern.pattern();
                    final int slotID = pattern.slot();
                    if (patternStack.isEmpty()) {
                        PatternSlot removed = patterns.getOrDefault(pos, Int2ObjectMaps.emptyMap()).remove(slotID);
                        if (removed != null) {
                            somethingRemoved.set(true);
                            rebuildWidget.set(true);
                        }
                        return;
                    }

                    final Int2ObjectMap<PatternSlot> slotMap = patterns.computeIfAbsent(pos, key -> new Int2ObjectLinkedOpenHashMap<>());
                    PatternSlot slot = slotMap.get(slotID);
                    if (slot == null) {
                        slot = new PatternSlot(pos, slotID);
                        slotMap.put(slotID, slot);
                        rebuildWidget.set(true);
                    }

                    slot.setStackInSlot(patternStack);
                });
            });

            if (fullUpdate || somethingRemoved.get()) {
                inputSearchStorage = new GeneralizedSuffixTree<>();
                outputSearchStorage = new GeneralizedSuffixTree<>();

                for (Int2ObjectMap<PatternSlot> slotPattern : patterns.values()) {
                    for (final PatternSlot slot : slotPattern.values()) {
                        ICraftingPatternDetails details = slot.getDetails();
                        if (details == null) {
                            continue;
                        }

                        IAEItemStack[] inputs = details.getCondensedInputs();
                        IAEItemStack primaryOutput = details.getPrimaryOutput();

                        for (final IAEItemStack input : inputs) {
                            String displayName = getClearColorName(input);
                            inputSearchStorage.put(displayName.toLowerCase(), slot);
                        }
                        if (primaryOutput != null) {
                            String displayName = getClearColorName(primaryOutput);
                            outputSearchStorage.put(displayName.toLowerCase(), slot);
                        }
                    }
                }
            } else {
                data.patterns().forEach((pos, patternSet) -> {
                    if (patternSet.isEmpty()) {
                        return;
                    }

                    Int2ObjectMap<PatternSlot> slotMap = patterns.get(pos);
                    patternSet.forEach(pattern -> {
                        PatternSlot changed = slotMap.get(pattern.slot());
                        if (changed == null) {
                            return;
                        }
                        ICraftingPatternDetails details = changed.getDetails();
                        if (details == null) {
                            return;
                        }

                        IAEItemStack[] inputs = details.getCondensedInputs();
                        IAEItemStack primaryOutput = details.getPrimaryOutput();

                        for (final IAEItemStack input : inputs) {
                            String displayName = getClearColorName(input);
                            inputSearchStorage.put(displayName.toLowerCase(), changed);
                        }
                        if (primaryOutput != null) {
                            String displayName = getClearColorName(primaryOutput);
                            outputSearchStorage.put(displayName.toLowerCase(), changed);
                        }
                    });
                });
            }

            if (rebuildWidget.get()) {
                if (inputSearchContent.trim().isEmpty() && outputSearchContent.trim().isEmpty()) {
                    rebuildSlots();
                } else {
                    Set<PatternSlot> matched = new ObjectLinkedOpenHashSet<>();
                    inputSearchStorage.getSearchResults(inputSearchContent, matched);
                    outputSearchStorage.getSearchResults(outputSearchContent, matched);
                    rebuildSlots(matched);
                }
            }
        }

        if (event instanceof EFPatternSearchContentUpdateEvent searchUpdateEvent) {
            inputSearchContent = searchUpdateEvent.getInputContent();
            outputSearchContent = searchUpdateEvent.getOutputContent();
            if (inputSearchContent.trim().isEmpty() && outputSearchContent.trim().isEmpty()) {
                rebuildSlots();
                return true;
            }

            Set<PatternSlot> matched = new ObjectLinkedOpenHashSet<>();
            inputSearchStorage.getSearchResults(inputSearchContent, matched);
            outputSearchStorage.getSearchResults(outputSearchContent, matched);
            rebuildSlots(matched);
            return true;
        }

        return super.onGuiEvent(event);
    }

    private void rebuildSlots() {
        internal.getWidgets().clear();

        int patternCount = 0;
        Row row = (Row) new Row().setMarginUp(3);

        for (final Int2ObjectMap<PatternSlot> slotPatterns : patterns.values()) {
            for (final PatternSlot patternSlot : slotPatterns.values()) {
                row.addWidget(patternSlot);
                ++patternCount;
                if (patternCount >= PATTERNS_PER_ROW) {
                    internal.addWidget(row.setUseScissor(false));
                    row = new Row();
                    patternCount = 0;
                }
            }
        }
        internal.addWidget(
                row.setUseScissor(false).setMarginDown(3)
        );
    }

    private void rebuildSlots(final Collection<PatternSlot> slots) {
        internal.getWidgets().clear();
        int patternCount = 0;
        Row row = (Row) new Row().setMarginUp(3);

        for (final PatternSlot slot : slots) {
            row.addWidget(slot);
            ++patternCount;
            if (patternCount >= PATTERNS_PER_ROW) {
                internal.addWidget(row.setUseScissor(false));
                row = new Row();
                patternCount = 0;
            }
        }
        internal.addWidget(
                row.setUseScissor(false).setMarginDown(3)
        );
    }

    private static String getClearColorName(final IAEItemStack input) {
        return COLOR_CODE_PATTERN.matcher(input.getDefinition().getDisplayName()).replaceAll("");
    }

    private static class InternalColumn extends ScrollingColumn {

        private static final int SCISSOR_OFFSET_Y = 2;

        InternalColumn() {
            setWidthHeight(INTERNAL_WIDTH, INTERNAL_HEIGHT);
            setAbsXY(INTERNAL_OFFSET, INTERNAL_OFFSET);
            setUseScissor(false);
        }

        @Override
        public void initWidget(final WidgetGui gui) {
            super.initWidget(gui);
            scrollbar.setMargin(0, 0, 0, 0);
            scrollbar.setWidthHeight(5, 80);
            scrollbar.getScroll()
                    .setMouseDownTexture(233, 1)
                    .setHoveredTexture(233, 1)
                    .setTexture(233, 1)
                    .setUnavailableTexture(233, 1)
                    .setTextureLocation(BACKGROUND.texRes())
                    .setWidthHeight(5, 15);
        }

        @Override
        protected void doRender(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos, final RenderFunction renderFunction) {
            final RenderSize actualSize = renderSize.subtract(new RenderSize(0, SCISSOR_OFFSET_Y * 2));
            setUseScissor(true);
            pushScissor(gui, actualSize, renderPos.add(new RenderPos(0, SCISSOR_OFFSET_Y)), actualSize.width(), actualSize.height());

            int width = renderSize.width() - (scrollbar.getMarginLeft() + scrollbar.getWidth() + scrollbar.getMarginRight());
            int height = renderSize.height();

            int y = getTotalHeight() > height ? -scrollbar.getCurrentScroll() : 0;

            for (final DynamicWidget widget : widgets) {
                if (widget.isDisabled()) {
                    continue;
                }
                RenderPos widgetRenderPos = getWidgetRenderOffset(widget, width, y);
                if (widgetRenderPos == null) {
                    continue;
                }
                int offsetY = widgetRenderPos.posY();
                if (offsetY + widget.getHeight() >= 0) {
                    RenderPos absRenderPos = widgetRenderPos.add(renderPos);
                    renderFunction.doRender(widget, gui, new RenderSize(widget.getWidth(), widget.getHeight()).smaller(renderSize), absRenderPos, mousePos.relativeTo(widgetRenderPos));
                }

                y += widget.getMarginUp() + widget.getHeight() + widget.getMarginDown();
                if (renderSize.isHeightLimited() && y >= renderSize.height()) {
                    break;
                }
            }

            popScissor(actualSize);
            setUseScissor(false);

            if (scrollbar.isDisabled()) {
                return;
            }
            RenderPos scrollbarRenderPos = new RenderPos(
                    width + (scrollbar.getMarginLeft()),
                    scrollbar.getMarginUp());
            renderFunction.doRender(scrollbar, gui,
                    new RenderSize(scrollbar.getWidth(), scrollbar.getHeight()).smaller(renderSize),
                    renderPos.add(scrollbarRenderPos),
                    mousePos.relativeTo(scrollbarRenderPos)
            );
        }

        @Override
        public boolean onMouseClick(final MousePos mousePos, final RenderPos renderPos, final int mouseButton) {
            if (!Minecraft.getMinecraft().player.inventory.getItemStack().isEmpty()) {
                ECOAEExtension.NET_CHANNEL.sendToServer(new PktEFabricatorPatternSearchGUIAction(
                        PktEFabricatorPatternSearchGUIAction.Action.PUT_PATTERN
                ));
                return true;
            }
            return super.onMouseClick(mousePos, renderPos, mouseButton);
        }

        @Override
        public List<String> getHoverTooltips(final WidgetGui widgetGui, final MousePos mousePos) {
            if (!Minecraft.getMinecraft().player.inventory.getItemStack().isEmpty()) {
                return Collections.singletonList(I18n.format("gui.efabricator.pattern_search.put_pattern"));
            }
            return super.getHoverTooltips(widgetGui, mousePos);
        }

    }

}
