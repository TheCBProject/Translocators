package codechicken.translocators.init;

import codechicken.lib.inventory.container.ICCLContainerType;
import codechicken.multipart.api.MultipartType;
import codechicken.multipart.api.SimpleMultipartType;
import codechicken.translocators.block.BlockCraftingGrid;
import codechicken.translocators.container.ContainerItemTranslocator;
import codechicken.translocators.item.FluidTranslocatorItem;
import codechicken.translocators.item.ItemTranslocatorItem;
import codechicken.translocators.part.FluidTranslocatorPart;
import codechicken.translocators.part.ItemTranslocatorPart;
import codechicken.translocators.tile.TileCraftingGrid;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static codechicken.translocators.Translocators.MOD_ID;

/**
 * Created by covers1624 on 4/19/20.
 */
public class TranslocatorsModContent {

    private static final CrashLock LOCK = new CrashLock("Already Initialized.");

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MOD_ID);
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);
    private static final DeferredRegister<MultipartType<?>> PARTS = DeferredRegister.create(MultipartType.MULTIPART_TYPES, MOD_ID);
    private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, MOD_ID);

    public static TagKey<Item> diamondNuggetTag = ItemTags.create(new ResourceLocation("forge:nuggets/diamond"));
    public static TagKey<Item> regulateItemsTag = ItemTags.create(new ResourceLocation("translocators:regulate"));

    //region Items.
    public static DeferredHolder<Item, ItemTranslocatorItem> itemTranslocatorItem = ITEMS.register("item_translocator", () ->
            new ItemTranslocatorItem(new Item.Properties()));

    public static DeferredHolder<Item, FluidTranslocatorItem> fluidTranslocatorItem = ITEMS.register("fluid_translocator", () ->
            new FluidTranslocatorItem(new Item.Properties()));

    public static DeferredHolder<Item, Item> diamondNuggetItem = ITEMS.register("diamond_nugget", () ->
            new Item(new Item.Properties()));
    //endregion

    //region Blocks.
    public static DeferredHolder<Block, BlockCraftingGrid> blockCraftingGrid = BLOCKS.register("crafting_grid", BlockCraftingGrid::new);
    //endregion

    //region TileEntityTypes.
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<TileCraftingGrid>> tileCraftingGridType = TILES.register("crafting_grid", () ->
            BlockEntityType.Builder.of(TileCraftingGrid::new, blockCraftingGrid.get()).build(null));
    //endregion

    //region MultiPartTypes.
    public static DeferredHolder<MultipartType<?>, MultipartType<ItemTranslocatorPart>> itemTranslocatorPartType = PARTS.register("item_translocator", () ->
            new SimpleMultipartType<>(s -> new ItemTranslocatorPart()));

    public static DeferredHolder<MultipartType<?>, MultipartType<FluidTranslocatorPart>> fluidTranslocatorPartType = PARTS.register("fluid_translocator", () ->
            new SimpleMultipartType<>(s -> new FluidTranslocatorPart()));
    //endregion

    //region ContainerTypes.
    public static DeferredHolder<MenuType<?>, MenuType<ContainerItemTranslocator>> containerItemTranslocator = MENU_TYPES.register("item_translocator", () ->
            ICCLContainerType.create(ContainerItemTranslocator::new));
    //endregion.

    public static void init(IEventBus modBus) {
        LOCK.lock();
        ITEMS.register(modBus);
        BLOCKS.register(modBus);
        TILES.register(modBus);
        PARTS.register(modBus);
        MENU_TYPES.register(modBus);
        modBus.addListener(TranslocatorsModContent::onCreativeTabBuild);
    }

    private static void onCreativeTabBuild(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
            event.accept(itemTranslocatorItem.get());
            event.accept(fluidTranslocatorItem.get());
        }
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(diamondNuggetItem.get());
        }
    }
}
