package com.mare5x.chargehockey;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;


class GameScreen implements Screen {
    private final ChargeHockeyGame game;

    private final Level level;
    private final GameLogic game_logic;

    private final Stage game_stage, button_stage;
    private final OrthographicCamera camera;

    private final PlayButton play_button;

    private final InputMultiplexer multiplexer;

    private Sprite sprite;

    private static FrameBuffer fbo = null;
    private static TextureRegion fbo_region = null;

    GameScreen(final ChargeHockeyGame game, Level level) {
        this.game = game;
        this.level = level;

        camera = new OrthographicCamera();

        float edit_aspect_ratio = Gdx.graphics.getWidth() / (Gdx.graphics.getHeight() * 0.8f);
        game_stage = new Stage(new FillViewport(edit_aspect_ratio * ChargeHockeyGame.WORLD_HEIGHT, ChargeHockeyGame.WORLD_HEIGHT, camera), game.batch);
        camera.position.set(ChargeHockeyGame.WORLD_WIDTH / 2, ChargeHockeyGame.WORLD_HEIGHT / 2, 0);  // center camera
        camera.zoom = 0.9f;
        game_stage.setDebugAll(true);

        game_logic = new GameLogic(game, game_stage, level, this);

        button_stage = new Stage(new StretchViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight() * 0.2f), game.batch);
        button_stage.setDebugAll(true);

        Button menu_button = new Button(game.skin, "menu");
        menu_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new GameMenuScreen(game, GameScreen.this));
            }
        });
        menu_button.pad(10);

        Button charge_pos_button = new Button(new TextureRegionDrawable(game.sprites.findRegion("pos_red64")));
        charge_pos_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game_logic.add_charge(CHARGE.POSITIVE);
            }
        });
        charge_pos_button.pad(10);

        Button charge_neg_button = new Button(new TextureRegionDrawable(game.sprites.findRegion("neg_blue64")));
        charge_neg_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game_logic.add_charge(CHARGE.NEGATIVE);
            }
        });
        charge_neg_button.pad(10);

        play_button = new PlayButton();
        play_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toggle_playing();
            }
        });
        play_button.pad(10);

        Table button_table = new Table();
        button_table.setFillParent(true);
        button_table.setBackground(game.skin.getDrawable("px_black"));
        button_table.add(play_button).size(Value.percentHeight(0.5f, button_table)).uniform().pad(15);
        button_table.add(charge_pos_button).pad(15).uniform().fill();
        button_table.add(charge_neg_button).pad(15).uniform().fill();
        button_table.add(menu_button).pad(15).uniform().fill();

        button_stage.addActor(button_table);

        multiplexer = new InputMultiplexer(game_stage, new GestureDetector(new GameGestureAdapter(camera)), button_stage);
    }

    void toggle_playing() {
        play_button.cycle_style();
        game_logic.set_playing(!game_logic.is_playing());
        render_background_fbo();
    }

    void restart_level() {
        game_logic.reset();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(multiplexer);

        load_settings();
    }

    private void load_settings() {
        SettingsFile settings = new SettingsFile();

        PuckActor.set_draw_velocity(settings.getBoolean(SETTINGS_KEY.SHOW_VELOCITY_VECTOR));
        PuckActor.set_draw_acceleration(settings.getBoolean(SETTINGS_KEY.SHOW_ACCELERATION_VECTOR));
        PuckActor.set_trace_path(settings.getBoolean(SETTINGS_KEY.TRACE_PATH));
        GameLogic.set_game_speed(settings.getFloat(SETTINGS_KEY.GAME_SPEED));
    }

    private void render_background_fbo() {
        fbo.begin();

        Gdx.gl20.glClearColor(0, 0, 0, 1);
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        for (int row = 0; row < ChargeHockeyGame.WORLD_HEIGHT; row++) {
            for (int col = 0; col < ChargeHockeyGame.WORLD_WIDTH; col++) {
                GRID_ITEM item = level.get_grid_item(row, col);
                if (item != GRID_ITEM.NULL) {
                    sprite = level.get_item_sprite(item);
                    sprite.setPosition(col, row);
                    sprite.draw(game.batch);
                }
            }
        }
        game.batch.end();

        fbo.end();
    }

    private void update_puck_trace_path() {
        if (!PuckActor.get_trace_path() || !game_logic.is_playing())
            return;

        // render on the fbo
        fbo.begin();

        game_stage.getViewport().apply();
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        for (PuckActor puck : game_logic.get_pucks()) {
            puck.draw_trace_path_point(game.batch);
        }

        game.batch.end();
        fbo.end();
    }

    @Override
    public void render(float delta) {
        Gdx.gl20.glClearColor(0.1f, 0.1f, 0.1f, 1);  // dark brownish color
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game_stage.getViewport().apply();

        game_logic.update(delta);
        game_stage.act();

        // fbo_region contains the static level background and the dynamically added puck trace path points
//        game.batch.begin();

//        update_puck_trace_path();

        game_stage.getViewport().apply();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();
        game.batch.draw(fbo_region, 0, 0, ChargeHockeyGame.WORLD_WIDTH, ChargeHockeyGame.WORLD_HEIGHT);
        game.batch.end();

        game_stage.draw();

        button_stage.getViewport().apply();
        button_stage.act();
        button_stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        int w = width;
        int h = (int) (height * 0.8f);
        game_stage.getViewport().setScreenBounds(0, (int) (height * 0.2f), w, h);

        button_stage.getViewport().setScreenBounds(0, 0, width, (int) (height * 0.2f));

        if (fbo != null)
            fbo.dispose();
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, w, h, false);
        fbo_region = new TextureRegion(fbo.getColorBufferTexture());
//        fbo_region.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        fbo_region.flip(false, true);  // FBO uses lower left, TextureRegion uses upper-left

        render_background_fbo();
        Gdx.graphics.requestRendering();
    }

    @Override
    public void pause() {
        game_logic.save_charge_state();
    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
//        dispose();
        game_logic.save_charge_state();
        if (game_logic.is_playing())
            toggle_playing();
    }

    @Override
    public void dispose() {
        fbo.dispose();
        game_stage.dispose();
        button_stage.dispose();
    }

    private class GameGestureAdapter extends BaseGestureAdapter {
        final Vector2 tmp_coords = new Vector2();

        GameGestureAdapter(OrthographicCamera camera) {
            super(camera);
        }

        @Override
        public boolean tap(float x, float y, int count, int button) {
            game_stage.screenToStageCoordinates(tmp_coords.set(x, y));
            System.out.printf("%f, %f, %d, %d\n", tmp_coords.x, tmp_coords.y, count, button);

            return false;
        }
    }

    private class PlayButton extends Button {
        private boolean is_playing = false;

        private final ButtonStyle play_style = game.skin.get("play", ButtonStyle.class);
        private final ButtonStyle stop_style = game.skin.get("stop", ButtonStyle.class);

        PlayButton() {
            super();

            setStyle(play_style);
        }

        void cycle_style() {
            is_playing = !is_playing;

            if (is_playing)
                setStyle(stop_style);
            else
                setStyle(play_style);
        }
    }
}
