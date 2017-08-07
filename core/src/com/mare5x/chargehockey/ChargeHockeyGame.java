package com.mare5x.chargehockey;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.assets.loaders.TextureAtlasLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ObjectMap;

import com.mare5x.chargehockey.settings.SettingsFile;
import com.mare5x.chargehockey.editor.FilePicker;

import java.util.Locale;

abstract public class ChargeHockeyGame extends Game {
    public static float DENSITY;
    public static final int WORLD_WIDTH = 64;
    public static final int WORLD_HEIGHT = 64;

    public static final Rectangle WORLD_RECT = new Rectangle(0, 0, WORLD_WIDTH, WORLD_HEIGHT);

	public SpriteBatch batch;
    public Skin skin;
    public TextureAtlas sprites;
    public Screen menu_screen;

    private AssetManager manager;

    // platform specific abstract methods

    public abstract PermissionTools get_permission_tools();

    public abstract FilePicker get_file_picker();

    public abstract FilePicker get_file_picker(FilePicker.FileFilter filter);

	@Override
	public void create () {
        DENSITY = Gdx.graphics.getDensity();

        Gdx.input.setCatchBackKey(true);

        Gdx.graphics.setContinuousRendering(false);
        Gdx.graphics.requestRendering();

		batch = new SpriteBatch();

        manager = new AssetManager();
        FileHandleResolver resolver = new InternalFileHandleResolver();
        manager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
        manager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));

        FreetypeFontLoader.FreeTypeFontLoaderParameter font_param = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        font_param.fontFileName = "OpenSans-Regular.ttf";
        font_param.fontParameters.size = (int)(DENSITY * 24.0f);
        font_param.fontParameters.borderWidth = 0.5f;  // make the font bold
        Gdx.app.log("font", String.format(Locale.US, "size: %d", font_param.fontParameters.size));

        manager.load("OpenSans-Regular.ttf", BitmapFont.class, font_param);
        manager.finishLoading();

        BitmapFont font = manager.get("OpenSans-Regular.ttf", BitmapFont.class);
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        ObjectMap<String, Object> font_map = new ObjectMap<String, Object>();
        font_map.put("font", font);
        manager.load("skin.json", Skin.class, new SkinLoader.SkinParameter("ui.atlas", font_map));

        manager.load("sprites.atlas", TextureAtlas.class, new TextureAtlasLoader.TextureAtlasParameter());

        manager.finishLoading();

        skin = manager.get("skin.json", Skin.class);

        sprites = manager.get("sprites.atlas", TextureAtlas.class);

        SettingsFile.initialize();

        menu_screen = new MenuScreen(this);
        setScreen(menu_screen);
	}

    @Override
	public void dispose () {
        menu_screen.dispose();

        skin.remove("font", BitmapFont.class);
        manager.dispose();
        batch.dispose();
	}
}
