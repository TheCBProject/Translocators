package codechicken.translocator.init;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

/**
 * Created by covers1624 on 5/17/2016.
 */
public class ModItems {

    public static Item itemDiamondNugget;

    public static void init() {
        itemDiamondNugget = new Item().setUnlocalizedName("translocator:diamondNugget").setCreativeTab(CreativeTabs.tabMaterials);
        GameRegistry.register(itemDiamondNugget.setRegistryName("diamondNugget"));
        OreDictionary.registerOre("nuggetDiamond", itemDiamondNugget);
    }
}
