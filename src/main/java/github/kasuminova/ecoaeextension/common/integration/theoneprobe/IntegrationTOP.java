package github.kasuminova.ecoaeextension.common.integration.theoneprobe;

import mcjty.theoneprobe.TheOneProbe;

public class IntegrationTOP {

    public static void registerProvider() {
        TheOneProbe.theOneProbeImp.registerProvider(EStorageInfoProvider.INSTANCE);
        TheOneProbe.theOneProbeImp.registerProvider(EFabricatorInfoProvider.INSTANCE);
        TheOneProbe.theOneProbeImp.registerProvider(ECalculatorInfoProvider.INSTANCE);
    }

}
