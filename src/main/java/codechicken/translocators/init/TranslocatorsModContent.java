package codechicken.translocators.init;

import codechicken.lib.inventory.container.ICCLContainerType;
import codechicken.lib.util.CrashLock;
import codechicken.lib.util.SneakyUtils;
import codechicken.multipart.api.MultiPartType;
import codechicken.multipart.api.SimpleMultiPartType;
import codechicken.translocators.block.BlockCraftingGrid;
import codechicken.translocators.container.ContainerItemTranslocator;
import codechicken.translocators.item.FluidTranslocatorItem;
import codechicken.translocators.item.ItemTranslocatorItem;
import codechicken.translocators.part.FluidTranslocatorPart;
import codechicken.translocators.part.ItemTranslocatorPart;
import codechicken.translocators.tile.TileCraftingGrid;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import static codechicken.translocators.Translocators.MOD_ID;

/**
 * Created by covers1624 on 4/19/20.
 */
public class TranslocatorsModContent {

    private static final CrashLock LOCK = new CrashLock("Already Initialized.");

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Item.class, MOD_ID);
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Block.class, MOD_ID);
    private static final DeferredRegister<TileEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, MOD_ID);
    private static final DeferredRegister<MultiPartType<?>> PARTS = DeferredRegister.create(SneakyUtils.<Class<MultiPartType<?>>>unsafeCast(MultiPartType.class), MOD_ID);
    private static final DeferredRegister<ContainerType<?>> CONTAINER_TYPES = DeferredRegister.create(SneakyUtils.<Class<ContainerType<?>>>unsafeCast(ContainerType.class), MOD_ID);

    public static Tags.IOptionalNamedTag<Item> diamondNuggetTag = ItemTags.createOptional(new ResourceLocation("forge:nuggets/diamond"));

    //region Items.
    public static RegistryObject<ItemTranslocatorItem> itemTranslocatorItem = ITEMS.register("item_translocator", () ->
            new ItemTranslocatorItem(new Item.Properties().tab(ItemGroup.TAB_REDSTONE)));

    public static RegistryObject<FluidTranslocatorItem> fluidTranslocatorItem = ITEMS.register("fluid_translocator", () ->
            new FluidTranslocatorItem(new Item.Properties().tab(ItemGroup.TAB_REDSTONE)));

    public static RegistryObject<Item> diamondNuggetItem = ITEMS.register("diamond_nugget", () ->
            new Item(new Item.Properties().tab(ItemGroup.TAB_MATERIALS)));
    //endregion

    //region Blocks.
    public static RegistryObject<BlockCraftingGrid> blockCraftingGrid = BLOCKS.register("crafting_grid", BlockCraftingGrid::new);
    //endregion

    //region TileEntityTypes.
    public static RegistryObject<TileEntityType<TileCraftingGrid>> tileCraftingGridType = TILES.register("crafting_grid", () ->
            TileEntityType.Builder.of(TileCraftingGrid::new, blockCraftingGrid.get()).build(null));
    //endregion

    //region MultiPartTypes.
    public static RegistryObject<MultiPartType<ItemTranslocatorPart>> itemTranslocatorPartType = PARTS.register("item_translocator", () ->
            new SimpleMultiPartType<>(s -> new ItemTranslocatorPart()));

    public static RegistryObject<MultiPartType<FluidTranslocatorPart>> fluidTranslocatorPartType = PARTS.register("fluid_translocator", () ->
            new SimpleMultiPartType<>(s -> new FluidTranslocatorPart()));
    //endregion

    //region ContainerTypes.
    public static RegistryObject<ContainerType<ContainerItemTranslocator>> containerItemTranslocator = CONTAINER_TYPES.register("item_translocator", () ->
            ICCLContainerType.create(ContainerItemTranslocator::new));
    //endregion.

    public static void init() {
        LOCK.lock();
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(bus);
        BLOCKS.register(bus);
        TILES.register(bus);
        PARTS.register(bus);
        CONTAINER_TYPES.register(bus);
    }
}
