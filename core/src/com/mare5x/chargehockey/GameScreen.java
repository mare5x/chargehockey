package com.mare5x.chargehockey;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;


class GameScreen implements Screen {
    private enum WinDialogBUTTON {
        BACK, SHARE, NEXT
    }

    private final ChargeHockeyGame game;
    private final Level level;

    private final GameLogic game_logic;

    private final Stage game_stage, hud_stage;
    private final OrthographicCamera camera;
    private final GameCameraController camera_controller;

    private final LevelFrameBuffer fbo;

    private final PlayButton play_button;

    private final InputMultiplexer multiplexer;

    GameScreen(final ChargeHockeyGame game, final Level level) {
        this.game = game;
        this.level = level;

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

        final WinDialog win_dialog = new WinDialog("WIN", game.skin);

        game_logic = new GameLogic(game, game_stage, level, new GameLogic.ResultCallback() {
            @Override
            public void win() {
                toggle_playing();
                win_dialog.show(hud_stage);
            }

            @Override
            public void loss() {
                toggle_playing();
                game_logic.blink_collided_pucks();
            }
        });

        hud_stage = new Stage(new StretchViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()), game.batch);

        final Button menu_button = new Button(game.skin, "menu");
        menu_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new GameMenuScreen(game, GameScreen.this, level));
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

        Table hud_table = new Table();
        hud_table.setFillParent(true);

        Table button_table = new Table();
        button_table.setBackground(game.skin.getDrawable("pixels/px_black"));
        button_table.add(play_button).size(Value.percentHeight(0.5f, button_table)).uniform().pad(15);
        button_table.add(charge_pos_button).pad(15).uniform().fill();
        button_table.add(charge_neg_button).pad(15).uniform().fill();

        hud_table.add(menu_button).pad(15).expandX().right().size(Value.percentWidth(0.15f, hud_table)).row();
        hud_table.add().expand().fill().row();
        hud_table.add(button_table).height(Value.percentHeight(0.2f, hud_table)).expandX().fill();

        hud_stage.addActor(hud_table);

        camera_controller = new GameCameraController(camera, game_stage);
        InputAdapter back_key_processor = new InputAdapter() {  // same as menu_button
            @Override
            public boolean keyUp(int keycode) {
                if (keycode == Input.Keys.BACK) {
                    game.setScreen(new GameMenuScreen(game, GameScreen.this, level));
                }
                return true;
            }
        };
        multiplexer = new InputMultiplexer(hud_stage, game_stage, new GestureDetector(camera_controller), back_key_processor);
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
        fbo.update(game.batch);
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

        hud_stage.getViewport().apply();
        hud_stage.act();
        hud_stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        game_stage.getViewport().setScreenBounds(0, (int) (height * 0.2f), width, (int) (height * 0.8f));

        hud_stage.getViewport().setScreenBounds(0, 0, width, height);

        camera_controller.resize(game_stage.getViewport().getScreenWidth(), game_stage.getViewport().getScreenHeight());

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
        game_logic.save_charge_state();
        if (game_logic.is_playing())
            toggle_playing();
    }

    @Override
    public void dispose() {
        fbo.dispose();
        game_stage.dispose();
        hud_stage.dispose();
    }

    private class GameCameraController extends CameraController {
        GameCameraController(OrthographicCamera camera, Stage stage) {
            super(camera, stage);

            set_double_tap_zoom(true);
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

    private class WinDialog extends Dialog {
        WinDialog(String title, Skin skin) {
            super(title, skin);

            setModal(true);
            setMovable(false);

            pad(15 * ChargeHockeyGame.DENSITY);

            getTitleTable().clear();  // hide the dumb title

            Table content_table = getContentTable();

            Label level_passed_label = new Label("LEVEL PASSED!", game.skin, "borderless");
            content_table.add(level_passed_label).padBottom(10).row();
            content_table.add(new Image(game.skin.getDrawable("star"))).size(percent_width(0.3f)).pad(10);

            Table button_table = getButtonTable();

            Button back_button = new Button(skin, "back");
            back_button.pad(10);

            button_table.add(back_button).pad(15).size(percent_width(0.2f), percent_width(0.1f)).padRight(30 * ChargeHockeyGame.DENSITY);
            setObject(back_button, WinDialogBUTTON.BACK);

//            TextButton next_level_button = new TextButton("NEXT LEVEL", skin);
            Button next_level_button = new Button(skin, "forward");
            button_table.add(next_level_button).pad(15).size(percent_width(0.2f), percent_width(0.1f));
            setObject(next_level_button, WinDialogBUTTON.NEXT);

            button_table.row();

//            TextButton share_button = new TextButton("SHARE", skin);
//            Button share_button = new Button(skin, "share");
//            button_table.add(share_button).pad(15).size(percent_width(0.1f)).colspan(2).expandX().center();
//            setObject(share_button, WinDialogBUTTON.SHARE);

            addListener(new InputListener() {
                @Override
                public boolean keyUp(InputEvent event, int keycode) {
                    if (keycode == Input.Keys.BACK) {
                        result(WinDialogBUTTON.BACK);
                    }
                    return true;
                }
            });
        }

        @Override
        public Dialog show(Stage stage) {
            super.show(stage);

            return this;
        }

        @Override
        protected void result(Object object) {
            hide();

            if (object == WinDialogBUTTON.BACK) {
                Gdx.app.log("WinDialog", "BACK");
            } else if (object == WinDialogBUTTON.SHARE) {
                Gdx.app.log("WinDialog", "SHARE");
            } else if (object == WinDialogBUTTON.NEXT) {
                Gdx.app.log("WinDialog", "NEXT");

                dispose();
                game.setScreen(new LevelSelectorScreen(game, level.get_type()));
            }
        }

        private Value percent_width(final float percent) {
            return new Value() {
                @Override
                public float get(Actor context) {
                    return percent * hud_stage.getWidth();
                }
            };
        }

//        @Override
//        public float getPrefWidth() {
//            return hud_stage.getWidth() * 0.75f;
//        }
//
//        @Override
//        public float getPrefHeight() {
//            return hud_stage.getHeight() * 0.5f;
//        }
    }
}
