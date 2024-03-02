package codechicken.translocators.init;

import codechicken.lib.model.ModelRegistryHelper;
import codechicken.multipart.api.MultipartClientRegistry;
import codechicken.translocators.client.gui.GuiTranslocator;
import codechicken.translocators.client.render.FluidTranslocatorPartRenderer;
import codechicken.translocators.client.render.ItemTranslocatorPartRenderer;
import codechicken.translocators.client.render.RenderTranslocatorItem;
import codechicken.translocators.client.render.TileCraftingGridRenderer;
import codechicken.translocators.handler.CraftingGridKeyHandler;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static codechicken.translocators.init.TranslocatorsModContent.fluidTranslocatorItem;
import static codechicken.translocators.init.TranslocatorsModContent.itemTranslocatorItem;

public class ClientInit {

    private static final CrashLock LOCK = new CrashLock("Already Initialized.");
    public static final ModelRegistryHelper modelHelper = new ModelRegistryHelper();

    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(ClientInit::clientSetup);
        bus.addListener(ClientInit::registerRenderers);
        CraftingGridKeyHandler.init(bus);
        TranslocatorTextures.init(bus);
    }

    private static void clientSetup(FMLClientSetupEvent event) {
        MenuScreens.register(TranslocatorsModContent.containerItemTranslocator.get(), GuiTranslocator::new);

        ModelResourceLocation itemTranslocatorInv = new ModelResourceLocation(itemTranslocatorItem.getId(), "inventory");
        ModelResourceLocation fluidTranslocatorInv = new ModelResourceLocation(fluidTranslocatorItem.getId(), "inventory");

        modelHelper.register(itemTranslocatorInv, new RenderTranslocatorItem(0));
        modelHelper.register(fluidTranslocatorInv, new RenderTranslocatorItem(1));

        MultipartClientRegistry.register(TranslocatorsModContent.itemTranslocatorPartType.get(), new ItemTranslocatorPartRenderer());
        MultipartClientRegistry.register(TranslocatorsModContent.fluidTranslocatorPartType.get(), new FluidTranslocatorPartRenderer());
    }


    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(TranslocatorsModContent.tileCraftingGridType.get(), TileCraftingGridRenderer::new);
    }

}
