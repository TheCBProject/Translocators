package codechicken.translocator.init;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

/**
 * Created by covers1624 on 5/17/2016.
 */
public class ModItems {

    public static Item itemDiamondNugget;

    public static void init() {
        itemDiamondNugget = new Item().setUnlocalizedName("translocator:diamondNugget").setCreativeTab(CreativeTabs.MATERIALS);
        GameRegistry.register(itemDiamondNugget.setRegistryName("diamondNugget"));
        OreDictionary.registerOre("nuggetDiamond", itemDiamondNugget);
    }

    @SideOnly(Side.CLIENT)
    public static void initModels() {
        ModelLoader.setCustomModelResourceLocation(itemDiamondNugget, 0, new ModelResourceLocation("translocator:resource", "type=diamond_nugget"));
    }
}
