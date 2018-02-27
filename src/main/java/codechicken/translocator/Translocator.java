package codechicken.translocator;

import codechicken.lib.CodeChickenLib;
import codechicken.translocator.handler.ConfigurationHandler;
import codechicken.translocator.proxy.Proxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import static codechicken.lib.CodeChickenLib.MC_VERSION;
import static codechicken.translocator.Translocator.*;

@Mod (modid = MOD_ID, name = MOD_NAME, dependencies = DEPENDENCIES, acceptedMinecraftVersions = CodeChickenLib.MC_VERSION_DEP, certificateFingerprint = "f1850c39b2516232a2108a7bd84d1cb5df93b261", updateJSON = Translocator.UPDATE_URL)
public class Translocator {

    public static final String MOD_ID = "translocator";
    public static final String MOD_NAME = "Translocator";
    public static final String VERSION = "${mod_version}";
    public static final String DEPENDENCIES = CodeChickenLib.MOD_VERSION_DEP + "required-after:forgemultipartcbe";
    static final String UPDATE_URL = "http://chickenbones.net/Files/notification/version.php?query=forge&version=" + MC_VERSION + "&file=Translocator";

    @SidedProxy (clientSide = "codechicken.translocator.proxy.ProxyClient", serverSide = "codechicken.translocator.proxy.Proxy")
    public static Proxy proxy;

    @Mod.Instance (MOD_ID)
    public static Translocator instance;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ConfigurationHandler.init(event.getSuggestedConfigurationFile());
        proxy.preInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ConfigurationHandler.loadConfig();
        proxy.init();
    }
}
