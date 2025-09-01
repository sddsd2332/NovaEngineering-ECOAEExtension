package github.kasuminova.ecoaeextension;

import github.kasuminova.ecoaeextension.common.CommonProxy;
import github.kasuminova.ecoaeextension.common.network.*;
import github.kasuminova.ecoaeextension.ecoaeextension.Tags;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(modid = Tags.MOD_ID, useMetadata = true, acceptedMinecraftVersions = "[1.12, 1.13)", version = Tags.VERSION)
@Mod.EventBusSubscriber()
@SuppressWarnings("MethodMayBeStatic")
public class ECOAEExtension {
    public static final String MOD_ID = Tags.MOD_ID;

    public static final String VERSION = Tags.VERSION;

    public static final String CLIENT_PROXY = "github.kasuminova.ecoaeextension.client.ClientProxy";
    public static final String COMMON_PROXY = "github.kasuminova.ecoaeextension.common.CommonProxy";

    public static final SimpleNetworkWrapper NET_CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(MOD_ID);


    @Instance(MOD_ID)
    public static ECOAEExtension instance;
    @SidedProxy(clientSide = CLIENT_PROXY, serverSide = COMMON_PROXY)
    public static CommonProxy proxy = null;
    public static Logger log = LogManager.getLogger(MOD_ID);


    @EventHandler
    public void construction(FMLConstructionEvent event) {
        proxy.construction();
    }

    @SuppressWarnings({"ValueOfIncrementOrDecrementUsed", "UnusedAssignment"})
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        event.getModMetadata().version = VERSION;

        byte start = 0;
        NET_CHANNEL.registerMessage(PktCellDriveStatusUpdate.class, PktCellDriveStatusUpdate.class, start++, Side.CLIENT);
        NET_CHANNEL.registerMessage(PktEStorageGUIData.class, PktEStorageGUIData.class, start++, Side.CLIENT);
        NET_CHANNEL.registerMessage(PktEFabricatorWorkerStatusUpdate.class, PktEFabricatorWorkerStatusUpdate.class, start++, Side.CLIENT);
        NET_CHANNEL.registerMessage(PktEFabricatorGUIData.class, PktEFabricatorGUIData.class, start++, Side.CLIENT);
        NET_CHANNEL.registerMessage(PktEFabricatorPatternSearchGUIUpdate.class, PktEFabricatorPatternSearchGUIUpdate.class, start++, Side.CLIENT);
        NET_CHANNEL.registerMessage(PktECalculatorGUIData.class, PktECalculatorGUIData.class, start++, Side.CLIENT);
        NET_CHANNEL.registerMessage(PktMouseItemUpdate.class, PktMouseItemUpdate.class, start++, Side.CLIENT);

        start = 64;

        NET_CHANNEL.registerMessage(PktPatternTermUploadPattern.class, PktPatternTermUploadPattern.class, start++, Side.SERVER);
        NET_CHANNEL.registerMessage(PktEFabricatorGUIAction.class, PktEFabricatorGUIAction.class, start++, Side.SERVER);
        NET_CHANNEL.registerMessage(PktEFabricatorPatternSearchGUIAction.class, PktEFabricatorPatternSearchGUIAction.class, start++, Side.SERVER);

        CommonProxy.loadModData(event.getModConfigurationDirectory());
        proxy.preInit();

    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        proxy.init();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit();
    }

    @EventHandler
    public void loadComplete(FMLLoadCompleteEvent event) {
        proxy.loadComplete();
    }

}
