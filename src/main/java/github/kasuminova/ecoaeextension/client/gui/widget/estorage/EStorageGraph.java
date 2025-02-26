package github.kasuminova.ecoaeextension.client.gui.widget.estorage;

import github.kasuminova.mmce.client.gui.util.AnimationValue;
import github.kasuminova.mmce.client.gui.widget.base.WidgetController;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;
import github.kasuminova.mmce.client.gui.widget.container.Row;
import github.kasuminova.mmce.client.gui.widget.event.GuiEvent;
import github.kasuminova.ecoaeextension.client.gui.GuiEStorageController;
import github.kasuminova.ecoaeextension.client.gui.widget.estorage.event.ESGUIDataUpdateEvent;
import github.kasuminova.ecoaeextension.common.block.ecotech.estorage.prop.DriveStorageType;
import github.kasuminova.ecoaeextension.common.container.data.EStorageCellData;
import github.kasuminova.ecoaeextension.common.container.data.EStorageEnergyData;
import github.kasuminova.ecoaeextension.common.crafttweaker.util.NovaEngUtils;
import github.kasuminova.ecoaeextension.common.tile.ecotech.estorage.EStorageCellDrive;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;

import java.util.Collections;

public class EStorageGraph extends Row {
    protected final GuiEStorageController controllerGUI;
    protected final EStorageGraphBar graphBar;

    public EStorageGraph(final GuiEStorageController controllerGUI) {
        this.controllerGUI = controllerGUI;
        addWidget(graphBar = new EStorageGraphBar(controllerGUI));
        WidgetController widgetController = controllerGUI.getWidgetController();
        widgetController.addWidgetContainer(new FluidGraph(this));
        widgetController.addWidgetContainer(new ItemGraph(this));
        widgetController.addWidgetContainer(new TotalGraph(this));
        widgetController.addWidgetContainer(new FluidTypeGraph(this));
        widgetController.addWidgetContainer(new ItemTypeGraph(this));
        widgetController.addWidgetContainer(new EnergyCapacityGraph(this));
        widgetController.addWidgetContainer(new EnergyUsageGraph(this));
    }

    @Override
    public void initWidget(final WidgetGui gui) {
        super.initWidget(gui);
    }

    public GuiEStorageController getControllerGUI() {
        return controllerGUI;
    }

    public EStorageGraphBar getGraphBar() {
        return graphBar;
    }

    public class FluidGraph extends Graph {
        
        public FluidGraph(final EStorageGraph graphParent) {
            super(graphParent, 
                    10, 32,
                    60, 16,
                    2,
                    6, 225,
                    60, 6,
                    true, false);
        }

        @Override
        public void update(final WidgetGui gui) {
            super.update(gui);
            if (!value.isAnimFinished()) {
                label.setContents(Collections.singletonList(
                        I18n.format("gui.estorage_controller.graph.fluid.percent",
                                NovaEngUtils.formatDouble(value.get() * 100, 1)))
                );
            }
        }

        @Override
        public boolean onGuiEvent(final GuiEvent event) {
            if (event instanceof ESGUIDataUpdateEvent) {
                double totalUsedBytes = 0;
                double totalMaxBytes = 0;
                for (final EStorageCellData data : controllerGUI.getCellDataList()) {
                    long maxBytes = EStorageCellDrive.getMaxBytes(data);
                    totalMaxBytes += maxBytes;
                    if (data.type() == DriveStorageType.FLUID) {
                        long usedBytes = data.usedBytes();
                        totalUsedBytes += usedBytes;
                    }
                }
                value.set(totalMaxBytes <= 0 ? 0 : totalUsedBytes / totalMaxBytes);
            }
            return super.onGuiEvent(event);
        }
    }

    public class ItemGraph extends Graph {

        public ItemGraph(final EStorageGraph graphParent) {
            super(graphParent,
                    9, 51,
                    65, 16,
                    2,
                    1, 232,
                    65, 6,
                    true, false);
        }

        @Override
        public void update(final WidgetGui gui) {
            super.update(gui);
            if (!value.isAnimFinished()) {
                label.setContents(Collections.singletonList(
                        I18n.format("gui.estorage_controller.graph.item.percent",
                                NovaEngUtils.formatDouble(value.get() * 100, 1)))
                );
            }
        }

