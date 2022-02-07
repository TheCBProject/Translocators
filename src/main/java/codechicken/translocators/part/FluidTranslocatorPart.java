package codechicken.translocators.part;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.math.MathHelper;
import codechicken.multipart.api.MultiPartType;
import codechicken.multipart.api.part.TMultiPart;
import codechicken.translocators.client.render.RenderTranslocator;
import codechicken.translocators.handler.ConfigHandler;
import codechicken.translocators.init.TranslocatorsModContent;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;

import java.util.*;

/**
 * Created by covers1624 on 10/11/2017.
 */
public class FluidTranslocatorPart extends TranslocatorPart {

    @CapabilityInject (IFluidHandler.class)
    public static Capability<IFluidHandler> FLUID_CAP = null;

    public List<MovingLiquid> movingLiquids = new LinkedList<>();
    public List<MovingLiquid> exitingLiquids = new LinkedList<>();

    @Override
    public ItemStack getItem() {
        return new ItemStack(TranslocatorsModContent.fluidTranslocatorItem.get(), 1);
    }

    @Override
    public int getTType() {
        return 1;
    }

    @Override
    public MultiPartType<?> getType() {
        return TranslocatorsModContent.fluidTranslocatorPartType.get();
    }

    @Override
    public boolean canStay() {
        return capCache().getCapability(FLUID_CAP, Direction.BY_3D_DATA[side]).isPresent();
    }

    @Override
    public void tick() {
        super.tick();

        if (world().isClientSide()) {
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
                IFluidHandler[] attached = new IFluidHandler[6];
                for (int i = 0; i < 6; i++) {
                    //Fill with empty if the translocator doesnt exist or is the incorrect type.
                    if (canInsert(i) || i == side) {
                        attached[i] = capCache().getCapabilityOr(FLUID_CAP, Direction.BY_3D_DATA[i], EmptyFluidHandler.INSTANCE);
                    } else {
                        attached[i] = EmptyFluidHandler.INSTANCE;
                    }
                }
                IFluidHandler myHandler = attached[side];

                FluidStack drain = myHandler.drain(fast ? 1000 : 100, FluidAction.SIMULATE);
                //Check if we have any fluid to transfer.
                if (drain != null && drain.getAmount() != 0) {
                    List<FluidTransfer> transfers = new ArrayList<>();
                    FluidStack move = drain.copy();
                    spreadOutput(move, getOutputs(), attached, transfers);
                    myHandler.drain(drain.getAmount() - move.getAmount(), FluidAction.EXECUTE);
                    sendTransferPacket(transfers);
                }
            }
        }
    }

    private void spreadOutput(FluidStack move, int[] outputs, IFluidHandler[] attached, List<FluidTransfer> transfers) {
        for (int k = 0; k < outputs.length && move.getAmount() > 0; k++) {
            int dst = outputs[k];
            IFluidHandler outaccess = attached[dst];

            int fit = outaccess.fill(move, FluidAction.SIMULATE);
            int spread = outputs.length - k;
            fit = Math.min(fit, move.getAmount() / spread + world().random.nextInt(move.getAmount() % spread + 1));

            if (fit == 0) {
                continue;
            }

            FluidStack add = new FluidStack(move, fit);
            outaccess.fill(add, FluidAction.EXECUTE);
            move.shrink(fit);

            transfers.add(new FluidTransfer(dst, add));
        }
    }

    public int[] getOutputs() {
        int[] att = new int[6];
        int[] r_att = new int[6];
        int a = 0;
        int r_a = 0;
        for (int i = 0; i < 6; i++) {
            TMultiPart p = tile().getSlottedPart(i);
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
            System.arraycopy(r_att, 0, ret, a, r_a);
        }
        return ret;
    }

    private void sendTransferPacket(List<FluidTransfer> transfers) {
        if (transfers.isEmpty()) return;
        sendIncUpdate(packet -> {
            packet.writeByte(transfers.size());
            for (FluidTransfer t : transfers) {
                packet.writeByte(t.dst);
                packet.writeFluidStack(t.fluid);
            }
        });
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

        for (FluidTransfer t : transfers) {
            boolean found = false;
            for (Iterator<MovingLiquid> iterator = movingLiquids.iterator(); iterator.hasNext(); ) {
                MovingLiquid m = iterator.next();
                if (m.liquid.isFluidEqual(t.fluid) && m.dst == t.dst) {
                    m.addLiquid(t.fluid.getAmount());
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
    public void renderDynamic(MatrixStack mStack, IRenderTypeBuffer buffers, int packedLight, int packedOverlay, float partialTicks) {
        // // only render fluid, if not set to hidden in config
        if (!ConfigHandler.hideParticlesAndMovingParts) {
            RenderTranslocator.renderFluid(this, mStack, buffers, partialTicks);
        }
        super.renderDynamic(mStack, buffers, packedLight, packedOverlay, partialTicks);
    }

    //    @Override
//    public void renderDynamic(Vector3 pos, int pass, float delta) {
//        RenderTranslocator.renderFluid(this, pos, delta);
//        super.renderDynamic(pos, pass, delta);
//    }

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
            liquid.setAmount(Math.min(liquid.getAmount(), fast ? 1000 : 100));
        }

        public boolean update() {
            if (a_end == 1) {
                return true;
            }

            b_start = a_start;
            a_start = MathHelper.approachLinear(a_start, 1, 0.2);

            b_end = a_end;

            if (liquid.getAmount() > 0) {
                liquid.setAmount(Math.max(liquid.getAmount() - (fast ? 200 : 20), 0));
                return liquid.getAmount() == 0;
            }
            a_end = MathHelper.approachLinear(a_end, 1, 0.2);

            return false;
        }

        public void addLiquid(int moving) {
            if (liquid.getAmount() == 0) {
                throw new IllegalArgumentException("Something went wrong!");
            }

            liquid.grow(moving);
            fast = FluidTranslocatorPart.this.fast;
            capLiquid();
        }

        public void finish() {
            liquid.setAmount(0);
        }
    }
}
