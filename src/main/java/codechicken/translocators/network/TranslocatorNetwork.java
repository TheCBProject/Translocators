package codechicken.translocators.network;

import codechicken.lib.packet.PacketCustomChannel;
import codechicken.translocators.Translocators;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;

/**
 * Created by covers1624 on 4/19/20.
 */
public class TranslocatorNetwork {

    public static final ResourceLocation NET_CHANNEL = new ResourceLocation("translocators:network");
    public static final PacketCustomChannel channel = new PacketCustomChannel(NET_CHANNEL)
            .versioned(Translocators.container().getModInfo().getVersion().toString())
            .client(() -> TranslocatorCPH::new)
            .server(() -> TranslocatorSPH::new);

    public static final int C_FILTER_GUI_SET_SLOT = 1;
    public static final int C_CRAFTING_GRID_UPDATE = 2;

    public static final int S_CRAFTING_GRID_PLACE = 1;
    public static final int S_CRAFTING_GRID_EXECUTE = 2;

    public static void init(IEventBus modBus) {
        channel.init(modBus);
    }

}
