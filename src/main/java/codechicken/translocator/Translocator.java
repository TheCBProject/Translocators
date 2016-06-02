package codechicken.translocator;

import codechicken.core.CommonUtils;
import codechicken.core.launch.CodeChickenCorePlugin;
import codechicken.lib.config.ConfigFile;
import codechicken.translocator.handler.ConfigurationHandler;
import codechicken.translocator.proxy.CommonProxy;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;

import static codechicken.translocator.reference.Reference.*;

@Mod(modid = MOD_ID, dependencies = DEPENDENCIES, acceptedMinecraftVersions = CodeChickenCorePlugin.mcVersion)
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
        ConfigurationHandler.init(event.getSuggestedConfigurationFile());
        //config = new ConfigFile(new File(CommonUtils.getMinecraftDir() + "/config", "Translocator.cfg")).setComment("Translocator Configuration File\nDeleting any element will restore it to it's default value\nBlock ID's will be automatically generated the first time it's run");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        //disableCraftingGridKey = config.getTag("disable-crafting-grid-key").setComment("Set to true to disable placement of crafting grids by keyboard shortcut.").getBooleanValue(false);
        proxy.init();
    }
}
