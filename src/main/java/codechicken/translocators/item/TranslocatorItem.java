package codechicken.translocators.item;

import codechicken.lib.vec.Vector3;
import codechicken.multipart.JItemMultiPart;
import codechicken.multipart.api.MultiPartType;
import codechicken.multipart.api.part.TMultiPart;
import codechicken.translocators.part.TranslocatorPart;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

/**
 * //TODO, This name is cancer. Also the IIR name.
 * Created by covers1624 on 10/11/2017.
 */
public abstract class TranslocatorItem<T extends TranslocatorPart> extends JItemMultiPart {

    public TranslocatorItem(Item.Properties props) {
        super(props);
    }

    public abstract MultiPartType<T> getType();

    public abstract Capability<?> getTargetCapability();

    @Override
    public TMultiPart newPart(ItemStack item, PlayerEntity player, World world, BlockPos pos, int side, Vector3 vhit) {
        BlockPos onPos = pos.offset(Direction.BY_INDEX[side ^ 1]);
        Direction face = Direction.BY_INDEX[side];
        TileEntity tile = world.getTileEntity(onPos);
        if (tile != null && tile.getCapability(getTargetCapability(), face).isPresent()) {
            TranslocatorPart part = world.isRemote ? getType().createPartClient(null) : getType().createPartServer(null);
            return part.setupPlacement(player, side);
        }
        return null;
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        if (isInGroup(group)) {
            items.add(new ItemStack(this, 1));
        }
    }

    //    @SideOnly (Side.CLIENT)
    //    public void initModels() {
    //        ModelResourceLocation loc = new ModelResourceLocation(getRegistryName(), "inventory");
    //        ModelLoader.setCustomModelResourceLocation(this, 0, loc);
    //        ModelLoader.setCustomMeshDefinition(this, s -> loc);
    //        ModelRegistryHelper.register(loc, new RenderTranslocatorPartItem());
    //    }
}
