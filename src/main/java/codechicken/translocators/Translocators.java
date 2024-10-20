package codechicken.translocators;

import codechicken.translocators.handler.ConfigHandler;
import codechicken.translocators.init.ClientInit;
import codechicken.translocators.init.DataGenerators;
import codechicken.translocators.init.TranslocatorsModContent;
import codechicken.translocators.network.TranslocatorNetwork;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Paths;

import static codechicken.translocators.Translocators.MOD_ID;
import static java.util.Objects.requireNonNull;

@Mod (MOD_ID)
public class Translocators {

    public static final String MOD_ID = "translocators";

    private static @Nullable ModContainer container;

    public Translocators(ModContainer container, IEventBus modBus) {
        Translocators.container = container;
        TranslocatorsModContent.init(modBus);
        ConfigHandler.init(Paths.get("./config/translocators.cfg"));
        ConfigHandler.loadConfig();
        TranslocatorNetwork.init(modBus);
        if (FMLEnvironment.dist.isClient()) {
            ClientInit.init(modBus);
        }

        DataGenerators.init(modBus);
    }

    public static ModContainer container() {
        return requireNonNull(container);
    }
}
