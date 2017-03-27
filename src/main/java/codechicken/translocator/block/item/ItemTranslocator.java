package codechicken.translocator.block.item;

import codechicken.translocator.tile.TileTranslocator;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
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
            if (!world.mayPlace(this.block, pos, false, side, null)) {
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
        world.notifyNeighborsOfStateChange(pos, block, true);
        world.notifyBlockUpdate(pos, state, state, 3);
        return true;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

        IBlockState iblockstate = worldIn.getBlockState(pos);
        Block block = iblockstate.getBlock();
        ItemStack stack = playerIn.getHeldItem(hand);

        if (!block.isReplaceable(worldIn, pos)) {
            pos = pos.offset(facing);
        }

        if (stack.getCount() != 0 && playerIn.canPlayerEdit(pos, facing, stack)) {
            if (placeBlockAt(stack, playerIn, worldIn, pos, facing, hitX, hitY, hitZ, this.block.getStateFromMeta(getMetadata(stack.getMetadata())))) {
                SoundType soundtype = this.block.getSoundType();
                worldIn.playSound(playerIn, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                stack.shrink(1);
            }
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.FAIL;

    }

    @Override
    public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side, EntityPlayer player, ItemStack stack) {

        IBlockState state = world.getBlockState(pos.offset(side));
        if (state.getBlock() == block && state.getBlock().getMetaFromState(state) != stack.getItemDamage()) {
            return false;
        }
        TileEntity tile = world.getTileEntity(pos);

        switch (stack.getItemDamage()) {
            case 0:
                return tile != null && tile instanceof IInventory;
            case 1:
                return tile != null && tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
        }
        return false;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {

        return super.getUnlocalizedName() + "|" + stack.getItemDamage();
    }
}
