package codechicken.translocator.block.item;

import codechicken.translocator.tile.TileTranslocator;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

public class ItemTranslocator extends ItemBlock {
    public ItemTranslocator(Block block) {
        super(block);
        setHasSubtypes(true);
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block != this.block) {
            if (!world.canBlockBePlaced(this.block, pos, false, side, null, stack)) {
                return false;
            }

            if (!super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState)) {
                return false;
            }
        }

        TileTranslocator ttrans = (TileTranslocator) world.getTileEntity(pos);
        if (ttrans.attachments[side.ordinal() ^ 1] != null) {
            return false;
        }

        ttrans.createAttachment(side.ordinal() ^ 1);
        world.notifyNeighborsOfStateChange(pos, block);
        world.notifyBlockUpdate(pos, state, state, 3);
        return true;
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

        IBlockState iblockstate = worldIn.getBlockState(pos);
        Block block = iblockstate.getBlock();

        if (!block.isReplaceable(worldIn, pos)) {
            pos = pos.offset(facing);
        }

        if (stack.stackSize != 0 && playerIn.canPlayerEdit(pos, facing, stack)) {
            if (placeBlockAt(stack, playerIn, worldIn, pos, facing, hitX, hitY, hitZ, this.block.getStateFromMeta(getMetadata(stack.getMetadata())))) {
                SoundType soundtype = this.block.getSoundType();
                worldIn.playSound(playerIn, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                --stack.stackSize;
            }
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.FAIL;

    }

    @Override
    public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side, EntityPlayer player, ItemStack stack) {
        //BlockCoord pos = new BlockCoord(x, y, z).offset(side);
        IBlockState state = world.getBlockState(pos.offset(side));
        if (state.getBlock() == block && state.getBlock().getMetaFromState(state) != stack.getItemDamage()) {
            return false;
        }

        switch (stack.getItemDamage()) {
        case 0:
            return world.getTileEntity(pos) instanceof IInventory;
        case 1:
            return world.getTileEntity(pos).hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
        }
        return false;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return super.getUnlocalizedName() + "|" + stack.getItemDamage();
    }
}
