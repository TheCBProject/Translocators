package codechicken.translocators;

import codechicken.translocators.handler.ConfigHandler;
import codechicken.translocators.proxy.Proxy;
import codechicken.translocators.proxy.ProxyClient;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.io.File;

import static codechicken.translocators.Translocators.MOD_ID;

@Mod (MOD_ID)
public class Translocators {

    public static final String MOD_ID = "translocators";

    public static Proxy proxy;

    public Translocators() {
        proxy = DistExecutor.safeRunForDist(() -> ProxyClient::new, () -> Proxy::new);
        FMLJavaModLoadingContext.get().getModEventBus().register(this);
        ConfigHandler.init(new File("./config/translocators.cfg"));
        ConfigHandler.loadConfig();
    }

    @SubscribeEvent
    public void onCommonSetup(FMLCommonSetupEvent event) {
        proxy.commonSetup(event);
    }

    @SubscribeEvent
    public void onClientSetup(FMLClientSetupEvent event) {
        proxy.clientSetup(event);
    }

    @SubscribeEvent
    public void onServerSetup(FMLDedicatedServerSetupEvent event) {

    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
    }

//    @Mod.EventHandler
//    public void preInit(FMLPreInitializationEvent event) {
//
//        proxy.preInit();
//        //        ModMetadata metadata = event.getModMetadata();
//        //        metadata.description = modifyDesc(metadata.description);
//        //        ModDescriptionEnhancer.registerEnhancement(MOD_ID, MOD_NAME);
//    }
//
//    @Mod.EventHandler
//    public void init(FMLInitializationEvent event) {
//
//        proxy.init();
//    }

    private static String modifyDesc(String desc) {
        desc += "\n";
        desc += "    Credits: MouseCop - Textures\n";
        return desc;
    }
}
