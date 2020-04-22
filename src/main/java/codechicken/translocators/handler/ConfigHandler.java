package codechicken.translocators.handler;

import codechicken.lib.configuration.ConfigFile;
import codechicken.lib.configuration.ConfigTag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * Created by covers1624 on 5/17/2016.
 */
public class ConfigHandler {

    private static final Logger logger = LogManager.getLogger("Translocators");

    private static boolean initialized;

    public static ConfigFile config;
    private static File cFile;
    public static boolean disableCraftingGrid;
    public static ItemStack nugget;

    public static void init(File file) {
        cFile = file;
        if (!initialized) {
            config = new ConfigFile(file, false);
            initialized = true;
        }
    }

    public static void loadConfig() {
        config.load();
        ConfigTag grid = config.getTag("disable_crafting_grid").setComment("Setting this to true will disable the placement of the CraftingGrid.");
        disableCraftingGrid = grid.setDefaultBoolean(false).getBoolean();

        ConfigTag filterItem = config.getTag("filter_item").setComment("Allows controlling what item is used to attach filtering mode.");
        {
//            ConfigTag itemTag = filterItem.getTag("registry_name").setDefaultString(ModItems.itemDiamondNugget.getRegistryName().toString());
//            ConfigTag metaTag = filterItem.getTag("meta").setComment("Use '32767' for wild card.");
//            ResourceLocation name = new ResourceLocation(itemTag.getString());
//            int meta = metaTag.setDefaultInt(0).getInt();
//            if (!ForgeRegistries.ITEMS.containsKey(name)) {
//                logger.error("Unable to locate item {}, Resetting to default.", name);
//                name = ModItems.itemDiamondNugget.getRegistryName();
//                meta = 0;
//                itemTag.setString(name.toString());
//                metaTag.setInt(meta);
//            }
//            nugget = new ItemStack(ForgeRegistries.ITEMS.getValue(name), 1, meta);
        }
        config.save();
    }
}
