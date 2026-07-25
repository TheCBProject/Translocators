package codechicken.translocators.init;

import codechicken.lib.datagen.recipe.RecipeProvider;
import codechicken.translocators.client.render.RenderTranslocatorItem;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ItemTagsProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

import static codechicken.translocators.Translocators.MOD_ID;

public class DataGenerators {

    private static final CrashLock LOCK = new CrashLock("Already Initialized.");

    public static void init(IEventBus modBus) {
        LOCK.lock();

        modBus.addListener(DataGenerators::gatherDataGenerators);
    }

    private static void gatherDataGenerators(GatherDataEvent.Client event) {
        event.createProvider(Models::new);
        event.createProvider(BlockTags::new);
        event.createProvider(ItemTags::new);
        event.createProvider(Recipes::new);
    }

    public static class Models extends ModelProvider {

        public Models(PackOutput output) {
            super(output, MOD_ID);
        }

        @Override
        protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
            blockModels.createParticleOnlyBlock(TranslocatorsModContent.blockCraftingGrid.get());

            itemModels.generateFlatItem(TranslocatorsModContent.diamondNuggetItem.get(), ModelTemplates.FLAT_ITEM);
            itemModels.itemModelOutput.accept(
                    TranslocatorsModContent.itemTranslocatorItem.get(),
                    ItemModelUtils.specialModel(
                            Identifier.withDefaultNamespace("block/block"),
                            new RenderTranslocatorItem.Unbaked(0)
                    )
            );
            itemModels.itemModelOutput.accept(
                    TranslocatorsModContent.fluidTranslocatorItem.get(),
                    ItemModelUtils.specialModel(
                            Identifier.withDefaultNamespace("block/block"),
                            new RenderTranslocatorItem.Unbaked(1)
                    )
            );
        }
    }

    private static class BlockTags extends BlockTagsProvider {

        public BlockTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
            super(output, lookupProvider, MOD_ID);
        }

        @Override
        protected void addTags(HolderLookup.Provider pProvider) {
        }
    }

    private static class ItemTags extends ItemTagsProvider {

        public ItemTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
            super(output, lookupProvider, MOD_ID);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            tag(TranslocatorsModContent.diamondNuggetTag).add(TranslocatorsModContent.diamondNuggetItem.get());
            tag(Tags.Items.NUGGETS).add(TranslocatorsModContent.diamondNuggetItem.get());
            tag(TranslocatorsModContent.regulateItemsTag).addTags(TranslocatorsModContent.diamondNuggetTag);
        }
    }

    private static class Recipes extends RecipeProvider {

        public Recipes(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
            super(output, lookupProvider, MOD_ID);
        }

        @Override
        protected void registerRecipes() {
            shapelessRecipe(Items.DIAMOND, 1, Identifier.fromNamespaceAndPath(MOD_ID, "diamond_nugget_combine"))
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
