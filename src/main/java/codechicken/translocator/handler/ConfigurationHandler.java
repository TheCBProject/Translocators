package codechicken.translocator.handler;

import codechicken.lib.config.ConfigFile;
import codechicken.lib.config.ConfigTag;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.Level;

import java.io.File;

/**
 * Created by covers1624 on 5/17/2016.
 */
public class ConfigurationHandler {

    private static boolean initialized;

    public static ConfigFile config;
    public static boolean clientCheckUpdates;
    public static boolean disableCraftingGrid;
    public static ItemStack nugget;

    public static void init(File file) {
        if (!initialized) {
            config = new ConfigFile(file).setComment("Translocator Configuration File\n" + "Deleting any element will restore it to it's default value");
            initialized = true;
        }
    }

    public static void loadConfig() {
        clientCheckUpdates = config.getTag("clientUpdateCheck").getBooleanValue(true);
        disableCraftingGrid = config.getTag("disableCraftingGrid").setComment("Setting this to true will disable the placement of the CraftingGrid.").getBooleanValue(false);

        ConfigTag tag = config.getTag("nuggetReplacement").setComment("The name of the item used to set the Translocator to filter mode. Diamond Nugget by default. Format <modid>:<registeredItemName>|<meta>, Meta can be replaced with \"WILD\"");
        String name = tag.getValue("translocator:diamondNugget|0");
        Item item;
        int meta;
        try {
            int pipeIndex = name.lastIndexOf("|");
            item = Item.REGISTRY.getObject(new ResourceLocation(name.substring(0, pipeIndex)));
            if (item == null) {
                throw new Exception("Item does not exist!");
            }
            String metaString = name.substring(pipeIndex + 1);
            if (metaString.equalsIgnoreCase("WILD")) {
                meta = OreDictionary.WILDCARD_VALUE;
            } else {
                meta = Integer.parseInt(metaString);
            }
        } catch (Exception e) {
            tag.setValue("translocator:diamondNugget|0");
            FMLLog.log("Translocators", Level.ERROR, e, "Failed to parse Nugget Replacement config entry, It has been reset to default.");
            //LogHelper.error("Failed to parse PersonalItem config entry, It has been reset to default. Reason: %s", e.getMessage());
            item = Items.DIAMOND;
            meta = 0;
        }
        nugget = new ItemStack(item, 1, meta);
    }
}
