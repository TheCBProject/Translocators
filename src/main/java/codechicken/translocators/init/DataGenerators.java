package codechicken.translocators.init;

import codechicken.lib.datagen.ItemModelProvider;
import codechicken.lib.datagen.recipe.RecipeProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

import static codechicken.translocators.Translocators.MOD_ID;

@Mod.EventBusSubscriber (modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherDataGenerators(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        ExistingFileHelper files = event.getExistingFileHelper();
        if (event.includeClient()) {
            gen.addProvider(new ItemModels(gen, files));
            gen.addProvider(new BlockStates(gen, files));
        }
        gen.addProvider(new ItemTags(gen, new BlockTagsProvider(gen, MOD_ID, files), files));
        gen.addProvider(new Recipes(gen));
    }

    private static class ItemModels extends ItemModelProvider {

        public ItemModels(DataGenerator generator, ExistingFileHelper existingFileHelper) {
            super(generator, MOD_ID, existingFileHelper);
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

        public BlockStates(DataGenerator gen, ExistingFileHelper exFileHelper) {
            super(gen, MOD_ID, exFileHelper);
        }

        @Override
        protected void registerStatesAndModels() {
            ModelFile model = models()
                    .withExistingParent("dummy", "block")
                    .texture("particle", "translocators:blocks/crafting_grid");
            simpleBlock(TranslocatorsModContent.blockCraftingGrid.get(), model);
        }
    }

    private static class ItemTags extends ItemTagsProvider {

        public ItemTags(DataGenerator gen, BlockTagsProvider blockTagProvider, ExistingFileHelper files) {
            super(gen, blockTagProvider, MOD_ID, files);
        }

        @Override
        protected void addTags() {
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

        public Recipes(DataGenerator gen) { super(gen); }

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
