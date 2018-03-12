package codechicken.translocators.init;

import codechicken.lib.render.CCIconRegister;
import codechicken.translocators.block.BlockCraftingGrid;
import codechicken.translocators.tile.TileCraftingGrid;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by covers1624 on 5/17/2016.
 */
public class ModBlocks {

    public static BlockCraftingGrid blockCraftingGrid;

    public static void init() {
        blockCraftingGrid = new BlockCraftingGrid();
        blockCraftingGrid.setUnlocalizedName("craftingGrid");
        ForgeRegistries.BLOCKS.register(blockCraftingGrid.setRegistryName("craftingGrid"));
        GameRegistry.registerTileEntity(TileCraftingGrid.class, "craftingGrid");
    }

    @SideOnly (Side.CLIENT)
    public static void initModels() {
        CCIconRegister.registerBlockTexture("translocators:crafting_grid");
    }
}
