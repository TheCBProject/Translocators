package codechicken.translocator.init;

import codechicken.translocator.item.TranslocatorPartItem;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

/**
 * Created by covers1624 on 5/17/2016.
 */
public class ModItems {

    public static Item itemDiamondNugget;
    public static TranslocatorPartItem translocatorPart;

    public static void init() {
        itemDiamondNugget = new Item().setUnlocalizedName("translocator:diamondNugget").setCreativeTab(CreativeTabs.MATERIALS);
        translocatorPart = new TranslocatorPartItem();
        ForgeRegistries.ITEMS.register(itemDiamondNugget.setRegistryName("diamondNugget"));
        ForgeRegistries.ITEMS.register(translocatorPart);
        OreDictionary.registerOre("nuggetDiamond", itemDiamondNugget);
    }

    @SideOnly(Side.CLIENT)
    public static void initModels() {
        ModelLoader.setCustomModelResourceLocation(itemDiamondNugget, 0, new ModelResourceLocation("translocator:resource", "type=diamond_nugget"));
        translocatorPart.initModels();
    }
}
