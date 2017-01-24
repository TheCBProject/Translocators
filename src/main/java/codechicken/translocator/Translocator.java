package codechicken.translocator;

import codechicken.lib.CodeChickenLib;
import codechicken.translocator.handler.ConfigurationHandler;
import codechicken.translocator.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import static codechicken.translocator.reference.Reference.*;

@Mod(modid = MOD_ID, dependencies = DEPENDENCIES, acceptedMinecraftVersions = CodeChickenLib.mcVersion, certificateFingerprint = "f1850c39b2516232a2108a7bd84d1cb5df93b261")
public class Translocator {
    @SidedProxy(clientSide = CLIENT_PROXY, serverSide = COMMON_PROXY)
    public static CommonProxy proxy;

    @Mod.Instance(MOD_ID)
    public static Translocator instance;

    //public static ConfigFile config;

    //public static boolean disableCraftingGridKey;

    public Translocator() {
        instance = this;
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        FingerprintChecker.runFingerprintChecks();
        ConfigurationHandler.init(event.getSuggestedConfigurationFile());
        //config = new ConfigFile(new File(CommonUtils.getMinecraftDir() + "/config", "Translocator.cfg")).setComment("Translocator Configuration File\nDeleting any element will restore it to it's default value\nBlock ID's will be automatically generated the first time it's run");
        proxy.preInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ConfigurationHandler.loadConfig();
        //disableCraftingGridKey = config.getTag("disable-crafting-grid-key").setComment("Set to true to disable placement of crafting grids by keyboard shortcut.").getBooleanValue(false);
        proxy.init();
    }
}
