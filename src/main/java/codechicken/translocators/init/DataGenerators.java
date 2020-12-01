package codechicken.translocators.init;

import codechicken.lib.datagen.ItemModelProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.ItemTagsProvider;
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

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
            getBuilder(ModContent.diamondNuggetTag).add(ModContent.diamondNuggetItem);
        }

        @Override
        public String getName() {
            return "Translocators Item tags";
        }
    }
}
