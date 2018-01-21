package codechicken.translocator.proxy;

import codechicken.lib.packet.PacketCustom;
import codechicken.multipart.MultiPartRegistry;
import codechicken.translocator.init.ModBlocks;
import codechicken.translocator.init.ModItems;
import codechicken.translocator.init.Recipes;
import codechicken.translocator.network.TranslocatorSPH;
import codechicken.translocator.part.PartFactory;

public class Proxy {

    public void preInit() {
        ModBlocks.init();
        ModItems.init();
        Recipes.init();
        PartFactory.init();
    }

    public void init() {
        PacketCustom.assignHandler(TranslocatorSPH.channel, new TranslocatorSPH());
    }
}
