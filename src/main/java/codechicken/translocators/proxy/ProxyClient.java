package codechicken.translocators.proxy;

import codechicken.lib.model.ModelRegistryHelper;
import codechicken.lib.texture.SpriteRegistryHelper;
import codechicken.translocators.client.render.RenderTranslocatorItem;
import codechicken.translocators.handler.CraftingGridKeyHandler;
import codechicken.translocators.init.TranslocatorTextures;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import static codechicken.translocators.init.ModContent.fluidTranslocatorItem;
import static codechicken.translocators.init.ModContent.itemTranslocatorItem;

public class ProxyClient extends Proxy {

    public static SpriteRegistryHelper spriteHelper = new SpriteRegistryHelper();
    public static ModelRegistryHelper modelHelper = new ModelRegistryHelper();

    static {
        spriteHelper.addIIconRegister(new TranslocatorTextures());
    }

    @Override
    public void commonSetup(FMLCommonSetupEvent event) {
    }

    @Override
    public void clientSetup(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(CraftingGridKeyHandler.instance);
        ClientRegistry.registerKeyBinding(CraftingGridKeyHandler.instance);

        ModelResourceLocation itemTranslocatorInv = new ModelResourceLocation(itemTranslocatorItem.getRegistryName(), "inventory");
        ModelResourceLocation fluidTranslocatorInv = new ModelResourceLocation(fluidTranslocatorItem.getRegistryName(), "inventory");

        modelHelper.register(itemTranslocatorInv, new RenderTranslocatorItem(0));
        modelHelper.register(fluidTranslocatorInv, new RenderTranslocatorItem(1));

        //ClientRegistry.bindTileEntitySpecialRenderer(TileCraftingGrid.class, new TileCraftingGridRenderer());
    }
}
