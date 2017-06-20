package com.mare5x.chargehockey;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
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

    private final GameLogic game_logic;

    private final Stage game_stage, button_stage;
    private final OrthographicCamera camera;
    private final GameCameraController camera_controller;

    private final LevelFrameBuffer fbo;

    private final PlayButton play_button;

    private final InputMultiplexer multiplexer;

    GameScreen(final ChargeHockeyGame game, Level level) {
        this.game = game;

        camera = new OrthographicCamera();

        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();
        float edit_aspect_ratio = w / (h * 0.8f);
        game_stage = new Stage(new FillViewport(edit_aspect_ratio * ChargeHockeyGame.WORLD_HEIGHT, ChargeHockeyGame.WORLD_HEIGHT, camera), game.batch);
        camera.position.set(ChargeHockeyGame.WORLD_WIDTH / 2, ChargeHockeyGame.WORLD_HEIGHT / 2, 0);  // center camera
        camera.zoom = 0.8f;

        fbo = new LevelFrameBuffer(game, level);
        fbo.set_draw_grid_lines(false);
        fbo.set_draw_pucks(false);
        fbo.update(game.batch);

        game_logic = new GameLogic(game, game_stage, level, new GameLogic.ResultCallback() {
            @Override
            public void win() {
                toggle_playing();
            }

            @Override
            public void loss() {
                toggle_playing();
                game_logic.blink_collided_pucks();
            }
        });

        button_stage = new Stage(new StretchViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight() * 0.2f), game.batch);

        final Button menu_button = new Button(game.skin, "menu");
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
                toggle_playing(!game_logic.is_playing());  // from pause to play
            }
        });
        play_button.pad(10);

        Table button_table = new Table();
        button_table.setFillParent(true);
        button_table.setBackground(game.skin.getDrawable("pixels/px_black"));
        button_table.add(play_button).size(Value.percentHeight(0.5f, button_table)).uniform().pad(15);
        button_table.add(charge_pos_button).pad(15).uniform().fill();
        button_table.add(charge_neg_button).pad(15).uniform().fill();
        button_table.add(menu_button).pad(15).uniform().fill();

        button_stage.addActor(button_table);

        camera_controller = new GameCameraController(camera);
        InputAdapter back_key_processor = new InputAdapter() {  // same as menu_button
            @Override
            public boolean keyUp(int keycode) {
                if (keycode == Input.Keys.BACK) {
                    game.setScreen(new GameMenuScreen(game, GameScreen.this));
                }
                return true;
            }
        };
        multiplexer = new InputMultiplexer(game_stage, new GestureDetector(camera_controller), button_stage, back_key_processor);
    }

    private void toggle_playing() {
        toggle_playing(false);
    }

    private void toggle_playing(boolean update_background) {
        play_button.cycle_style();
        game_logic.set_playing(!game_logic.is_playing());

        // render if going from pause to playing
        if (update_background) {
            fbo.set_draw_pucks(!game_logic.is_playing());
            fbo.update(game.batch);
        }
        if (!game_logic.is_playing()) {
            // if not playing draw pucks on the background without clearing it, so the trace path remains
            fbo.draw_pucks(game.batch);
        }

        Gdx.graphics.setContinuousRendering(game_logic.is_playing());
        Gdx.graphics.requestRendering();
        camera_controller.set_rendering(game_logic.is_playing());
    }

    void restart_level() {
        game_logic.reset();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(multiplexer);
    }

    private void update_puck_trace_path() {
        if (!PuckActor.get_trace_path() || !game_logic.is_playing())
            return;

        // render on the fbo
        fbo.begin();

        fbo.set_projection_matrix(game.batch);
        game.batch.begin();

        for (PuckActor puck : game_logic.get_pucks()) {
            puck.draw_trace_path_history(game.batch);
        }

        game.batch.end();
        fbo.end();
    }

    @Override
    public void render(float delta) {
        Gdx.gl20.glClearColor(0.1f, 0.1f, 0.1f, 1);  // dark brownish color
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera_controller.update(delta);

        game_logic.update(delta);
        game_stage.act();

        // fbo_region contains the static level background and the dynamically added puck trace path points
        update_puck_trace_path();

        game_stage.getViewport().apply();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();
        game.batch.disableBlending();
        fbo.render(game.batch, 0, 0, ChargeHockeyGame.WORLD_WIDTH, ChargeHockeyGame.WORLD_HEIGHT);
        game.batch.enableBlending();
        game.batch.end();

        game_stage.draw();

        button_stage.getViewport().apply();
        button_stage.act();
        button_stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        game_stage.getViewport().setScreenBounds(0, (int) (height * 0.2f), width, (int) (height * 0.8f));

        button_stage.getViewport().setScreenBounds(0, 0, width, (int) (height * 0.2f));

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

    private class GameCameraController extends CameraController {
        final Vector2 tmp_coords = new Vector2();

        GameCameraController(OrthographicCamera camera) {
            super(camera);
        }

        @Override
        public boolean tap(float x, float y, int count, int button) {
            super.tap(x, y, count, button);

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
