package codechicken.translocators.item;

import codechicken.multipart.api.MultiPartType;
import codechicken.translocators.init.TranslocatorsModContent;
import codechicken.translocators.part.FluidTranslocatorPart;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

/**
 * Created by covers1624 on 4/20/20.
 */
public class FluidTranslocatorItem extends TranslocatorItem<FluidTranslocatorPart> {

    public FluidTranslocatorItem(Properties props) {
        super(props);
    }

    @Override
    public MultiPartType<FluidTranslocatorPart> getType() {
        return TranslocatorsModContent.fluidTranslocatorPartType;
    }

    @Override
    public Capability<?> getTargetCapability() {
        return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
    }
}
