package codechicken.translocator.init;

import codechicken.translocator.block.BlockCraftingGrid;
import codechicken.translocator.block.BlockTranslocator;
import codechicken.translocator.block.item.ItemTranslocator;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Created by covers1624 on 5/17/2016.
 */
public class ModBlocks {

    public static BlockTranslocator blockTranslocator;
    public static BlockCraftingGrid blockCraftingGrid;

    public static void init(){
        blockTranslocator = new BlockTranslocator();
        blockTranslocator.setUnlocalizedName("translocator").setCreativeTab(CreativeTabs.tabRedstone);
        blockCraftingGrid = new BlockCraftingGrid();
        blockCraftingGrid.setUnlocalizedName("craftingGrid");



        GameRegistry.register(blockTranslocator.setRegistryName("translocator"));
        GameRegistry.register(new ItemTranslocator(blockTranslocator).setRegistryName("translocator"));
        GameRegistry.register(blockCraftingGrid.setRegistryName("craftingGrid"));
    }

}
