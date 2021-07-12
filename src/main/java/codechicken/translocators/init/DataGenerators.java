package codechicken.translocators.init;

import codechicken.lib.datagen.ItemModelProvider;
import codechicken.translocators.Translocators;
import net.minecraft.data.*;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

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
        protected void buildShapelessRecipes(Consumer<IFinishedRecipe> consumer) {
            ShapelessRecipeBuilder.shapeless(Items.DIAMOND)
                    .requires(TranslocatorsModContent.diamondNuggetItem.get(), 9)
                    .unlockedBy("has_diamond_nugget", has(TranslocatorsModContent.diamondNuggetItem.get()))
                    .save(consumer, new ResourceLocation(Translocators.MOD_ID, "diamond"));

            ShapelessRecipeBuilder.shapeless(TranslocatorsModContent.diamondNuggetItem.get(), 9)
                    .requires(Items.DIAMOND)
                    .unlockedBy("has_diamond", has(Items.DIAMOND))
                    .save(consumer);

            ShapedRecipeBuilder.shaped(TranslocatorsModContent.itemTranslocatorItem.get(), 2)
                    .pattern("RER")
                    .pattern("IPI")
                    .pattern("RGR")
                    .define('R', Tags.Items.DUSTS_REDSTONE)
                    .define('E', Tags.Items.ENDER_PEARLS)
                    .define('I', Tags.Items.INGOTS_IRON)
                    .define('P', Items.PISTON)
                    .define('G', Tags.Items.INGOTS_GOLD)
                    .unlockedBy("has_redstone", has(Tags.Items.DUSTS_REDSTONE))
                    .unlockedBy("has_ender_pearl", has(Tags.Items.ENDER_PEARLS))
                    .unlockedBy("has_iron_ingot", has(Tags.Items.INGOTS_IRON))
                    .unlockedBy("has_piston", has(Items.PISTON))
                    .unlockedBy("has_gold_ingot", has(Tags.Items.INGOTS_GOLD))
                    .save(consumer);

            ShapedRecipeBuilder.shaped(TranslocatorsModContent.fluidTranslocatorItem.get(), 2)
                    .pattern("RER")
                    .pattern("IPI")
                    .pattern("RLR")
                    .define('R', Tags.Items.DUSTS_REDSTONE)
                    .define('E', Tags.Items.ENDER_PEARLS)
                    .define('I', Tags.Items.INGOTS_IRON)
                    .define('P', Items.PISTON)
                    .define('L', Tags.Items.GEMS_LAPIS)
                    .unlockedBy("has_redstone", has(Tags.Items.DUSTS_REDSTONE))
                    .unlockedBy("has_ender_pearl", has(Tags.Items.ENDER_PEARLS))
                    .unlockedBy("has_iron_ingot", has(Tags.Items.INGOTS_IRON))
                    .unlockedBy("has_piston", has(Items.PISTON))
                    .unlockedBy("has_lapis", has(Tags.Items.GEMS_LAPIS))
                    .save(consumer);
        }
    }
}
