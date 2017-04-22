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
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ObjectMap;

public class ChargeHockeyGame extends Game {
    public static float DENSITY;
    public static final int WORLD_WIDTH = 64;
    public static final int WORLD_HEIGHT = 64;

	public SpriteBatch batch;
    public Skin skin;
    public TextureAtlas sprites;

    public AssetManager manager;

    public Screen menu_screen;
//    final protected Screen level_screen;
//    final protected Screen editor_screen;
//    final protected Screen game_screen;

	@Override
	public void create () {
        DENSITY = Gdx.graphics.getDensity();

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
        Gdx.app.log("font", String.format("size: %d", font_param.fontParameters.size));

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
