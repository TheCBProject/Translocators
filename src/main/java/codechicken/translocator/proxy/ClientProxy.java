package codechicken.translocator.proxy;

import codechicken.core.CCUpdateChecker;
import codechicken.core.ModDescriptionEnhancer;
import codechicken.lib.packet.PacketCustom;
import codechicken.translocator.client.render.TileCraftingGridRenderer;
import codechicken.translocator.client.render.TileTranslocatorRenderer;
import codechicken.translocator.handler.ConfigurationHandler;
import codechicken.translocator.handler.CraftingGridKeyHandler;
import codechicken.translocator.init.ModBlocks;
import codechicken.translocator.init.ModItems;
import codechicken.translocator.network.TranslocatorCPH;
import codechicken.translocator.tile.TileCraftingGrid;
import codechicken.translocator.tile.TileItemTranslocator;
import codechicken.translocator.tile.TileLiquidTranslocator;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit() {
        super.preInit();
        ModItems.initModels();
        ModBlocks.initModels();
    }

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

        FMLCommonHandler.instance().bus().register(CraftingGridKeyHandler.instance);
        ClientRegistry.registerKeyBinding(CraftingGridKeyHandler.instance);
    }
}
