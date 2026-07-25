package codechicken.translocators.init;

import codechicken.multipart.api.MultipartClientRegistry;
import codechicken.translocators.client.gui.GuiTranslocator;
import codechicken.translocators.client.render.FluidTranslocatorPartRenderer;
import codechicken.translocators.client.render.ItemTranslocatorPartRenderer;
import codechicken.translocators.client.render.RenderTranslocatorItem;
import codechicken.translocators.handler.CraftingGridKeyHandler;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;

import static codechicken.translocators.Translocators.MOD_ID;

public class ClientInit {

    private static final CrashLock LOCK = new CrashLock("Already Initialized.");

    public static void init(IEventBus modBus) {
        LOCK.lock();

        modBus.addListener(ClientInit::clientSetup);
        modBus.addListener(ClientInit::onRegisterMenuScreens);
        modBus.addListener(ClientInit::onRegisterSpecialModelRenderers);
        modBus.addListener(ClientInit::registerRenderers);

        CraftingGridKeyHandler.init(modBus);
        TranslocatorTextures.init(modBus);
    }

    private static void clientSetup(FMLClientSetupEvent event) {
        MultipartClientRegistry.register(TranslocatorsModContent.itemTranslocatorPartType.get(), new ItemTranslocatorPartRenderer());
        MultipartClientRegistry.register(TranslocatorsModContent.fluidTranslocatorPartType.get(), new FluidTranslocatorPartRenderer());
    }

    private static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(TranslocatorsModContent.containerItemTranslocator.get(), GuiTranslocator::new);
    }

    private static void onRegisterSpecialModelRenderers(RegisterSpecialModelRendererEvent event) {
        event.register(Identifier.fromNamespaceAndPath(MOD_ID, "translocator"), RenderTranslocatorItem.Unbaked.MAP_CODEC);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
//        event.registerBlockEntityRenderer(TranslocatorsModContent.tileCraftingGridType.get(), TileCraftingGridRenderer::new);
    }

}
