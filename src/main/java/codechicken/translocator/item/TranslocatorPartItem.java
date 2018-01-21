package codechicken.translocator.item;

import codechicken.lib.model.ModelRegistryHelper;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.JItemMultiPart;
import codechicken.multipart.TMultiPart;
import codechicken.translocator.client.render.RenderTranslocatorPartItem;
import codechicken.translocator.part.FluidTranslocatorPart;
import codechicken.translocator.part.ItemTranslocatorPart;
import codechicken.translocator.part.TranslocatorPart;
import net.minecraft.block.SoundType;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;

/**
 * //TODO, This name is cancer. Also the IIR name.
 * Created by covers1624 on 10/11/2017.
 */
public class TranslocatorPartItem extends JItemMultiPart {

    public static final SoundType placement_sound = new SoundType(1.0F, 1.0F, null, null, SoundEvents.BLOCK_STONE_STEP, null, null);

    public TranslocatorPartItem() {
        setHasSubtypes(true);
        setUnlocalizedName("translocators.part");
        setCreativeTab(CreativeTabs.REDSTONE);
        setRegistryName("translocator_part");
    }

    @Override
    public TMultiPart newPart(ItemStack item, EntityPlayer player, World world, BlockPos pos, int side, Vector3 vhit) {
        BlockPos onPos = pos.offset(EnumFacing.VALUES[side ^ 1]);

        TranslocatorPart part;

        TileEntity tile = world.getTileEntity(onPos);
        if (tile != null) {
            if (item.getMetadata() == 0) {
                if (tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.VALUES[side])) {
                    part = new ItemTranslocatorPart();
                } else {
                    return null;
                }
            } else {
                if (tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.VALUES[side])) {
                    part = new FluidTranslocatorPart();
                } else {
                    return null;
                }
            }
        } else {
            return null;
        }

        return part.setupPlacement(player, side);
    }

    @Override
    public SoundType getPlacementSound(ItemStack item) {
        return placement_sound;
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (isInCreativeTab(tab)) {
            items.add(new ItemStack(this, 1, 0));
            items.add(new ItemStack(this, 1, 1));
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        String name = "unknown";
        if (stack.getMetadata() == 0) {
            name = "item";
        } else if (stack.getMetadata() == 1) {
            name = "fluid";
        }
        return super.getUnlocalizedName(stack) + "_" + name;
    }


    @SideOnly (Side.CLIENT)
    public void initModels() {
        ModelResourceLocation loc = new ModelResourceLocation(getRegistryName(), "inventory");
        ModelLoader.setCustomModelResourceLocation(this, 0, loc);
        ModelLoader.setCustomMeshDefinition(this, s -> loc);
        ModelRegistryHelper.register(loc, new RenderTranslocatorPartItem());
    }
}
