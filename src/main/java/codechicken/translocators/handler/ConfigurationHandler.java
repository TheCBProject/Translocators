package codechicken.translocators.handler;

import codechicken.lib.configuration.ConfigFile;
import codechicken.lib.configuration.ConfigFile.ConfigException;
import codechicken.lib.configuration.ConfigTag;
import codechicken.translocators.init.ModItems;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * Created by covers1624 on 5/17/2016.
 */
public class ConfigurationHandler {

    private static final Logger logger = LogManager.getLogger("Translocator");

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
        try {
            config.load();
        } catch (ConfigException ignored) {
            //Old config! Migrate.
            try {
                logger.warn("Found old translocators config, Attempting migration.");
                codechicken.lib.config.ConfigFile oldConfig = new codechicken.lib.config.ConfigFile(cFile);
                config.getTag("disable_crafting_grid").setBoolean(oldConfig.getTag("disableCraftingGrid").getBooleanValue(false));
                String old = oldConfig.getTag("nuggetReplacement").getValue();
                ConfigTag filterTag = config.getTag("filter_item");
                int pipe = old.lastIndexOf("|");
                String meta = old.substring(pipe + 1);
                filterTag.getTag("registry_name").setString(old.substring(0, pipe));
                filterTag.getTag("meta").setInt(meta.equalsIgnoreCase("WILD") ? OreDictionary.WILDCARD_VALUE : Integer.parseInt(meta));
                logger.info("Migration successful!");
            } catch (Throwable t) {
                logger.error("Failed to migrate Translocators config, Resetting to defaults.", t);
            }
        }

        ConfigTag grid = config.getTag("disable_crafting_grid").setComment("Setting this to true will disable the placement of the CraftingGrid.");
        disableCraftingGrid = grid.setDefaultBoolean(false).getBoolean();

        ConfigTag filterItem = config.getTag("filter_item").setComment("Allows controlling what item is used to attach filtering mode.");
        {
            ConfigTag itemTag = filterItem.getTag("registry_name").setDefaultString(ModItems.itemDiamondNugget.getRegistryName().toString());
            ConfigTag metaTag = filterItem.getTag("meta").setComment("Use '32767' for wild card.");
            ResourceLocation name = new ResourceLocation(itemTag.getString());
            int meta = metaTag.setDefaultInt(0).getInt();
            if (!ForgeRegistries.ITEMS.containsKey(name)) {
                logger.error("Unable to locate item {}, Resetting to default.", name);
                name = ModItems.itemDiamondNugget.getRegistryName();
                meta = 0;
                itemTag.setString(name.toString());
                metaTag.setInt(meta);
            }
            nugget = new ItemStack(ForgeRegistries.ITEMS.getValue(name), 1, meta);
        }
        config.save();
    }
}
