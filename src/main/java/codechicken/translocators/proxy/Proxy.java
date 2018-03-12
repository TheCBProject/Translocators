package codechicken.translocators.proxy;

import codechicken.lib.packet.PacketCustom;
import codechicken.translocators.init.ModBlocks;
import codechicken.translocators.init.ModItems;
import codechicken.translocators.init.Recipes;
import codechicken.translocators.network.TranslocatorSPH;
import codechicken.translocators.part.PartFactory;

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
