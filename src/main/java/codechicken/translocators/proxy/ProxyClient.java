package codechicken.translocators.proxy;

import codechicken.lib.packet.PacketCustom;
import codechicken.lib.texture.TextureUtils;
import codechicken.translocators.client.render.TileCraftingGridRenderer;
import codechicken.translocators.handler.CraftingGridKeyHandler;
import codechicken.translocators.init.ModBlocks;
import codechicken.translocators.init.ModItems;
import codechicken.translocators.init.TranslocatorTextures;
import codechicken.translocators.network.TranslocatorCPH;
import codechicken.translocators.tile.TileCraftingGrid;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class ProxyClient extends Proxy {

    @Override
    public void preInit() {
        super.preInit();
        ModItems.initModels();
        ModBlocks.initModels();
        TextureUtils.addIconRegister(new TranslocatorTextures());
    }

    public void init() {
        super.init();

        ClientRegistry.bindTileEntitySpecialRenderer(TileCraftingGrid.class, new TileCraftingGridRenderer());

        PacketCustom.assignHandler(TranslocatorCPH.channel, new TranslocatorCPH());

        MinecraftForge.EVENT_BUS.register(CraftingGridKeyHandler.instance);
        ClientRegistry.registerKeyBinding(CraftingGridKeyHandler.instance);
    }
}
