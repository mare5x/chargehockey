package com.mare5x.chargehockey;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.ObjectMap;
import com.mare5x.chargehockey.editor.FilePicker;
import com.mare5x.chargehockey.editor.PermissionTools;
import com.mare5x.chargehockey.level.GridSprites;
import com.mare5x.chargehockey.menus.MenuScreen;
import com.mare5x.chargehockey.settings.GameDefaults;
import com.mare5x.chargehockey.settings.SettingsFile;

import java.util.Locale;

abstract public class ChargeHockeyGame extends Game {
	public SpriteBatch batch;
    public Skin skin;
    public MenuScreen menu_screen;

    private AssetManager manager;

    // FBO helper
    public GridSprites grid_sprites;

    // platform specific abstract methods
    public abstract PermissionTools get_permission_tools();

    public abstract FilePicker get_file_picker();

    public abstract FilePicker get_file_picker(FilePicker.FileFilter filter);

	@Override
	public void create () {
        Gdx.app.setLogLevel(0);

        Gdx.input.setCatchBackKey(true);

        Gdx.graphics.setContinuousRendering(false);
        Gdx.graphics.requestRendering();

		batch = new SpriteBatch();

		// DO NOT USE STATIC VARIABLES IN ANDROID! https://github.com/libgdx/libgdx/wiki/Managing-your-assets http://bitiotic.com/blog/2013/05/23/libgdx-and-android-application-lifecycle/
        manager = new AssetManager();
        FileHandleResolver resolver = new InternalFileHandleResolver();
        manager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
        manager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));

        FreetypeFontLoader.FreeTypeFontLoaderParameter font_param = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        font_param.fontFileName = "OpenSans-Regular.ttf";
        font_param.fontParameters.size = (int)(GameDefaults.DENSITY * GameDefaults.FONT_SIZE);
        font_param.fontParameters.borderWidth = 0.5f;  // make the font bold
        Gdx.app.log("font", String.format(Locale.US, "size: %d", font_param.fontParameters.size));

        manager.load("OpenSans-Regular.ttf", BitmapFont.class, font_param);
        manager.finishLoading();

        BitmapFont font = manager.get("OpenSans-Regular.ttf", BitmapFont.class);
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        ObjectMap<String, Object> font_map = new ObjectMap<String, Object>();
        font_map.put("font", font);
        manager.load("skin.json", Skin.class, new SkinLoader.SkinParameter("texturepack_linear.atlas", font_map));
        manager.load("texturepack_nearest.atlas", TextureAtlas.class);

        manager.finishLoading();

        skin = manager.get("skin.json", Skin.class);

        // manually flip the back button arrow and make a new button out of it
        Sprite next_up_sprite = create_sprite("ui_back_up");
        next_up_sprite.flip(true, false);
        Sprite next_down_sprite = create_sprite("ui_back_down");
        next_down_sprite.flip(true, false);
        Button.ButtonStyle next_button = new Button.ButtonStyle(new SpriteDrawable(next_up_sprite), new SpriteDrawable(next_down_sprite), null);
        skin.add("next", next_button, Button.ButtonStyle.class);

        skin.addRegions(manager.get("texturepack_nearest.atlas", TextureAtlas.class));

        grid_sprites = new GridSprites(this);

        SettingsFile.initialize();

        menu_screen = new MenuScreen(this);
        setScreen(menu_screen);
	}

	public Sprite create_sprite(String path) {
	    return new Sprite(skin.getRegion(path));
    }

    @Override
	public void dispose () {
        menu_screen.dispose();

        skin.remove("font", BitmapFont.class);
        manager.dispose();
        batch.dispose();
	}

	/** Safely exit the game. ... or not. */
	public void exit() {
        Gdx.app.exit();
    }
}