        @Override
        public boolean onGuiEvent(final GuiEvent event) {
            if (event instanceof ESGUIDataUpdateEvent) {
                double totalUsedBytes = 0;
                double totalMaxBytes = 0;
                for (final EStorageCellData data : controllerGUI.getCellDataList()) {
                    long maxBytes = EStorageCellDrive.getMaxBytes(data);
                    totalMaxBytes += maxBytes;
                    if (data.type() == DriveStorageType.ITEM) {
                        long usedBytes = data.usedBytes();
                        totalUsedBytes += usedBytes;
                    }
                }
                value.set(totalMaxBytes <= 0 ? 0 : totalUsedBytes / totalMaxBytes);
            }
            return super.onGuiEvent(event);
        }
    }

    public class TotalGraph extends Graph {

        public TotalGraph(final EStorageGraph graphParent) {
            super(graphParent,
                    8, 70,
                    64, 16,
                    2,
                    2, 239,
                    64, 6,
                    true, false);
            focused = true;
            graphParent.getGraphBar().setPercentage(value, false);
        }

        @Override
        public void update(final WidgetGui gui) {
            super.update(gui);
            if (!value.isAnimFinished()) {
                label.setContents(Collections.singletonList(
                        I18n.format("gui.estorage_controller.graph.total.percent",
                                NovaEngUtils.formatDouble(value.get() * 100, 1)))
                );
            }
        }

        @Override
        public boolean onGuiEvent(final GuiEvent event) {
            if (event instanceof ESGUIDataUpdateEvent) {
                double totalUsedBytes = 0;
                double totalMaxBytes = 0;
                for (final EStorageCellData data : controllerGUI.getCellDataList()) {
                    long maxBytes = EStorageCellDrive.getMaxBytes(data);
                    long usedBytes = data.usedBytes();
                    totalMaxBytes += maxBytes;
                    totalUsedBytes += usedBytes;
                }
                value.set(totalMaxBytes <= 0 ? 0 : totalUsedBytes / totalMaxBytes);
            }
            return super.onGuiEvent(event);
        }
    }

    public class FluidTypeGraph extends Graph {

        protected final AnimationValue totalUsedFluidTypes = AnimationValue.ofFinished(0, 500, .25, .1, .25, 1);
        protected final AnimationValue totalMaxFluidTypes = AnimationValue.ofFinished(0, 500, .25, .1, .25, 1);
        
        public FluidTypeGraph(final EStorageGraph graphParent) {
            super(graphParent,
                    85, 46,
                    60, 16,
                    1,
                    1, 197,
                    60, 6,
                    false, false);
        }

        @Override
        public void update(final WidgetGui gui) {
            super.update(gui);
            if (!value.isAnimFinished()) {
                label.setContents(Collections.singletonList(
                        I18n.format("gui.estorage_controller.graph.fluid_type",
                                NovaEngUtils.formatNumber((long) totalUsedFluidTypes.get(), 1),
                                NovaEngUtils.formatNumber((long) totalMaxFluidTypes.get(), 1)
                        ))
                );
            }
        }

        @Override
        public boolean onGuiEvent(final GuiEvent event) {
            if (event instanceof ESGUIDataUpdateEvent) {
                int totalUsedFluidTypes = 0;
                int totalMaxFluidTypes = 0;
                for (final EStorageCellData data : controllerGUI.getCellDataList()) {
                    if (data.type() == DriveStorageType.FLUID) {
                        totalUsedFluidTypes += data.usedTypes();
                        totalMaxFluidTypes += 25;
                    }
                }
                this.totalUsedFluidTypes.set(totalUsedFluidTypes);
                this.totalMaxFluidTypes.set(totalMaxFluidTypes);
                value.set(totalUsedFluidTypes <= 0 ? 0 : (double) totalUsedFluidTypes / totalMaxFluidTypes);
            }
            return super.onGuiEvent(event);
        }
    }

    public class ItemTypeGraph extends Graph {

        protected final AnimationValue totalUsedItemTypes = AnimationValue.ofFinished(0, 500, .25, .1, .25, 1);
        protected final AnimationValue totalMaxItemTypes = AnimationValue.ofFinished(0, 500, .25, .1, .25, 1);

        public ItemTypeGraph(final EStorageGraph graphParent) {
            super(graphParent,
                    82, 29,
                    59, 16,
                    1,
                    1, 197,
                    59, 6,
                    false, false);
        }

