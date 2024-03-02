package codechicken.translocators.item;

import codechicken.multipart.api.MultipartType;
import codechicken.translocators.init.TranslocatorsModContent;
import codechicken.translocators.part.ItemTranslocatorPart;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

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
    public Capability<?> getTargetCapability() {
        return ForgeCapabilities.ITEM_HANDLER;
    }
}
