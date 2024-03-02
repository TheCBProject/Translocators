package codechicken.translocators.init;

import codechicken.lib.datagen.ItemModelProvider;
import codechicken.lib.datagen.recipe.RecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.CompletableFuture;

import static codechicken.translocators.Translocators.MOD_ID;

@Mod.EventBusSubscriber (modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherDataGenerators(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        PackOutput output = gen.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ExistingFileHelper files = event.getExistingFileHelper();

        gen.addProvider(event.includeClient(), new ItemModels(output, files));
        gen.addProvider(event.includeClient(), new BlockStates(output, files));
        BlockTags blockTags = new BlockTags(output, lookupProvider, files);
        gen.addProvider(event.includeServer(), new ItemTags(output, lookupProvider, blockTags.contentsGetter(), files));
        gen.addProvider(event.includeServer(), new Recipes(output));
    }

    private static class ItemModels extends ItemModelProvider {

        public ItemModels(PackOutput output, ExistingFileHelper existingFileHelper) {
            super(output, MOD_ID, existingFileHelper);
        }

        @Override
        protected void registerModels() {
            generated(TranslocatorsModContent.itemTranslocatorItem).texture(null);
            generated(TranslocatorsModContent.fluidTranslocatorItem).texture(null);
            generated(TranslocatorsModContent.diamondNuggetItem);
        }

        @Override
        public String getName() {
            return "Translocators Item models";
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
                    .texture("particle", "translocators:blocks/crafting_grid");
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

        @Override
        public String getName() {
            return "Translocators Item tags";
        }
    }

    private static class Recipes extends RecipeProvider {

        public Recipes(PackOutput output) { super(output); }

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

        @Override
        public String getName() {
            return "Translocators Recipes";
        }
    }
}
