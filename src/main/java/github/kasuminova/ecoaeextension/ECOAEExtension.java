package github.kasuminova.ecoaeextension;

import github.kasuminova.ecoaeextension.common.CommonProxy;
import github.kasuminova.ecoaeextension.common.network.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(modid = ECOAEExtension.MOD_ID, name = ECOAEExtension.MOD_NAME, version = ECOAEExtension.VERSION,
        dependencies = "required-after:forge@[14.23.5.2847,);" +
                "required-after:modularmachinery@[2.1.0,);" +
                "required-after:appliedenergistics2@[v0.56.4,);" +
                "required-after:mixinbooter@[8.0,);" +
                "required-after:jei@[4.25.0,);" +
                "required:theoneprobe@[1.12-1.4.28,);" +
                "required:ae2fc@[2.6.3-r,);" +
                "required:lumenized@[1.0.2,);",
        acceptedMinecraftVersions = "[1.12, 1.13)"
)
@SuppressWarnings("MethodMayBeStatic")
public class ECOAEExtension {
    public static final String MOD_ID = "ecoaeextension";
    public static final String MOD_NAME = "Nova Engineering - ECO AE Extension";

    public static final String VERSION = Tags.VERSION;

    public static final String CLIENT_PROXY = "github.kasuminova.ecoaeextension.client.ClientProxy";
    public static final String COMMON_PROXY = "github.kasuminova.ecoaeextension.common.CommonProxy";

    public static final SimpleNetworkWrapper NET_CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(MOD_ID);


    @Mod.Instance(MOD_ID)
    public static ECOAEExtension instance = null;
    @SidedProxy(clientSide = CLIENT_PROXY, serverSide = COMMON_PROXY)
    public static CommonProxy proxy = null;
    public static Logger log = LogManager.getLogger(MOD_ID);


    @Mod.EventHandler
    public void construction(FMLConstructionEvent event) {
        proxy.construction();
    }

    @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
    @Mod.EventHandler
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

        proxy.preInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit();
    }

    @Mod.EventHandler
    public void loadComplete(FMLLoadCompleteEvent event) {
        proxy.loadComplete();
    }



}
