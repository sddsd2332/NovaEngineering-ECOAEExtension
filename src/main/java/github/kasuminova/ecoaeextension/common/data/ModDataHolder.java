package github.kasuminova.ecoaeextension.common.data;

import github.kasuminova.ecoaeextension.ECOAEExtension;
import hellfirepvp.modularmachinery.ModularMachinery;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

public class ModDataHolder {

    private File ecomachineryDir;
    private boolean requiresDefaultMachinery;

    public void setup(File configDir) {
        File mainDir = new File(configDir, ModularMachinery.MODID);
        if (!mainDir.exists()) {
            mainDir.mkdirs();
        }

        File machineryDir = new File(mainDir, "machinery");
        if (!machineryDir.exists()) {
            machineryDir.mkdirs();
        }

        ecomachineryDir = new File(machineryDir, "ECOAEExtension");
        if (!ecomachineryDir.exists()) {
            requiresDefaultMachinery = true;
            ecomachineryDir.mkdirs();
        }

    }

    public boolean requiresDefaultMachinery() {
        boolean old = requiresDefaultMachinery;
        requiresDefaultMachinery = false;
        return old;
    }

    public void copyDefaultMachinery() {
        copy("default_machinery", ecomachineryDir);
    }

    private void copy(String assetDirFrom, File directoryTo) {
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
                    fs = FileSystems.newFileSystem(modSource.toPath(), null);
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
