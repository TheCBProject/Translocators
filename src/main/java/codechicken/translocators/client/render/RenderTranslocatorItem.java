package codechicken.translocators.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Vector3fc;

import java.util.function.Consumer;

/**
 * Created by covers1624 on 18/11/2017.
 */
public record RenderTranslocatorItem(int type) implements NoDataSpecialModelRenderer {

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        TranslocatorPartRenderer.itemExtents(output);
    }

    @Override
    public void submit(ItemDisplayContext ctx, PoseStack pStack, SubmitNodeCollector collector, int packedLight, int packedOverlay, boolean hasFoil, int outlineColor) {
        TranslocatorPartRenderer.submitItem(type, pStack, ctx, collector, packedLight, packedOverlay);
    }

    public record Unbaked(int translocatorType) implements SpecialModelRenderer.Unbaked {

        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                        Codec.INT.fieldOf("translocatorType").forGetter(Unbaked::translocatorType)
                ).apply(builder, Unbaked::new)
        );

        @Override
        public SpecialModelRenderer<?> bake(BakingContext context) {
            return new RenderTranslocatorItem(translocatorType);
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
