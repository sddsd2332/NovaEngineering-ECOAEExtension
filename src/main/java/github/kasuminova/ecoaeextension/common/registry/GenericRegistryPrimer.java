package github.kasuminova.ecoaeextension.common.registry;

import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GenericRegistryPrimer {
    public static final GenericRegistryPrimer INSTANCE = new GenericRegistryPrimer();

    private GenericRegistryPrimer() {
    }

    private final Map<Type, List<IForgeRegistryEntry<?>>> primed = new HashMap<>();

    public <V extends IForgeRegistryEntry<V>> V register(V entry) {
        Class<V> type = entry.getRegistryType();
        List<IForgeRegistryEntry<?>> entries = primed.computeIfAbsent(type, k -> new LinkedList<>());
        entries.add(entry);
        return entry;
    }

    public <T extends IForgeRegistryEntry<T>> List<?> getEntries(Class<T> type) {
        return primed.get(type);
    }

    public void wipe(Type type) {
        primed.remove(type);
    }

    public <T extends IForgeRegistryEntry<T>> void fillRegistry(Class<T> registrySuperType, IForgeRegistry<T> forgeRegistry) {
        List<?> entries = getEntries(registrySuperType);
        if (entries != null) {
            entries.forEach((e) -> forgeRegistry.register((T) e));
        }
    }
}
