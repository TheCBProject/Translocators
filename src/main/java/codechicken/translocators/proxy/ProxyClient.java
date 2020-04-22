package codechicken.translocators.proxy;

import codechicken.lib.texture.SpriteRegistryHelper;
import codechicken.translocators.handler.CraftingGridKeyHandler;
import codechicken.translocators.init.TranslocatorTextures;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ProxyClient extends Proxy {

    public static SpriteRegistryHelper spriteHelper = new SpriteRegistryHelper();

    @Override
    public void commonSetup(FMLCommonSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(CraftingGridKeyHandler.instance);
        ClientRegistry.registerKeyBinding(CraftingGridKeyHandler.instance);
    }

    @Override
    public void clientSetup(FMLClientSetupEvent event) {
        spriteHelper.addIIconRegister(new TranslocatorTextures());
        //ClientRegistry.bindTileEntitySpecialRenderer(TileCraftingGrid.class, new TileCraftingGridRenderer());
    }
}
