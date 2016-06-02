package codechicken.translocator.proxy;

import codechicken.core.CCUpdateChecker;
import codechicken.core.ModDescriptionEnhancer;
import codechicken.lib.packet.PacketCustom;
import codechicken.lib.render.CCIconRegister;
import codechicken.lib.render.ModelRegistryHelper;
import codechicken.translocator.client.render.tile.TileCraftingGridRenderer;
import codechicken.translocator.client.render.tile.TileTranslocatorRenderer;
import codechicken.translocator.handler.ConfigurationHandler;
import codechicken.translocator.handler.CraftingGridKeyHandler;
import codechicken.translocator.init.ModBlocks;
import codechicken.translocator.network.TranslocatorCPH;
import codechicken.translocator.tile.TileCraftingGrid;
import codechicken.translocator.tile.TileItemTranslocator;
import codechicken.translocator.tile.TileLiquidTranslocator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class ClientProxy extends CommonProxy {
    public void init() {
        if (ConfigurationHandler.clientCheckUpdates) {
            CCUpdateChecker.updateCheck("Translocator");
        }
        ModDescriptionEnhancer.enhanceMod("Translocator");

        super.init();

        ClientRegistry.bindTileEntitySpecialRenderer(TileItemTranslocator.class, new TileTranslocatorRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileLiquidTranslocator.class, new TileTranslocatorRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileCraftingGrid.class, new TileCraftingGridRenderer());

        PacketCustom.assignHandler(TranslocatorCPH.channel, new TranslocatorCPH());

        //MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(blockTranslocator), new ItemTranslocatorRenderer());

        FMLCommonHandler.instance().bus().register(CraftingGridKeyHandler.instance);
        ClientRegistry.registerKeyBinding(CraftingGridKeyHandler.instance);

        CCIconRegister.registerBlockTexture("translocator:craftingGrid");
        ModelRegistryHelper.setParticleTexture(ModBlocks.blockCraftingGrid, new ResourceLocation("translocator", "blocks/craftingGrid"));
    }
}
