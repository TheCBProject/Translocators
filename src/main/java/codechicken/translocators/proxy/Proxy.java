package codechicken.translocators.proxy;

import codechicken.translocators.network.TranslocatorNetwork;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class Proxy {

    public void commonSetup(FMLCommonSetupEvent event) {
        TranslocatorNetwork.init();

    }

    public void clientSetup(FMLClientSetupEvent event) {
    }

    public void preInit() {
        //        ModBlocks.init();
        //        ModItems.init();
    }
}
