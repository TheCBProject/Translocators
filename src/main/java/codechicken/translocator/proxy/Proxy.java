package codechicken.translocator.proxy;

import codechicken.lib.packet.PacketCustom;
import codechicken.translocator.init.ModBlocks;
import codechicken.translocator.init.ModItems;
import codechicken.translocator.init.Recipes;
import codechicken.translocator.network.TranslocatorSPH;

public class Proxy {

    public void preInit() {
        ModBlocks.init();
        ModItems.init();
        Recipes.init();
    }

    public void init() {
        PacketCustom.assignHandler(TranslocatorSPH.channel, new TranslocatorSPH());
    }
}
