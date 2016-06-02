package codechicken.translocator.init;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Created by covers1624 on 5/17/2016.
 */
public class Recipes {

    public static void init() {
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockTranslocator, 2, 0), "rer", "ipi", "rgr", 'r', Items.redstone, 'e', Items.ender_pearl, 'i', Items.iron_ingot, 'g', Items.gold_ingot, 'p', Blocks.piston);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockTranslocator, 2, 1), "rer", "ipi", "rlr", 'r', Items.redstone, 'e', Items.ender_pearl, 'i', Items.iron_ingot, 'l', new ItemStack(Items.dye, 1, 4), 'p', Blocks.piston);
        //TODO JEI Hide this bs. API.hideItem(new ItemStack(blockCraftingGrid));

        GameRegistry.addShapelessRecipe(new ItemStack(Items.diamond), ModItems.itemDiamondNugget, ModItems.itemDiamondNugget, ModItems.itemDiamondNugget, ModItems.itemDiamondNugget, ModItems.itemDiamondNugget, ModItems.itemDiamondNugget, ModItems.itemDiamondNugget, ModItems.itemDiamondNugget, ModItems.itemDiamondNugget);

        GameRegistry.addShapelessRecipe(new ItemStack(ModItems.itemDiamondNugget, 9), Items.diamond);
    }

}
