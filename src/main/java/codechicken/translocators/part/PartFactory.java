package codechicken.translocators.part;

import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.api.IPartFactory;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Created by covers1624 on 10/11/2017.
 */
public class PartFactory implements IPartFactory {

    public static final PartFactory instance = new PartFactory();
    private static Map<ResourceLocation, Supplier<TMultiPart>> parts = new HashMap<>();

    static {
        parts.put(new ResourceLocation("translocators", "item_translocator"), ItemTranslocatorPart::new);
        parts.put(new ResourceLocation("translocators", "fluid_translocator"), FluidTranslocatorPart::new);
    }

    public static void init() {
        MultiPartRegistry.registerParts(instance, parts.keySet());
    }

    @Override
    public TMultiPart createPart(ResourceLocation identifier, boolean client) {
        if (parts.containsKey(identifier)) {
            return parts.get(identifier).get();
        }
        return null;
    }
}
