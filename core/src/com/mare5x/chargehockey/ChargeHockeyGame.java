package com.mare5x.chargehockey;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.assets.loaders.TextureAtlasLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.ObjectMap;
import com.mare5x.chargehockey.editor.FilePicker;
import com.mare5x.chargehockey.editor.PermissionTools;
import com.mare5x.chargehockey.level.GridSprites;
import com.mare5x.chargehockey.menus.MenuScreen;
import com.mare5x.chargehockey.settings.SettingsFile;

import java.util.Locale;

abstract public class ChargeHockeyGame extends Game {
    public static float DENSITY;
    public static final float FONT_SIZE = 24;  // dp units
    public static final int WORLD_WIDTH = 64;
    public static final int WORLD_HEIGHT = 64;

    public static final Rectangle WORLD_RECT = new Rectangle(0, 0, WORLD_WIDTH, WORLD_HEIGHT);

    private static final boolean LOG_FPS = true;
    private static final FPSLogger fps_logger = new FPSLogger();

	public SpriteBatch batch;
    public Skin skin;
    public TextureAtlas sprites;
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
        font_param.fontParameters.size = (int)(DENSITY * FONT_SIZE);
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

        // manually flip the back button arrow and make a new button out of it
        Sprite next_up_sprite = new Sprite(skin.getRegion("back_up"));
        next_up_sprite.flip(true, false);
        Sprite next_down_sprite = new Sprite(skin.getRegion("back_down"));
        next_down_sprite.flip(true, false);
        Button.ButtonStyle next_button = new Button.ButtonStyle(new SpriteDrawable(next_up_sprite), new SpriteDrawable(next_down_sprite), null);
        skin.add("next", next_button, Button.ButtonStyle.class);

        sprites = manager.get("sprites.atlas", TextureAtlas.class);

        grid_sprites = new GridSprites(this);

        SettingsFile.initialize();

        menu_screen = new MenuScreen(this);
        setScreen(menu_screen);
	}

    @Override
    public void render() {
	    if (LOG_FPS)
	        fps_logger.log();

        super.render();
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
