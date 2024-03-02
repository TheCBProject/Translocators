package codechicken.translocators.item;

import codechicken.multipart.api.MultipartType;
import codechicken.translocators.init.TranslocatorsModContent;
import codechicken.translocators.part.FluidTranslocatorPart;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

/**
 * Created by covers1624 on 4/20/20.
 */
public class FluidTranslocatorItem extends TranslocatorItem<FluidTranslocatorPart> {

    public FluidTranslocatorItem(Properties props) {
        super(props);
    }

    @Override
    public MultipartType<FluidTranslocatorPart> getType() {
        return TranslocatorsModContent.fluidTranslocatorPartType.get();
    }

    @Override
    public Capability<?> getTargetCapability() {
        return ForgeCapabilities.FLUID_HANDLER;
    }
}
