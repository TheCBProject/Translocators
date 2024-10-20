package codechicken.translocators.item;

import codechicken.multipart.api.ItemMultipart;
import codechicken.multipart.api.MultipartType;
import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.util.MultipartPlaceContext;
import codechicken.translocators.part.TranslocatorPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.Nullable;

/**
 * Created by covers1624 on 10/11/2017.
 */
public abstract class TranslocatorItem<T extends TranslocatorPart> extends ItemMultipart {

    public TranslocatorItem(Item.Properties props) {
        super(props);
    }

    public abstract MultipartType<T> getType();

    public abstract BlockCapability<?, @Nullable Direction> getTargetCapability();

    @Override
    public MultiPart newPart(MultipartPlaceContext context) {
        Level world = context.getLevel();
        Direction side = context.getClickedFace();
        BlockPos onPos = context.getClickedPos().relative(side.getOpposite());
        if (world.getCapability(getTargetCapability(), onPos, side) != null) {
            TranslocatorPart part = world.isClientSide() ? getType().createPartClient(null) : getType().createPartServer(null);
            return part.setupPlacement(context.getPlayer(), side);
        }
        return null;
    }
}
