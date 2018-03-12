package codechicken.translocators;

import codechicken.lib.CodeChickenLib;
import codechicken.lib.internal.ModDescriptionEnhancer;
import codechicken.translocators.handler.ConfigurationHandler;
import codechicken.translocators.proxy.Proxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import static codechicken.lib.CodeChickenLib.MC_VERSION;
import static codechicken.translocators.Translocator.*;

@Mod (modid = MOD_ID, name = MOD_NAME, dependencies = DEPENDENCIES, acceptedMinecraftVersions = CodeChickenLib.MC_VERSION_DEP, certificateFingerprint = "f1850c39b2516232a2108a7bd84d1cb5df93b261", updateJSON = Translocator.UPDATE_URL)
public class Translocator {

    public static final String MOD_ID = "translocators";
    public static final String MOD_NAME = "Translocators";
    public static final String VERSION = "${mod_version}";
    public static final String DEPENDENCIES = CodeChickenLib.MOD_VERSION_DEP + "required-after:forgemultipartcbe";
    static final String UPDATE_URL = "http://chickenbones.net/Files/notification/version.php?query=forge&version=" + MC_VERSION + "&file=Translocators";

    @SidedProxy (clientSide = "codechicken.translocator.proxy.ProxyClient", serverSide = "codechicken.translocator.proxy.Proxy")
    public static Proxy proxy;

    @Mod.Instance (MOD_ID)
    public static Translocator instance;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ConfigurationHandler.init(event.getSuggestedConfigurationFile());
        proxy.preInit();
        ModMetadata metadata = event.getModMetadata();
        metadata.description = modifyDesc(metadata.description);
        ModDescriptionEnhancer.registerEnhancement(MOD_ID, "Translocators");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ConfigurationHandler.loadConfig();
        proxy.init();
    }

    private static String modifyDesc(String desc) {
        desc += "\n";
        desc += "    Credits: MouseCop - Textures\n";
        return desc;
    }
}
