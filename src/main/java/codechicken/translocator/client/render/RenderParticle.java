package codechicken.translocator.client.render;

import codechicken.lib.colour.Colour;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;

public class RenderParticle {
    public static void render(double x, double y, double z, Colour colour, double s, double u1, double v1, double u2, double v2) {
        x -= Particle.interpPosX;
        y -= Particle.interpPosY;
        z -= Particle.interpPosZ;

        float par3 = ActiveRenderInfo.getRotationX();
        float par4 = ActiveRenderInfo.getRotationXZ();
        float par5 = ActiveRenderInfo.getRotationZ();
        float par6 = ActiveRenderInfo.getRotationYZ();
        float par7 = ActiveRenderInfo.getRotationXY();

        VertexBuffer b = Tessellator.getInstance().getBuffer();
        //t.setColorRGBA(colour.r&0xFF, colour.g&0xFF, colour.b&0xFF, colour.a&0xFF);
        //t.addVertexWithUV((x - par3 * s - par6 * s), (y - par4 * s), (z - par5 * s - par7 * s), u2, v2);
        //t.addVertexWithUV((x - par3 * s + par6 * s), (y + par4 * s), (z - par5 * s + par7 * s), u2, v1);
        //t.addVertexWithUV((x + par3 * s + par6 * s), (y + par4 * s), (z + par5 * s + par7 * s), u1, v1);
        //t.addVertexWithUV((x + par3 * s - par6 * s), (y - par4 * s), (z + par5 * s - par7 * s), u1, v2);
        b.pos((x - par3 * s - par6 * s), (y - par4 * s), (z - par5 * s - par7 * s)).tex(u2, v2).color(colour.r & 0xFF, colour.g & 0xFF, colour.b & 0xFF, colour.a & 0xFF).endVertex();
        b.pos((x - par3 * s + par6 * s), (y + par4 * s), (z - par5 * s + par7 * s)).tex(u2, v1).color(colour.r & 0xFF, colour.g & 0xFF, colour.b & 0xFF, colour.a & 0xFF).endVertex();
        b.pos((x + par3 * s + par6 * s), (y + par4 * s), (z + par5 * s + par7 * s)).tex(u1, v1).color(colour.r & 0xFF, colour.g & 0xFF, colour.b & 0xFF, colour.a & 0xFF).endVertex();
        b.pos((x + par3 * s - par6 * s), (y - par4 * s), (z + par5 * s - par7 * s)).tex(u1, v2).color(colour.r & 0xFF, colour.g & 0xFF, colour.b & 0xFF, colour.a & 0xFF).endVertex();
    }
}
