package codechicken.translocators.init;

import codechicken.lib.inventory.container.ICCLContainerType;
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
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

import static codechicken.translocators.Translocators.MOD_ID;

/**
 * Created by covers1624 on 4/19/20.
 */
@ObjectHolder (MOD_ID)
@Mod.EventBusSubscriber (modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TranslocatorsModContent {

    @ObjectHolder ("item_translocator")
    public static ItemTranslocatorItem itemTranslocatorItem;
    @ObjectHolder ("fluid_translocator")
    public static FluidTranslocatorItem fluidTranslocatorItem;

    @ObjectHolder ("diamond_nugget")
    public static Item diamondNuggetItem;

    @ObjectHolder ("crafting_grid")
    public static BlockCraftingGrid blockCraftingGrid;

    @ObjectHolder ("crafting_grid")
    public static TileEntityType<TileCraftingGrid> tileCraftingGridType;

    @ObjectHolder ("item_translocator")
    public static MultiPartType<ItemTranslocatorPart> itemTranslocatorPartType;
    @ObjectHolder ("fluid_translocator")
    public static MultiPartType<FluidTranslocatorPart> fluidTranslocatorPartType;

    @ObjectHolder ("item_translocator")
    public static ContainerType<ContainerItemTranslocator> containerItemTranslocator;

    @SubscribeEvent
    public static void onRegisterItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> r = event.getRegistry();
        Item.Properties translocatorProperties = new Item.Properties()//
                .group(ItemGroup.REDSTONE);
        r.register(new ItemTranslocatorItem(translocatorProperties).setRegistryName("item_translocator"));
        r.register(new FluidTranslocatorItem(translocatorProperties).setRegistryName("fluid_translocator"));

        r.register(new Item(new Item.Properties().group(ItemGroup.MATERIALS)).setRegistryName("diamond_nugget"));
    }

    @SubscribeEvent
    public static void onRegisterBlocks(RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> r = event.getRegistry();
        r.register(new BlockCraftingGrid().setRegistryName("crafting_grid"));
    }

    @SubscribeEvent
    public static void onRegisterTiles(RegistryEvent.Register<TileEntityType<?>> event) {
        IForgeRegistry<TileEntityType<?>> r = event.getRegistry();
        r.register(TileEntityType.Builder.create(TileCraftingGrid::new, blockCraftingGrid).build(null).setRegistryName("crafting_grid"));
    }

    @SubscribeEvent
    public static void onRegisterMultiParts(RegistryEvent.Register<MultiPartType<?>> event) {
        IForgeRegistry<MultiPartType<?>> r = event.getRegistry();
        r.register(new SimpleMultiPartType<>(s -> new ItemTranslocatorPart()).setRegistryName("item_translocator"));
        r.register(new SimpleMultiPartType<>(s -> new FluidTranslocatorPart()).setRegistryName("fluid_translocator"));
    }

    @SubscribeEvent
    public static void onRegisterContainers(RegistryEvent.Register<ContainerType<?>> event) {
        IForgeRegistry<ContainerType<?>> r = event.getRegistry();
        r.register(ICCLContainerType.create(ContainerItemTranslocator::new).setRegistryName("item_translocator"));
    }
}
