package codechicken.translocators.network;

import codechicken.lib.packet.PacketCustomChannelBuilder;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.event.EventNetworkChannel;

/**
 * Created by covers1624 on 4/19/20.
 */
public class TranslocatorNetwork {

    public static final ResourceLocation NET_CHANNEL = new ResourceLocation("translocators:network");
    public static EventNetworkChannel netChannel;

    public static final int C_FILTER_GUI_SET_SLOT = 1;
    public static final int C_CRAFTING_GRID_UPDATE = 2;

    public static final int S_CRAFTING_GRID_PLACE = 1;
    public static final int S_CRAFTING_GRID_EXECUTE = 2;

    public static void init() {
        netChannel = PacketCustomChannelBuilder.named(NET_CHANNEL)
                .assignClientHandler(() -> TranslocatorCPH::new)
                .assignServerHandler(() -> TranslocatorSPH::new)
                .build();
    }

}
