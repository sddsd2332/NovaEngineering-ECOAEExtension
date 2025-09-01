package github.kasuminova.ecoaeextension.mixin.mmce;

import github.kasuminova.ecoaeextension.ECOAEExtension;

import hellfirepvp.modularmachinery.common.data.ModDataHolder;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.commons.io.IOUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

//TODO:改成检查配置文件内是否含有ECOAEExtension文件夹
@Mixin(ModDataHolder.class)
public class MixinModDataHolder {

    @Shadow(remap = false)
    private File machineryDir;

    @Inject(method = "copyDefaultMachinery",at = @At(value = "TAIL"),remap = false)
    public void copyDefaultMachinery(CallbackInfo ci) {
        File eco = new File(machineryDir, "ECOAEExtension");
        if (!eco.exists()) {
            eco.mkdirs();
        }
        novaEngineering_Core$copy("default_machinery", eco);
    }

    @Unique
    private void novaEngineering_Core$copy(String assetDirFrom, File directoryTo) {
        ModContainer thisMod = Loader.instance().getIndexedModList().get(ECOAEExtension.MOD_ID);
        if (thisMod == null) {
            ModContainer active = Loader.instance().activeModContainer();
            if (active != null && active.getModId().equalsIgnoreCase(ECOAEExtension.MOD_ID)) {
                thisMod = active;
            }
        }
        if (thisMod == null) {
            return;
        }
        FileSystem fs = null;
        try {
            File modSource = thisMod.getSource();
            Path root = null;
            if (modSource.isFile()) {
                try {
                    fs = FileSystems.newFileSystem(modSource.toPath(),null);
                    root = fs.getPath("/assets/" + ECOAEExtension.MOD_ID + "/" + assetDirFrom);
                } catch (IOException e) {
                    ECOAEExtension.log.error("Error loading FileSystem from jar: ", e);
                    return;
                }
            } else if (modSource.isDirectory()) {
                root = modSource.toPath().resolve("assets/" + ECOAEExtension.MOD_ID + "/" + assetDirFrom);
            }
            if (root == null || !Files.exists(root)) {
                return;
            }
            Iterator<Path> itr;
            try {
                itr = Files.walk(root).iterator();
            } catch (IOException e) {
                ECOAEExtension.log.error("Error iterating through " + assetDirFrom + " Skipping copying default setup!", e);
                return;
            }
            while (itr.hasNext()) {
                Path filePath = itr.next();
                if (!filePath.getFileName().toString().endsWith(".json")) continue;

                File target = new File(directoryTo, filePath.getFileName().toString());
                try (FileOutputStream fos = new FileOutputStream(target)) {
                    Files.copy(filePath, fos);
                } catch (Exception exc) {
                    ECOAEExtension.log.error("Couldn't copy file from " + filePath);
                }
            }
        } finally {
            IOUtils.closeQuietly(fs);
        }
    }
}
