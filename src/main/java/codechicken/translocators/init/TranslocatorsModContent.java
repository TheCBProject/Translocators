package codechicken.translocators.init;

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
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import static codechicken.translocators.Translocators.MOD_ID;

/**
 * Created by covers1624 on 4/19/20.
 */
public class TranslocatorsModContent {

    private static final CrashLock LOCK = new CrashLock("Already Initialized.");

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);
    private static final DeferredRegister<MultipartType<?>> PARTS = DeferredRegister.create(MultipartType.MULTIPART_TYPES, MOD_ID);
    private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, MOD_ID);

    public static TagKey<Item> diamondNuggetTag = ItemTags.create(Identifier.fromNamespaceAndPath("c", "nuggets/diamond"));
    public static TagKey<Item> regulateItemsTag = ItemTags.create(Identifier.fromNamespaceAndPath("translocators", "regulate"));

    //region Items.
    public static DeferredItem<ItemTranslocatorItem> itemTranslocatorItem = ITEMS.registerItem("item_translocator", ItemTranslocatorItem::new);

    public static DeferredItem<FluidTranslocatorItem> fluidTranslocatorItem = ITEMS.registerItem("fluid_translocator", FluidTranslocatorItem::new);

    public static DeferredItem<Item> diamondNuggetItem = ITEMS.registerItem("diamond_nugget", Item::new);
    //endregion

    //region Blocks.
    public static DeferredBlock<BlockCraftingGrid> blockCraftingGrid = BLOCKS.registerBlock("crafting_grid", BlockCraftingGrid::new);
    //endregion

    //region TileEntityTypes.
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<TileCraftingGrid>> tileCraftingGridType = TILES.register("crafting_grid", () ->
            new BlockEntityType<>(TileCraftingGrid::new, blockCraftingGrid.get()));
    //endregion

    //region MultiPartTypes.
    public static DeferredHolder<MultipartType<?>, MultipartType<ItemTranslocatorPart>> itemTranslocatorPartType = PARTS.register("item_translocator", () ->
            new SimpleMultipartType<>(s -> new ItemTranslocatorPart()));

    public static DeferredHolder<MultipartType<?>, MultipartType<FluidTranslocatorPart>> fluidTranslocatorPartType = PARTS.register("fluid_translocator", () ->
            new SimpleMultipartType<>(s -> new FluidTranslocatorPart()));
    //endregion

    //region ContainerTypes.
    public static DeferredHolder<MenuType<?>, MenuType<ContainerItemTranslocator>> containerItemTranslocator = MENU_TYPES.register("item_translocator", () ->
            IMenuTypeExtension.create(ContainerItemTranslocator::new));
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
