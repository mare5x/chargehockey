package com.mare5x.chargehockey;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.assets.loaders.TextureAtlasLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ObjectMap;

public class ChargeHockeyGame extends Game {
	public SpriteBatch batch;
    public Skin skin;
    public TextureAtlas sprites;

    protected AssetManager manager;
    protected Screen menu_screen;

    private Sprite tmp_sprite;
//    final protected Screen level_screen;
//    final protected Screen editor_screen;
//    final protected Screen game_screen;

	@Override
	public void create () {
		batch = new SpriteBatch();

        manager = new AssetManager();
        FileHandleResolver resolver = new InternalFileHandleResolver();
        manager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
        manager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));

        FreetypeFontLoader.FreeTypeFontLoaderParameter font_param = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        font_param.fontFileName = "OpenSans-Regular.ttf";
        font_param.fontParameters.size = 16;  //(int)(Gdx.graphics.getDensity() * 16.0f);
        Gdx.app.log("font", String.format("font size: %d", font_param.fontParameters.size));

        manager.load("OpenSans-Regular.ttf", BitmapFont.class, font_param);
        manager.finishLoading();

        ObjectMap<String, Object> font_map = new ObjectMap<String, Object>();
        font_map.put("font", manager.get("OpenSans-Regular.ttf", BitmapFont.class));
        manager.load("skin.json", Skin.class, new SkinLoader.SkinParameter("ui.atlas", font_map));

        manager.load("sprites.atlas", TextureAtlas.class, new TextureAtlasLoader.TextureAtlasParameter());

        manager.finishLoading();

        skin = manager.get("skin.json", Skin.class);

        sprites = manager.get("sprites.atlas", TextureAtlas.class);
        tmp_sprite = sprites.createSprite("neg_blue64");
//        setScreen(menu_screen);
	}

    @Override
    public void render() {
        Gdx.gl20.glClearColor(1, 0, 0, 1);
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        tmp_sprite.draw(batch);
        skin.get("font", BitmapFont.class).draw(batch, "font", 100, 100);
        batch.end();
    }

    @Override
	public void dispose () {
        skin.remove("font", BitmapFont.class);
        manager.dispose();
        batch.dispose();

//        menu_screen.dispose();
	}
}
