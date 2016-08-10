package codechicken.translocator.handler;

import codechicken.lib.config.ConfigFile;

import java.io.File;

/**
 * Created by covers1624 on 5/17/2016.
 */
public class ConfigurationHandler {

    private static boolean initialized;

    public static ConfigFile config;
    public static boolean clientCheckUpdates;
    public static boolean disableCraftingGrid;

    public static void init(File file) {
        if (!initialized) {
            config = new ConfigFile(file).setComment("Translocator Configuration File\n" + "Deleting any element will restore it to it's default value");
            initialized = true;
        }
        loadConfig();
    }

    public static void loadConfig() {
        clientCheckUpdates = config.getTag("clientUpdateCheck").getBooleanValue(true);
        disableCraftingGrid = config.getTag("disableCraftingGrid").setComment("Setting this to true will disable the placement of the CraftingGrid.").getBooleanValue(false);
    }
}
