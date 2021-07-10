package codechicken.translocators.handler;

import codechicken.lib.config.ConfigTag;
import codechicken.lib.config.StandardConfigFile;
import codechicken.translocators.init.TranslocatorsModContent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.function.Supplier;

/**
 * Created by covers1624 on 5/17/2016.
 */
public class ConfigHandler {

    private static final Logger logger = LogManager.getLogger("Translocators");

    private static boolean initialized;

    public static ConfigTag config;
    public static boolean disableCraftingGrid;
    private static ItemStack nugget;
    private static Runnable nuggetFactory;

    public static void init(Path file) {
        if (!initialized) {
            config = new StandardConfigFile(file).load();
            initialized = true;
        }
    }

    public static void loadConfig() {
        disableCraftingGrid = config.getTag("disable_crafting_grid")
                .setComment("Setting this to true will disable the placement of the CraftingGrid.")
                .setDefaultBoolean(false)
                .getBoolean();

        ConfigTag filterItem = config.getTag("filter_item")
                .setDefaultString("translocators:diamond_nugget")
                .setComment("Allows controlling what item is used to attach filtering mode. This should be the Registry name of the item.");
        ResourceLocation itemName = new ResourceLocation(filterItem.getString());
        nuggetFactory = () -> {
            if (ForgeRegistries.ITEMS.containsKey(itemName)) {
                nugget = new ItemStack(ForgeRegistries.ITEMS.getValue(itemName));
            } else {
                logger.warn("Failed to load Nugget item '{}', does not exist. Using default.", itemName);
                filterItem.resetToDefault().save();
                nugget = new ItemStack(TranslocatorsModContent.diamondNuggetItem.get());
            }
        };
        config.save();
    }

    public static ItemStack getNugget() {
        if (nugget == null) {
            nuggetFactory.run();
        }
        return nugget;
    }
}
