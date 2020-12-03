package codechicken.translocators.init;

import codechicken.lib.datagen.ItemModelProvider;
import codechicken.translocators.Translocators;
import net.minecraft.data.*;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import net.minecraftforge.common.Tags;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

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
        }
        gen.addProvider(new ItemTags(gen));
        gen.addProvider(new Recipes(gen));
    }

    private static class ItemModels extends ItemModelProvider {

        public ItemModels(DataGenerator generator, ExistingFileHelper existingFileHelper) {
            super(generator, MOD_ID, existingFileHelper);
        }

        @Override
        protected void registerModels() {
            noTexture(ModContent.itemTranslocatorItem);
            noTexture(ModContent.fluidTranslocatorItem);
            generated(ModContent.diamondNuggetItem);
        }

        @Override
        public String getName() {
            return "Translocators Item models";
        }
    }

    private static class ItemTags extends ItemTagsProvider {

        public ItemTags(DataGenerator gen) {
            super(gen);
        }

        @Override
        protected void registerTags() {
            getBuilder(Translocators.diamondNuggetTag).add(ModContent.diamondNuggetItem);
            getBuilder(Tags.Items.NUGGETS).add(ModContent.diamondNuggetItem);
        }

        @Override
        public String getName() {
            return "Translocators Item tags";
        }
    }

    private static class Recipes extends RecipeProvider {
        public Recipes(DataGenerator gen) { super(gen); }

        @Override
        protected void registerRecipes(Consumer<IFinishedRecipe> consumer) {
            ShapelessRecipeBuilder.shapelessRecipe(Items.DIAMOND)
                    .addIngredient(ModContent.diamondNuggetItem, 9)
                    .addCriterion("has_diamond_nugget", hasItem(ModContent.diamondNuggetItem))
                    .build(consumer, new ResourceLocation(Translocators.MOD_ID, "diamond"));

            ShapelessRecipeBuilder.shapelessRecipe(ModContent.diamondNuggetItem, 9)
                    .addIngredient(Items.DIAMOND)
                    .addCriterion("has_diamond", hasItem(Items.DIAMOND))
                    .build(consumer);

            ShapedRecipeBuilder.shapedRecipe(ModContent.itemTranslocatorItem, 2)
                    .patternLine("RER")
                    .patternLine("IPI")
                    .patternLine("RGR")
                    .key('R', Tags.Items.DUSTS_REDSTONE)
                    .key('E', Tags.Items.ENDER_PEARLS)
                    .key('I', Tags.Items.INGOTS_IRON)
                    .key('P', Items.PISTON)
                    .key('G', Tags.Items.INGOTS_GOLD)
                    .addCriterion("has_redstone", hasItem(Tags.Items.DUSTS_REDSTONE))
                    .addCriterion("has_ender_pearl", hasItem(Tags.Items.ENDER_PEARLS))
                    .addCriterion("has_iron_ingot", hasItem(Tags.Items.INGOTS_IRON))
                    .addCriterion("has_piston", hasItem(Items.PISTON))
                    .addCriterion("has_gold_ingot", hasItem(Tags.Items.INGOTS_GOLD))
                    .build(consumer);

            ShapedRecipeBuilder.shapedRecipe(ModContent.fluidTranslocatorItem, 2)
                    .patternLine("RER")
                    .patternLine("IPI")
                    .patternLine("RLR")
                    .key('R', Tags.Items.DUSTS_REDSTONE)
                    .key('E', Tags.Items.ENDER_PEARLS)
                    .key('I', Tags.Items.INGOTS_IRON)
                    .key('P', Items.PISTON)
                    .key('L', Tags.Items.GEMS_LAPIS)
                    .addCriterion("has_redstone", hasItem(Tags.Items.DUSTS_REDSTONE))
                    .addCriterion("has_ender_pearl", hasItem(Tags.Items.ENDER_PEARLS))
                    .addCriterion("has_iron_ingot", hasItem(Tags.Items.INGOTS_IRON))
                    .addCriterion("has_piston", hasItem(Items.PISTON))
                    .addCriterion("has_lapis", hasItem(Tags.Items.GEMS_LAPIS))
                    .build(consumer);
        }
    }
}
