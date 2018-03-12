package codechicken.translocators.part;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.fluid.FluidUtils;
import codechicken.lib.math.MathHelper;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.TMultiPart;
import codechicken.translocators.client.render.RenderTranslocator;
import codechicken.translocators.init.ModItems;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.*;

/**
 * Created by covers1624 on 10/11/2017.
 */
public class FluidTranslocatorPart extends TranslocatorPart {

    public List<MovingLiquid> movingLiquids = new LinkedList<>();
    public List<MovingLiquid> exitingLiquids = new LinkedList<>();

    @Override
    public ItemStack getItem() {
        return new ItemStack(ModItems.translocatorPart, 1, 1);
    }

    @Override
    public int getTType() {
        return 1;
    }

    @Override
    public ResourceLocation getType() {
        return new ResourceLocation("translocators", "fluid_translocator");
    }

    @Override
    public boolean canStay() {
        BlockPos pos = pos().offset(EnumFacing.VALUES[side]);
        TileEntity tile = world().getTileEntity(pos);
        return tile != null && tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.VALUES[side ^ 1]);
    }

    @Override
    public void update() {
        super.update();

        if (world().isRemote) {
            for (Iterator<MovingLiquid> iterator = movingLiquids.iterator(); iterator.hasNext(); ) {
                MovingLiquid m = iterator.next();
                if (m.update()) {
                    iterator.remove();
                    exitingLiquids.add(m);
                }
            }

            exitingLiquids.removeIf(MovingLiquid::update);
        } else {
            //If we can eject stuffs.
            if (a_eject) {
                IFluidHandler myHandler;
                IFluidHandler[] attached = new IFluidHandler[6];
                myHandler = FluidUtils.getFluidHandlerOrEmpty(world(), pos().offset(EnumFacing.VALUES[side]), side ^ 1);

                FluidStack drain = myHandler.drain(fast ? 1000 : 100, false);
                //Check if we have any fluid to transfer.
                if (drain != null && drain.amount != 0) {
                    //All the caps! :D
                    for (int i = 0; i < 6; i++) {
                        if (i == side) {
                            attached[i] = myHandler;
                            continue;
                        }
                        EnumFacing f = EnumFacing.VALUES[i];
                        TileEntity tile = world().getTileEntity(pos().offset(f));
                        attached[i] = FluidUtils.getFluidHandlerOrEmpty(tile, f.getOpposite());
                    }

                    List<FluidTransfer> transfers = new ArrayList<>();
                    FluidStack move = drain.copy();
                    spreadOutput(move, getOutputs(), attached, transfers);
                    myHandler.drain(drain.amount - move.amount, true);
                    sendTransferPacket(transfers);
                }
            }
        }
    }

    private void spreadOutput(FluidStack move, int[] outputs, IFluidHandler[] attached, List<FluidTransfer> transfers) {
        for (int k = 0; k < outputs.length && move.amount > 0; k++) {
            int dst = outputs[k];
            IFluidHandler outaccess = attached[dst];

            int fit = outaccess.fill(move, false);
            int spread = outputs.length - k;
            fit = Math.min(fit, move.amount / spread + world().rand.nextInt(move.amount % spread + 1));

            if (fit == 0) {
                continue;
            }

            FluidStack add = FluidUtils.copy(move, fit);
            outaccess.fill(add, true);
            move.amount -= fit;

            transfers.add(new FluidTransfer(dst, add));
        }
    }

    public int[] getOutputs() {
        int[] att = new int[6];
        int[] r_att = new int[6];
        int a = 0;
        int r_a = 0;
        for (int i = 0; i < 6; i++) {
            TMultiPart p = tile().partMap(i);
            if (p instanceof FluidTranslocatorPart) {
                FluidTranslocatorPart part = (FluidTranslocatorPart) p;
                //If the part can accept stuffs.
                if (!part.canEject() && i != side) {
                    //Add it to separate arrays.
                    if (part.redstone) {
                        r_att[r_a++] = i;
                    } else {
                        att[a++] = i;
                    }
                }
            }
        }
        //Concat the arrays, prioritizing non redstone connections.
        int[] ret = new int[a + r_a];
        if (a > 0) {
            System.arraycopy(att, 0, ret, 0, a);
        }
        if (r_a > 0) {
            System.arraycopy(r_att, 0, ret, a > 0 ? a + 1 : 0, r_a);
        }
        return ret;
    }

    private void sendTransferPacket(List<FluidTransfer> transfers) {
        MCDataOutput stream = getIncStream();
        stream.writeByte(transfers.size());
        for (FluidTransfer t : transfers) {
            stream.writeByte(t.dst);
            stream.writeFluidStack(t.fluid);
        }
    }

    public List<MovingLiquid> getMovingLiquids() {
        List<MovingLiquid> liquids = new LinkedList<>();
        liquids.addAll(movingLiquids);
        liquids.addAll(exitingLiquids);
        return liquids;
    }

    @Override
    public void readIncUpdate(MCDataInput packet) {

        List<FluidTransfer> transfers = new ArrayList<>();//Incoming transfers to process.
        Set<Integer> updatedDests = new HashSet<>();//Quick lookup set of updated destinations.

        int num_t = packet.readUByte();
        for (int i = 0; i < num_t; i++) {
            int dst = packet.readUByte();
            FluidStack fluid = packet.readFluidStack();
            transfers.add(new FluidTransfer(dst, fluid));
            updatedDests.add(dst);
        }

        for(FluidTransfer t : transfers) {
            boolean found = false;
            for (Iterator<MovingLiquid> iterator = movingLiquids.iterator(); iterator.hasNext();) {
                MovingLiquid m = iterator.next();
                if (m.liquid.isFluidEqual(t.fluid) && m.dst == t.dst) {
                    m.addLiquid(t.fluid.amount);
                    found = true;
                    continue;
                }
                if (m.dst == t.dst && !updatedDests.contains(m.dst)) {
                    iterator.remove();
                    m.finish();
                    exitingLiquids.add(m);
                }
            }
            if (!found) {
                movingLiquids.add(new MovingLiquid(t.dst, t.fluid));
            }
        }
    }

    @Override
    public void renderDynamic(Vector3 pos, int pass, float delta) {
        RenderTranslocator.renderFluid(this, pos, delta);
        super.renderDynamic(pos, pass, delta);
    }

    //Network object for client sync.
    private class FluidTransfer {

        int dst;
        FluidStack fluid;

        public FluidTransfer(int dst, FluidStack fluid) {
            this.dst = dst;
            this.fluid = fluid;
        }
    }

    public class MovingLiquid {

        public int dst;
        public FluidStack liquid;

        public double a_start;
        public double b_start;

        public double a_end;
        public double b_end;

        public boolean fast;

        public MovingLiquid(int dst, FluidStack add) {
            this.dst = dst;
            liquid = add;
            fast = FluidTranslocatorPart.this.fast;
            capLiquid();
        }

        private void capLiquid() {
            liquid.amount = Math.min(liquid.amount, fast ? 1000 : 100);
        }

        public boolean update() {
            if (a_end == 1) {
                return true;
            }

            b_start = a_start;
            a_start = MathHelper.approachLinear(a_start, 1, 0.2);

            b_end = a_end;

            if (liquid.amount > 0) {
                liquid.amount = Math.max(liquid.amount - (fast ? 200 : 20), 0);
                return liquid.amount == 0;
            }
            a_end = MathHelper.approachLinear(a_end, 1, 0.2);

            return false;
        }

        public void addLiquid(int moving) {
            if (liquid.amount == 0) {
                throw new IllegalArgumentException("Something went wrong!");
            }

            liquid.amount += moving;
            fast = FluidTranslocatorPart.this.fast;
            capLiquid();
        }

        public void finish() {
            liquid.amount = 0;
        }
    }
}
