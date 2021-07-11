package codechicken.translocators.init;

import codechicken.lib.texture.AtlasRegistrar;
import codechicken.lib.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import java.util.function.Consumer;

/**
 * Created by covers1624 on 14/11/2017.
 */
public class TranslocatorTextures implements IIconRegister {

    public static TextureAtlasSprite[] ITEM_ICONS = new TextureAtlasSprite[64];
    public static TextureAtlasSprite[] FLUID_ICONS = new TextureAtlasSprite[64];
    public static TextureAtlasSprite[][] TEXTURES;
    public static TextureAtlasSprite CRAFTING_GRID;

    private static final String BLOCKS_ = "translocators:blocks/";
    private static final String TRANSLOCATOR_ = BLOCKS_ + "translocator/";
    private static final String ITEM_ = TRANSLOCATOR_ + "item/";
    private static final String FLUID_ = TRANSLOCATOR_ + "fluid/";

    private static AtlasRegistrar registrar;

    @Override
    public void registerIcons(AtlasRegistrar registrar) {
        TranslocatorTextures.registrar = registrar;
        register(BLOCKS_ + "crafting_grid", e -> CRAFTING_GRID = e);

        //See https://github.com/covers1624/TextureSheetExporter/wiki
        //@formatter:off
        register(ITEM_  + "item_00_0_0_00",  e -> ITEM_ICONS [0x00]                       = e);
        register(ITEM_  + "item_10_0_0_00",  e -> ITEM_ICONS [0x01]                       = e);
        register(ITEM_  + "item_11_0_1_00",  e -> ITEM_ICONS [0x02 | 0x08]                = e);
        register(ITEM_  + "item_00_1_1_00",  e -> ITEM_ICONS [0x04 | 0x08]                = e);
        register(ITEM_  + "item_10_1_1_00",  e -> ITEM_ICONS [0x01 | 0x04 | 0x08]         = e);
        register(ITEM_  + "item_11_1_1_00",  e -> ITEM_ICONS [0x02 | 0x04 | 0x08]         = e);
        register(ITEM_  + "item_00_0_0_10",  e -> ITEM_ICONS [0x10]                       = e);
        register(ITEM_  + "item_10_0_0_10",  e -> ITEM_ICONS [0x01 | 0x10]                = e);
        register(ITEM_  + "item_11_0_0_10",  e -> ITEM_ICONS [0x02 | 0x10]                = e);
        register(ITEM_  + "item_11_0_0_00",  e -> ITEM_ICONS [0x02]                       = e);
        register(ITEM_  + "item_00_1_0_10",  e -> ITEM_ICONS [0x04 | 0x10]                = e);
        register(ITEM_  + "item_10_1_0_10",  e -> ITEM_ICONS [0x01 | 0x04 | 0x10]         = e);
        register(ITEM_  + "item_11_1_0_10",  e -> ITEM_ICONS [0x02 | 0x04 | 0x10]         = e);
        register(ITEM_  + "item_00_0_1_10",  e -> ITEM_ICONS [0x08 | 0x10]                = e);
        register(ITEM_  + "item_10_0_1_10",  e -> ITEM_ICONS [0x01 | 0x08 | 0x10]         = e);
        register(ITEM_  + "item_11_0_1_10",  e -> ITEM_ICONS [0x02 | 0x08 | 0x10]         = e);
        register(ITEM_  + "item_00_1_1_10",  e -> ITEM_ICONS [0x04 | 0x08 | 0x10]         = e);
        register(ITEM_  + "item_10_1_1_10",  e -> ITEM_ICONS [0x01 | 0x04 | 0x08 | 0x10]  = e);
        register(ITEM_  + "item_11_1_1_10",  e -> ITEM_ICONS [0x02 | 0x04 | 0x08 | 0x10]  = e);
        register(ITEM_  + "item_00_0_0_11",  e -> ITEM_ICONS [0x20]                       = e);
        register(ITEM_  + "item_10_0_0_11",  e -> ITEM_ICONS [0x01 | 0x20]                = e);
        register(ITEM_  + "item_11_0_0_11",  e -> ITEM_ICONS [0x02 | 0x20]                = e);
        register(ITEM_  + "item_00_1_0_11",  e -> ITEM_ICONS [0x04 | 0x20]                = e);
        register(ITEM_  + "item_10_1_0_11",  e -> ITEM_ICONS [0x01 | 0x04 | 0x20]         = e);
        register(ITEM_  + "item_11_1_0_11",  e -> ITEM_ICONS [0x02 | 0x04 | 0x20]         = e);
        register(ITEM_  + "item_00_1_0_00",  e -> ITEM_ICONS [0x04]                       = e);
        register(ITEM_  + "item_00_0_1_11",  e -> ITEM_ICONS [0x08 | 0x20]                = e);
        register(ITEM_  + "item_10_0_1_11",  e -> ITEM_ICONS [0x01 | 0x08 | 0x20]         = e);
        register(ITEM_  + "item_11_0_1_11",  e -> ITEM_ICONS [0x02 | 0x08 | 0x20]         = e);
        register(ITEM_  + "item_00_1_1_11",  e -> ITEM_ICONS [0x04 | 0x08 | 0x20]         = e);
        register(ITEM_  + "item_10_1_1_11",  e -> ITEM_ICONS [0x01 | 0x04 | 0x08 | 0x20]  = e);
        register(ITEM_  + "item_11_1_1_11",  e -> ITEM_ICONS [0x02 | 0x04 | 0x08 | 0x20]  = e);
        register(ITEM_  + "item_10_1_0_00",  e -> ITEM_ICONS [0x01 | 0x04]                = e);
        register(ITEM_  + "item_11_1_0_00",  e -> ITEM_ICONS [0x02 | 0x04]                = e);
        register(ITEM_  + "item_00_0_1_00",  e -> ITEM_ICONS [0x08]                       = e);
        register(ITEM_  + "item_10_0_1_00",  e -> ITEM_ICONS [0x01 | 0x08]                = e);
        register(FLUID_ + "fluid_00_0_0_00", e -> FLUID_ICONS[0x00]                       = e);
        register(FLUID_ + "fluid_10_0_0_00", e -> FLUID_ICONS[0x01]                       = e);
        register(FLUID_ + "fluid_11_0_1_00", e -> FLUID_ICONS[0x02 | 0x08]                = e);
        register(FLUID_ + "fluid_00_1_1_00", e -> FLUID_ICONS[0x04 | 0x08]                = e);
        register(FLUID_ + "fluid_10_1_1_00", e -> FLUID_ICONS[0x01 | 0x04 | 0x08]         = e);
        register(FLUID_ + "fluid_11_1_1_00", e -> FLUID_ICONS[0x02 | 0x04 | 0x08]         = e);
        register(FLUID_ + "fluid_00_0_0_10", e -> FLUID_ICONS[0x10]                       = e);
        register(FLUID_ + "fluid_10_0_0_10", e -> FLUID_ICONS[0x01 | 0x10]                = e);
        register(FLUID_ + "fluid_11_0_0_10", e -> FLUID_ICONS[0x02 | 0x10]                = e);
        register(FLUID_ + "fluid_11_0_0_00", e -> FLUID_ICONS[0x02]                       = e);
        register(FLUID_ + "fluid_00_1_0_10", e -> FLUID_ICONS[0x04 | 0x10]                = e);
        register(FLUID_ + "fluid_10_1_0_10", e -> FLUID_ICONS[0x01 | 0x04 | 0x10]         = e);
        register(FLUID_ + "fluid_11_1_0_10", e -> FLUID_ICONS[0x02 | 0x04 | 0x10]         = e);
        register(FLUID_ + "fluid_00_0_1_10", e -> FLUID_ICONS[0x08 | 0x10]                = e);
        register(FLUID_ + "fluid_10_0_1_10", e -> FLUID_ICONS[0x01 | 0x08 | 0x10]         = e);
        register(FLUID_ + "fluid_11_0_1_10", e -> FLUID_ICONS[0x02 | 0x08 | 0x10]         = e);
        register(FLUID_ + "fluid_00_1_1_10", e -> FLUID_ICONS[0x04 | 0x08 | 0x10]         = e);
        register(FLUID_ + "fluid_10_1_1_10", e -> FLUID_ICONS[0x01 | 0x04 | 0x08 | 0x10]  = e);
        register(FLUID_ + "fluid_11_1_1_10", e -> FLUID_ICONS[0x02 | 0x04 | 0x08 | 0x10]  = e);
        register(FLUID_ + "fluid_00_0_0_11", e -> FLUID_ICONS[0x20]                       = e);
        register(FLUID_ + "fluid_10_0_0_11", e -> FLUID_ICONS[0x01 | 0x20]                = e);
        register(FLUID_ + "fluid_11_0_0_11", e -> FLUID_ICONS[0x02 | 0x20]                = e);
        register(FLUID_ + "fluid_00_1_0_11", e -> FLUID_ICONS[0x04 | 0x20]                = e);
        register(FLUID_ + "fluid_10_1_0_11", e -> FLUID_ICONS[0x01 | 0x04 | 0x20]         = e);
        register(FLUID_ + "fluid_11_1_0_11", e -> FLUID_ICONS[0x02 | 0x04 | 0x20]         = e);
        register(FLUID_ + "fluid_00_1_0_00", e -> FLUID_ICONS[0x04]                       = e);
        register(FLUID_ + "fluid_00_0_1_11", e -> FLUID_ICONS[0x08 | 0x20]                = e);
        register(FLUID_ + "fluid_10_0_1_11", e -> FLUID_ICONS[0x01 | 0x08 | 0x20]         = e);
        register(FLUID_ + "fluid_11_0_1_11", e -> FLUID_ICONS[0x02 | 0x08 | 0x20]         = e);
        register(FLUID_ + "fluid_00_1_1_11", e -> FLUID_ICONS[0x04 | 0x08 | 0x20]         = e);
        register(FLUID_ + "fluid_10_1_1_11", e -> FLUID_ICONS[0x01 | 0x04 | 0x08 | 0x20]  = e);
        register(FLUID_ + "fluid_11_1_1_11", e -> FLUID_ICONS[0x02 | 0x04 | 0x08 | 0x20]  = e);
        register(FLUID_ + "fluid_10_1_0_00", e -> FLUID_ICONS[0x01 | 0x04]                = e);
        register(FLUID_ + "fluid_11_1_0_00", e -> FLUID_ICONS[0x02 | 0x04]                = e);
        register(FLUID_ + "fluid_00_0_1_00", e -> FLUID_ICONS[0x08]                       = e);
        register(FLUID_ + "fluid_10_0_1_00", e -> FLUID_ICONS[0x01 | 0x08]                = e);
        //@formatter:on

        TEXTURES = new TextureAtlasSprite[][] { ITEM_ICONS, FLUID_ICONS };
    }

    private static void register(String sprite, Consumer<TextureAtlasSprite> onReady) {
        registrar.registerSprite(sprite, onReady);
    }

}
