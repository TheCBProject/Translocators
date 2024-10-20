package codechicken.translocators.item;

import codechicken.multipart.api.MultipartType;
import codechicken.translocators.init.TranslocatorsModContent;
import codechicken.translocators.part.ItemTranslocatorPart;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.jetbrains.annotations.Nullable;

/**
 * Created by covers1624 on 4/20/20.
 */
public class ItemTranslocatorItem extends TranslocatorItem<ItemTranslocatorPart> {

    public ItemTranslocatorItem(Properties props) {
        super(props);
    }

    @Override
    public MultipartType<ItemTranslocatorPart> getType() {
        return TranslocatorsModContent.itemTranslocatorPartType.get();
    }

    @Override
    public BlockCapability<?, @Nullable Direction> getTargetCapability() {
        return Capabilities.ItemHandler.BLOCK;
    }
}
