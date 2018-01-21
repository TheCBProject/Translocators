package codechicken.translocator.client.render;

import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.OBJParser;
import codechicken.lib.texture.TextureUtils;
import codechicken.lib.vec.SwapYZ;
import codechicken.lib.vec.Vector3;
import codechicken.lib.vec.uv.IconTransformation;
import codechicken.translocator.init.TranslocatorTextures;
import codechicken.translocator.part.TranslocatorPart;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Created by covers1624 on 15/11/2017.
 */
public class Temp {


    public static void renderTest(TranslocatorPart p, Vector3 pos, int pass, float frame) {
        try {
            CCRenderState ccrs = CCRenderState.instance();
            CCModel[] plates = new CCModel[6];

            Map<String, CCModel> models = OBJParser.parseModels(new FileInputStream(new File("C:\\Users\\laughlan\\Modding Projects\\Minecraft\\1.12\\Translocators\\src\\main\\resources\\assets\\translocator\\models\\model_new.obj")), 0x07, new SwapYZ());
            plates[0] = models.get("Plate");
            CCModel.generateSidedModels(plates, 0, new Vector3());

            TextureUtils.bindBlockTexture();

            Vector3 trans = pos.copy().add(Vector3.center);
            IconTransformation i_trans = new IconTransformation(TranslocatorTextures.missing);

            ccrs.reset();
            ccrs.startDrawing(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
            plates[0].render(ccrs, trans.translation(), i_trans);
            models.get("Insert").render(ccrs, trans.translation(), i_trans);
            ccrs.draw();
        } catch (IOException | NullPointerException e ) {
            e.printStackTrace();
        }
    }

}
