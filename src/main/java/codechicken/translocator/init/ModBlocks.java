package codechicken.translocator.init;

import codechicken.lib.render.CCIconRegister;
import codechicken.lib.render.ModelRegistryHelper;
import codechicken.translocator.block.BlockCraftingGrid;
import codechicken.translocator.block.BlockTranslocator;
import codechicken.translocator.block.item.ItemTranslocator;
import codechicken.translocator.client.render.TranslocatorItemRender;
import codechicken.translocator.reference.Reference;
import codechicken.translocator.reference.VariantReference;
import codechicken.translocator.tile.TileCraftingGrid;
import codechicken.translocator.tile.TileItemTranslocator;
import codechicken.translocator.tile.TileLiquidTranslocator;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by covers1624 on 5/17/2016.
 */
public class ModBlocks {

    public static BlockTranslocator blockTranslocator;
    public static BlockCraftingGrid blockCraftingGrid;

    public static void init() {
        blockTranslocator = new BlockTranslocator();
        blockTranslocator.setUnlocalizedName("translocator").setCreativeTab(CreativeTabs.REDSTONE);
        GameRegistry.register(blockTranslocator.setRegistryName("translocator"));
        GameRegistry.register(new ItemTranslocator(blockTranslocator).setRegistryName("translocator"));
        GameRegistry.registerTileEntity(TileItemTranslocator.class, "itemTranslocator");
        GameRegistry.registerTileEntity(TileLiquidTranslocator.class, "liquidTranslocator");

        blockCraftingGrid = new BlockCraftingGrid();
        blockCraftingGrid.setUnlocalizedName("craftingGrid");
        GameRegistry.register(blockCraftingGrid.setRegistryName("craftingGrid"));
        GameRegistry.registerTileEntity(TileCraftingGrid.class, "craftingGrid");
    }

    @SideOnly(Side.CLIENT)
    public static void initModels() {

        for (int i = 0; i < VariantReference.translocatorNamesList.size(); i++) {
            String variant = VariantReference.translocatorNamesList.get(i);
            ModelResourceLocation location = new ModelResourceLocation(Reference.MOD_PREFIX + "translocator", "type=" + variant);
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(blockTranslocator), i, location);
        }

        CCIconRegister.registerBlockTexture("translocator:craftingGrid");
        ModelRegistryHelper.setParticleTexture(ModBlocks.blockCraftingGrid, new ResourceLocation("translocator", "blocks/craftingGrid"));

        ModelRegistryHelper.register(new ModelResourceLocation(Reference.MOD_PREFIX + "translocator", "type=item"), new TranslocatorItemRender());
        ModelRegistryHelper.register(new ModelResourceLocation(Reference.MOD_PREFIX + "translocator", "type=liquid"), new TranslocatorItemRender());
    }
}
