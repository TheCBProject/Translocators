package codechicken.translocators.item;

import codechicken.multipart.api.ItemMultipart;
import codechicken.multipart.api.MultipartType;
import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.util.MultipartPlaceContext;
import codechicken.translocators.part.TranslocatorPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;

/**
 * Created by covers1624 on 10/11/2017.
 */
public abstract class TranslocatorItem<T extends TranslocatorPart> extends ItemMultipart {

    public TranslocatorItem(Item.Properties props) {
        super(props);
    }

    public abstract MultipartType<T> getType();

    public abstract Capability<?> getTargetCapability();

    @Override
    public MultiPart newPart(MultipartPlaceContext context) {
        Level world = context.getLevel();
        Direction side = context.getClickedFace();
        BlockPos onPos = context.getClickedPos().relative(side.getOpposite());
        BlockEntity tile = world.getBlockEntity(onPos);
        if (tile != null && tile.getCapability(getTargetCapability(), side).isPresent()) {
            TranslocatorPart part = world.isClientSide() ? getType().createPartClient(null) : getType().createPartServer(null);
            return part.setupPlacement(context.getPlayer(), side);
        }
        return null;
    }
}