        @Override
        public void update(final WidgetGui gui) {
            super.update(gui);
            if (!value.isAnimFinished()) {
                label.setContents(Collections.singletonList(
                        I18n.format("gui.estorage_controller.graph.item_type",
                                NovaEngUtils.formatNumber((long) totalUsedItemTypes.get(), 1),
                                NovaEngUtils.formatNumber((long) totalMaxItemTypes.get(), 1)
                        ))
                );
            }
        }

        @Override
        public boolean onGuiEvent(final GuiEvent event) {
            if (event instanceof ESGUIDataUpdateEvent) {
                int totalUsedItemTypes = 0;
                int totalMaxItemTypes = 0;
                for (final EStorageCellData data : controllerGUI.getCellDataList()) {
                    if (data.type() == DriveStorageType.ITEM) {
                        totalUsedItemTypes += data.usedTypes();
                        totalMaxItemTypes += 315;
                    }
                }
                this.totalUsedItemTypes.set(totalUsedItemTypes);
                this.totalMaxItemTypes.set(totalMaxItemTypes);
                value.set(totalUsedItemTypes <= 0 ? 0 : (double) totalUsedItemTypes / totalMaxItemTypes);
            }
            return super.onGuiEvent(event);
        }
    }


    public class EnergyUsageGraph extends Graph {

        protected final AnimationValue energyConsumePerTick = AnimationValue.ofFinished(0, 500, .25, .1, .25, 1);

        public EnergyUsageGraph(final EStorageGraph graphParent) {
            super(graphParent,
                    83, 63,
                    61, 16,
                    1,
                    1, 211,
                    61, 6,
                    false, false);
        }

        @Override
        public void update(final WidgetGui gui) {
            super.update(gui);
            if (!value.isAnimFinished()) {
                label.setContents(Collections.singletonList(
                        I18n.format("gui.estorage_controller.graph.energy_usage",
                                NovaEngUtils.formatNumber((long) energyConsumePerTick.get())))
                );
            }
        }

        @Override
        public boolean onGuiEvent(final GuiEvent event) {
            if (event instanceof ESGUIDataUpdateEvent) {
                EStorageEnergyData energyData = controllerGUI.getEnergyData();
                if (energyData != null) {
                    if (energyData.energyStored() > 0) {
                        value.set(energyData.energyConsumePerTick() / energyData.energyStored());
                    } else {
                        value.set(0);
                    }
                    this.energyConsumePerTick.set(energyData.energyConsumePerTick());
                }
            }
            return super.onGuiEvent(event);
        }
    }

    public class EnergyCapacityGraph extends Graph {
        protected final AnimationValue energyStored = AnimationValue.ofFinished(0, 500, .25, .1, .25, 1);
        protected final AnimationValue maxEnergyStore = AnimationValue.ofFinished(0, 500, .25, .1, .25, 1);
        protected boolean shiftDown = false;

        public EnergyCapacityGraph(final EStorageGraph graphParent) {
            super(graphParent,
                    83, 78,
                    60, 16,
                    1,
                    1, 218,
                    60, 6,
                    false, true);
        }

        @Override
        public void update(final WidgetGui gui) {
            super.update(gui);
            if (value.isAnimFinished()
                    && (!shiftDown || Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
                    && ((shiftDown || !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) && !Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))) {
                return;
            }
            shiftDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
            if (shiftDown) {
                label.setContents(Collections.singletonList(
                        I18n.format("gui.estorage_controller.graph.energy_stored",
                                NovaEngUtils.formatNumber((long) energyStored.get()),
                                NovaEngUtils.formatNumber((long) maxEnergyStore.get()))
                ));
            } else {
                label.setContents(Collections.singletonList(
                        I18n.format("gui.estorage_controller.graph.energy_stored.percent",
                                NovaEngUtils.formatDouble(value.get() * 100, 1)))
                );
            }
        }

        @Override
        public boolean onGuiEvent(final GuiEvent event) {
            if (event instanceof ESGUIDataUpdateEvent) {
                EStorageEnergyData energyData = controllerGUI.getEnergyData();
                if (energyData != null && energyData.maxEnergyStore() > 0) {
                    value.set(energyData.energyStored() / energyData.maxEnergyStore());
                    energyStored.set(energyData.energyStored());
                    maxEnergyStore.set(energyData.maxEnergyStore());
                }
            }
            return super.onGuiEvent(event);
        }
    }

}
