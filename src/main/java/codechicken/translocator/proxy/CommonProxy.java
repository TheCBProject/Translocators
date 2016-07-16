package codechicken.translocator.proxy;

import codechicken.lib.packet.PacketCustom;
import codechicken.translocator.init.ModBlocks;
import codechicken.translocator.init.ModItems;
import codechicken.translocator.network.TranslocatorSPH;
import codechicken.translocator.tile.TileCraftingGrid;
import codechicken.translocator.tile.TileItemTranslocator;
import codechicken.translocator.tile.TileLiquidTranslocator;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class CommonProxy {

    public void preInit() {
        ModBlocks.init();
        ModItems.init();
    }

    public void init() {



        //TODO IIndexedCuboidProvider
        //MinecraftForge.EVENT_BUS.register(blockTranslocator);
        //MinecraftForge.EVENT_BUS.register(blockCraftingGrid);

        GameRegistry.registerTileEntity(TileItemTranslocator.class, "itemTranslocator");
        GameRegistry.registerTileEntity(TileLiquidTranslocator.class, "liquidTranslocator");
        GameRegistry.registerTileEntity(TileCraftingGrid.class, "craftingGrid");

        PacketCustom.assignHandler(TranslocatorSPH.channel, new TranslocatorSPH());
    }
}
