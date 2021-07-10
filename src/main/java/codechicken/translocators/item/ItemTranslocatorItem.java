package codechicken.translocators.item;

import codechicken.multipart.api.MultiPartType;
import codechicken.translocators.init.TranslocatorsModContent;
import codechicken.translocators.part.ItemTranslocatorPart;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;

/**
 * Created by covers1624 on 4/20/20.
 */
public class ItemTranslocatorItem extends TranslocatorItem<ItemTranslocatorPart> {

    public ItemTranslocatorItem(Properties props) {
        super(props);
    }

    @Override
    public MultiPartType<ItemTranslocatorPart> getType() {
        return TranslocatorsModContent.itemTranslocatorPartType;
    }

    @Override
    public Capability<?> getTargetCapability() {
        return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
    }
}
