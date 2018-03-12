package codechicken.translocators.tile;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;

/**
 * Created by covers1624 on 6/12/2016.
 */
public abstract class DummyFluidCapability implements IFluidHandler {

    @Override
    public int fill(FluidStack resource, boolean doFill) {

        return 0;
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {

        return null;
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {

        return null;
    }
}
