package codechicken.translocators.handler;

import codechicken.lib.config.ConfigCategory;
import codechicken.lib.config.ConfigFile;
import codechicken.lib.config.ConfigValue;
import codechicken.translocators.Translocators;
import codechicken.translocators.init.TranslocatorsModContent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.nio.file.Path;

/**
 * Created by covers1624 on 5/17/2016.
 */
public class ConfigHandler {

    private static boolean initialized;

    public static ConfigCategory config;
    public static boolean disableCraftingGrid;
    public static TagKey<Item> regulateTag;

    public static void init(Path file) {
        if (!initialized) {
            config = new ConfigFile(Translocators.MOD_ID)
                    .path(file)
                    .load();
            initialized = true;
        }
    }

    public static void loadConfig() {
        disableCraftingGrid = config.getValue("disable_crafting_grid")
                .setComment("Setting this to true will disable the placement of the CraftingGrid.")
                .setDefaultBoolean(false)
                .getBoolean();

        ConfigValue filterItem = config.getValue("filter_item")
                .setDefaultString(TranslocatorsModContent.regulateItemsTag.location().toString())
                .setComment("The Tag of Items able to set an ItemTranslocator into Regulate mode.");
        regulateTag = ItemTags.create(new ResourceLocation(filterItem.getString()));
        config.save();
    }
}
