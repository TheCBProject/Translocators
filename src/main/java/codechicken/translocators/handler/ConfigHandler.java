package codechicken.translocators.handler;

import codechicken.lib.config.ConfigTag;
import codechicken.lib.config.StandardConfigFile;
import codechicken.translocators.init.TranslocatorsModContent;
import net.minecraft.item.Item;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;

import java.nio.file.Path;

/**
 * Created by covers1624 on 5/17/2016.
 */
public class ConfigHandler {

    private static boolean initialized;

    public static ConfigTag config;
    public static boolean disableCraftingGrid;
    public static boolean hideParticlesAndMovingParts;
    public static Tags.IOptionalNamedTag<Item> regulateTag;

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

        hideParticlesAndMovingParts = config.getTag("disable_particles")
                .setComment("Setting this to true will stop the client from rendering the moving parts, liquids and items")
                .setDefaultBoolean(false)
                .getBoolean();

        ConfigTag filterItem = config.getTag("filter_item")
                .setDefaultString(TranslocatorsModContent.regulateItemsTag.getName().toString())
                .setComment("The Tag of Items able to set an ItemTranslocator into Regulate mode.");
        regulateTag = ItemTags.createOptional(new ResourceLocation(filterItem.getString()));
        config.save();
    }
}
