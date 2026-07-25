package codechicken.translocators.client;

import net.neoforged.neoforge.model.data.ModelProperty;

/**
 * Created by covers1624 on 7/22/26.
 */
public record TranslocatorModelData(int side, int type, int iconIndex) {

    public static final ModelProperty<TranslocatorModelData> TYPE = new ModelProperty<>();
}
