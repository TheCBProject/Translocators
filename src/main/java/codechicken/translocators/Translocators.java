package codechicken.translocators;

import codechicken.translocators.handler.ConfigHandler;
import codechicken.translocators.init.ClientInit;
import codechicken.translocators.init.TranslocatorsModContent;
import codechicken.translocators.network.TranslocatorNetwork;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.nio.file.Paths;

import static codechicken.translocators.Translocators.MOD_ID;

@Mod (MOD_ID)
public class Translocators {

    public static final String MOD_ID = "translocators";

    public Translocators() {
        FMLJavaModLoadingContext.get().getModEventBus().register(this);
        TranslocatorsModContent.init();
        ConfigHandler.init(Paths.get("./config/translocators.cfg"));
        ConfigHandler.loadConfig();
        TranslocatorNetwork.init();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientInit::init);
    }
}
