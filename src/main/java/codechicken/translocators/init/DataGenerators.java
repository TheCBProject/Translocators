package codechicken.translocators.init;

import codechicken.lib.datagen.ItemModelProvider;
import codechicken.lib.datagen.recipe.RecipeProvider;
import codechicken.translocators.client.render.RenderTranslocatorItem;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

import static codechicken.translocators.Translocators.MOD_ID;

public class DataGenerators {

    private static final CrashLock LOCK = new CrashLock("Already Initialized.");

    public static void init(IEventBus modBus) {
        LOCK.lock();

        modBus.addListener(DataGenerators::gatherDataGenerators);
    }

    private static void gatherDataGenerators(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        PackOutput output = gen.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ExistingFileHelper files = event.getExistingFileHelper();

        gen.addProvider(event.includeClient(), new ItemModels(output, files));
        gen.addProvider(event.includeClient(), new BlockStates(output, files));
        BlockTags blockTags = gen.addProvider(event.includeServer(), new BlockTags(output, lookupProvider, files));
        gen.addProvider(event.includeServer(), new ItemTags(output, lookupProvider, blockTags.contentsGetter(), files));
        gen.addProvider(event.includeServer(), new Recipes(lookupProvider, output));
    }

    private static class ItemModels extends ItemModelProvider {

        public ItemModels(PackOutput output, ExistingFileHelper existingFileHelper) {
            super(output, MOD_ID, existingFileHelper);
        }

        @Override
        protected void registerModels() {
            clazz(TranslocatorsModContent.itemTranslocatorItem, RenderTranslocatorItem.Item.class);
            clazz(TranslocatorsModContent.fluidTranslocatorItem, RenderTranslocatorItem.Fluid.class);
            generated(TranslocatorsModContent.diamondNuggetItem);
        }
    }

    private static class BlockStates extends BlockStateProvider {

        public BlockStates(PackOutput output, ExistingFileHelper exFileHelper) {
            super(output, MOD_ID, exFileHelper);
        }

        @Override
        protected void registerStatesAndModels() {
            ModelFile model = models()
                    .withExistingParent("dummy", "block")
                    .texture("particle", "translocators:block/crafting_grid");
            simpleBlock(TranslocatorsModContent.blockCraftingGrid.get(), model);
        }
    }

    private static class BlockTags extends BlockTagsProvider {

        public BlockTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper files) {
            super(output, lookupProvider, MOD_ID, files);
        }

        @Override
        protected void addTags(HolderLookup.Provider pProvider) {
        }
    }

    private static class ItemTags extends ItemTagsProvider {

        public ItemTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTagProvider, ExistingFileHelper files) {
            super(output, lookupProvider, blockTagProvider, MOD_ID, files);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            tag(TranslocatorsModContent.diamondNuggetTag).add(TranslocatorsModContent.diamondNuggetItem.get());
            tag(Tags.Items.NUGGETS).add(TranslocatorsModContent.diamondNuggetItem.get());
            tag(TranslocatorsModContent.regulateItemsTag).addTags(TranslocatorsModContent.diamondNuggetTag);
        }
    }

    private static class Recipes extends RecipeProvider {

        public Recipes(CompletableFuture<HolderLookup.Provider> lookupProvider, PackOutput output) {
            super(lookupProvider, output, MOD_ID);
        }

        @Override
        protected void registerRecipes() {
            shapelessRecipe(Items.DIAMOND)
                    .addIngredient(TranslocatorsModContent.diamondNuggetItem.get(), 9);

            shapelessRecipe(TranslocatorsModContent.diamondNuggetItem.get(), 9)
                    .addIngredient(Items.DIAMOND);

            shapedRecipe(TranslocatorsModContent.itemTranslocatorItem.get(), 2)
                    .patternLine("RER")
                    .patternLine("IPI")
                    .patternLine("RGR")
                    .key('R', Tags.Items.DUSTS_REDSTONE)
                    .key('E', Tags.Items.ENDER_PEARLS)
                    .key('I', Tags.Items.INGOTS_IRON)
                    .key('P', Items.PISTON)
                    .key('G', Tags.Items.INGOTS_GOLD);

            shapedRecipe(TranslocatorsModContent.fluidTranslocatorItem.get(), 2)
                    .patternLine("RER")
                    .patternLine("IPI")
                    .patternLine("RLR")
                    .key('R', Tags.Items.DUSTS_REDSTONE)
                    .key('E', Tags.Items.ENDER_PEARLS)
                    .key('I', Tags.Items.INGOTS_IRON)
                    .key('P', Items.PISTON)
                    .key('L', Tags.Items.GEMS_LAPIS);
        }
    }
}
