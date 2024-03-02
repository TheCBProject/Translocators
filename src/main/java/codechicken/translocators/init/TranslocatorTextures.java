package codechicken.translocators.init;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * Created by covers1624 on 14/11/2017.
 */
public class TranslocatorTextures {

    public static TextureAtlasSprite[] ITEM_ICONS = new TextureAtlasSprite[64];
    public static TextureAtlasSprite[] FLUID_ICONS = new TextureAtlasSprite[64];
    public static TextureAtlasSprite[][] TEXTURES = new TextureAtlasSprite[][] { ITEM_ICONS, FLUID_ICONS };
    public static TextureAtlasSprite CRAFTING_GRID;

    private static final String BLOCKS_ = "translocators:block/";
    private static final String TRANSLOCATOR_ = BLOCKS_ + "translocator/";
    private static final String ITEM_ = TRANSLOCATOR_ + "item/";
    private static final String FLUID_ = TRANSLOCATOR_ + "fluid/";

    public static void init(IEventBus bus) {
        bus.addListener(TranslocatorTextures::textureStitchPost);
    }

    private static void textureStitchPost(TextureStitchEvent.Post event) {
        TextureAtlas atlas = event.getAtlas();
        if (!atlas.location().equals(TextureAtlas.LOCATION_BLOCKS)) return;

        CRAFTING_GRID = atlas.getSprite(new ResourceLocation(BLOCKS_ + "crafting_grid"));

        //See https://github.com/covers1624/TextureSheetExporter/wiki
        //@formatter:off
        ITEM_ICONS [0x00]                        = atlas.getSprite(new ResourceLocation(ITEM_  + "item_00_0_0_00"));
        ITEM_ICONS [0x01]                        = atlas.getSprite(new ResourceLocation(ITEM_  + "item_10_0_0_00"));
        ITEM_ICONS [0x02 | 0x08]                 = atlas.getSprite(new ResourceLocation(ITEM_  + "item_11_0_1_00"));
        ITEM_ICONS [0x04 | 0x08]                 = atlas.getSprite(new ResourceLocation(ITEM_  + "item_00_1_1_00"));
        ITEM_ICONS [0x01 | 0x04 | 0x08]          = atlas.getSprite(new ResourceLocation(ITEM_  + "item_10_1_1_00"));
        ITEM_ICONS [0x02 | 0x04 | 0x08]          = atlas.getSprite(new ResourceLocation(ITEM_  + "item_11_1_1_00"));
        ITEM_ICONS [0x10]                        = atlas.getSprite(new ResourceLocation(ITEM_  + "item_00_0_0_10"));
        ITEM_ICONS [0x01 | 0x10]                 = atlas.getSprite(new ResourceLocation(ITEM_  + "item_10_0_0_10"));
        ITEM_ICONS [0x02 | 0x10]                 = atlas.getSprite(new ResourceLocation(ITEM_  + "item_11_0_0_10"));
        ITEM_ICONS [0x02]                        = atlas.getSprite(new ResourceLocation(ITEM_  + "item_11_0_0_00"));
        ITEM_ICONS [0x04 | 0x10]                 = atlas.getSprite(new ResourceLocation(ITEM_  + "item_00_1_0_10"));
        ITEM_ICONS [0x01 | 0x04 | 0x10]          = atlas.getSprite(new ResourceLocation(ITEM_  + "item_10_1_0_10"));
        ITEM_ICONS [0x02 | 0x04 | 0x10]          = atlas.getSprite(new ResourceLocation(ITEM_  + "item_11_1_0_10"));
        ITEM_ICONS [0x08 | 0x10]                 = atlas.getSprite(new ResourceLocation(ITEM_  + "item_00_0_1_10"));
        ITEM_ICONS [0x01 | 0x08 | 0x10]          = atlas.getSprite(new ResourceLocation(ITEM_  + "item_10_0_1_10"));
        ITEM_ICONS [0x02 | 0x08 | 0x10]          = atlas.getSprite(new ResourceLocation(ITEM_  + "item_11_0_1_10"));
        ITEM_ICONS [0x04 | 0x08 | 0x10]          = atlas.getSprite(new ResourceLocation(ITEM_  + "item_00_1_1_10"));
        ITEM_ICONS [0x01 | 0x04 | 0x08 | 0x10]   = atlas.getSprite(new ResourceLocation(ITEM_  + "item_10_1_1_10"));
        ITEM_ICONS [0x02 | 0x04 | 0x08 | 0x10]   = atlas.getSprite(new ResourceLocation(ITEM_  + "item_11_1_1_10"));
        ITEM_ICONS [0x20]                        = atlas.getSprite(new ResourceLocation(ITEM_  + "item_00_0_0_11"));
        ITEM_ICONS [0x01 | 0x20]                 = atlas.getSprite(new ResourceLocation(ITEM_  + "item_10_0_0_11"));
        ITEM_ICONS [0x02 | 0x20]                 = atlas.getSprite(new ResourceLocation(ITEM_  + "item_11_0_0_11"));
        ITEM_ICONS [0x04 | 0x20]                 = atlas.getSprite(new ResourceLocation(ITEM_  + "item_00_1_0_11"));
        ITEM_ICONS [0x01 | 0x04 | 0x20]          = atlas.getSprite(new ResourceLocation(ITEM_  + "item_10_1_0_11"));
        ITEM_ICONS [0x02 | 0x04 | 0x20]          = atlas.getSprite(new ResourceLocation(ITEM_  + "item_11_1_0_11"));
        ITEM_ICONS [0x04]                        = atlas.getSprite(new ResourceLocation(ITEM_  + "item_00_1_0_00"));
        ITEM_ICONS [0x08 | 0x20]                 = atlas.getSprite(new ResourceLocation(ITEM_  + "item_00_0_1_11"));
        ITEM_ICONS [0x01 | 0x08 | 0x20]          = atlas.getSprite(new ResourceLocation(ITEM_  + "item_10_0_1_11"));
        ITEM_ICONS [0x02 | 0x08 | 0x20]          = atlas.getSprite(new ResourceLocation(ITEM_  + "item_11_0_1_11"));
        ITEM_ICONS [0x04 | 0x08 | 0x20]          = atlas.getSprite(new ResourceLocation(ITEM_  + "item_00_1_1_11"));
        ITEM_ICONS [0x01 | 0x04 | 0x08 | 0x20]   = atlas.getSprite(new ResourceLocation(ITEM_  + "item_10_1_1_11"));
        ITEM_ICONS [0x02 | 0x04 | 0x08 | 0x20]   = atlas.getSprite(new ResourceLocation(ITEM_  + "item_11_1_1_11"));
        ITEM_ICONS [0x01 | 0x04]                 = atlas.getSprite(new ResourceLocation(ITEM_  + "item_10_1_0_00"));
        ITEM_ICONS [0x02 | 0x04]                 = atlas.getSprite(new ResourceLocation(ITEM_  + "item_11_1_0_00"));
        ITEM_ICONS [0x08]                        = atlas.getSprite(new ResourceLocation(ITEM_  + "item_00_0_1_00"));
        ITEM_ICONS [0x01 | 0x08]                 = atlas.getSprite(new ResourceLocation(ITEM_  + "item_10_0_1_00"));
        FLUID_ICONS[0x00]                        = atlas.getSprite(new ResourceLocation(FLUID_ + "fluid_00_0_0_00"));
        FLUID_ICONS[0x01]                        = atlas.getSprite(new ResourceLocation(FLUID_ + "fluid_10_0_0_00"));
        FLUID_ICONS[0x02 | 0x08]                 = atlas.getSprite(new ResourceLocation(FLUID_ + "fluid_11_0_1_00"));
        FLUID_ICONS[0x04 | 0x08]                 = atlas.getSprite(new ResourceLocation(FLUID_ + "fluid_00_1_1_00"));
        FLUID_ICONS[0x01 | 0x04 | 0x08]          = atlas.getSprite(new ResourceLocation(FLUID_ + "fluid_10_1_1_00"));
        FLUID_ICONS[0x02 | 0x04 | 0x08]          = atlas.getSprite(new ResourceLocation(FLUID_ + "fluid_11_1_1_00"));
        FLUID_ICONS[0x10]                        = atlas.getSprite(new ResourceLocation(FLUID_ + "fluid_00_0_0_10"));
        FLUID_ICONS[0x01 | 0x10]                 = atlas.getSprite(new ResourceLocation(FLUID_ + "fluid_10_0_0_10"));
        FLUID_ICONS[0x02 | 0x10]                 = atlas.getSprite(new ResourceLocation(FLUID_ + "fluid_11_0_0_10"));
        FLUID_ICONS[0x02]                        = atlas.getSprite(new ResourceLocation(FLUID_ + "fluid_11_0_0_00"));
        FLUID_ICONS[0x04 | 0x10]                 = atlas.getSprite(new ResourceLocation(FLUID_ + "fluid_00_1_0_10"));
        FLUID_ICONS[0x01 | 0x04 | 0x10]          = atlas.getSprite(new ResourceLocation(FLUID_ + "fluid_10_1_0_10"));
        FLUID_ICONS[0x02 | 0x04 | 0x10]          = atlas.getSprite(new ResourceLocation(FLUID_ + "fluid_11_1_0_10"));
        FLUID_ICONS[0x08 | 0x10]                 = atlas.getSprite(new ResourceLocation(FLUID_ + "fluid_00_0_1_10"));
        FLUID_ICONS[0x01 | 0x08 | 0x10]          = atlas.getSprite(new ResourceLocation(FLUID_ + "fluid_10_0_1_10"));
        FLUID_ICONS[0x02 | 0x08 | 0x10]          = atlas.getSprite(new ResourceLocation(FLUID_ + "fluid_11_0_1_10"));
        FLUID_ICONS[0x04 | 0x08 | 0x10]          = atlas.getSprite(new ResourceLocation(FLUID_ + "fluid_00_1_1_10"));
        FLUID_ICONS[0x01 | 0x04 | 0x08 | 0x10]   = atlas.getSprite(new ResourceLocation(FLUID_ + "fluid_10_1_1_10"));
        FLUID_ICONS[0x02 | 0x04 | 0x08 | 0x10]   = atlas.getSprite(new ResourceLocation(FLUID_ + "fluid_11_1_1_10"));
        FLUID_ICONS[0x20]                        = atlas.getSprite(new ResourceLocation(FLUID_ + "fluid_00_0_0_11"));
        FLUID_ICONS[0x01 | 0x20]                 = atlas.getSprite(new ResourceLocation(FLUID_ + "fluid_10_0_0_11"));
        FLUID_ICONS[0x02 | 0x20]                 = atlas.getSprite(new ResourceLocation(FLUID_ + "fluid_11_0_0_11"));
        FLUID_ICONS[0x04 | 0x20]                 = atlas.getSprite(new ResourceLocation(FLUID_ + "fluid_00_1_0_11"));
        FLUID_ICONS[0x01 | 0x04 | 0x20]          = atlas.getSprite(new ResourceLocation(FLUID_ + "fluid_10_1_0_11"));
        FLUID_ICONS[0x02 | 0x04 | 0x20]          = atlas.getSprite(new ResourceLocation(FLUID_ + "fluid_11_1_0_11"));
        FLUID_ICONS[0x04]                        = atlas.getSprite(new ResourceLocation(FLUID_ + "fluid_00_1_0_00"));
        FLUID_ICONS[0x08 | 0x20]                 = atlas.getSprite(new ResourceLocation(FLUID_ + "fluid_00_0_1_11"));
        FLUID_ICONS[0x01 | 0x08 | 0x20]          = atlas.getSprite(new ResourceLocation(FLUID_ + "fluid_10_0_1_11"));
        FLUID_ICONS[0x02 | 0x08 | 0x20]          = atlas.getSprite(new ResourceLocation(FLUID_ + "fluid_11_0_1_11"));
        FLUID_ICONS[0x04 | 0x08 | 0x20]          = atlas.getSprite(new ResourceLocation(FLUID_ + "fluid_00_1_1_11"));
        FLUID_ICONS[0x01 | 0x04 | 0x08 | 0x20]   = atlas.getSprite(new ResourceLocation(FLUID_ + "fluid_10_1_1_11"));
        FLUID_ICONS[0x02 | 0x04 | 0x08 | 0x20]   = atlas.getSprite(new ResourceLocation(FLUID_ + "fluid_11_1_1_11"));
        FLUID_ICONS[0x01 | 0x04]                 = atlas.getSprite(new ResourceLocation(FLUID_ + "fluid_10_1_0_00"));
        FLUID_ICONS[0x02 | 0x04]                 = atlas.getSprite(new ResourceLocation(FLUID_ + "fluid_11_1_0_00"));
        FLUID_ICONS[0x08]                        = atlas.getSprite(new ResourceLocation(FLUID_ + "fluid_00_0_1_00"));
        FLUID_ICONS[0x01 | 0x08]                 = atlas.getSprite(new ResourceLocation(FLUID_ + "fluid_10_0_1_00"));
        //@formatter:on

    }
}
