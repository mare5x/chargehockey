package com.mare5x.chargehockey.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Event;
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
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mare5x.chargehockey.ChargeHockeyGame;
import com.mare5x.chargehockey.actors.ChargeActor;
import com.mare5x.chargehockey.actors.ChargeActor.CHARGE;
import com.mare5x.chargehockey.actors.PuckActor;
import com.mare5x.chargehockey.actors.SymmetryToolActor;
import com.mare5x.chargehockey.level.Grid;
import com.mare5x.chargehockey.level.GridCache;
import com.mare5x.chargehockey.level.Level;
import com.mare5x.chargehockey.level.LevelFrameBuffer;
import com.mare5x.chargehockey.level.LevelSelectorScreen;
import com.mare5x.chargehockey.notifications.NoChargesNotification;
import com.mare5x.chargehockey.notifications.Notification;
import com.mare5x.chargehockey.settings.GameDefaults;
import com.mare5x.chargehockey.settings.SettingsFile;

import static com.mare5x.chargehockey.settings.GameDefaults.ACTOR_PAD;
import static com.mare5x.chargehockey.settings.GameDefaults.CELL_PAD;
import static com.mare5x.chargehockey.settings.GameDefaults.CHARGE_ZONE_ACTIVE_BG;
import static com.mare5x.chargehockey.settings.GameDefaults.CHARGE_ZONE_BG;
import static com.mare5x.chargehockey.settings.GameDefaults.CHARGE_ZONE_HEIGHT;
import static com.mare5x.chargehockey.settings.GameDefaults.IMAGE_BUTTON_SIZE;
import static com.mare5x.chargehockey.settings.GameDefaults.IMAGE_FONT_SIZE;
import static com.mare5x.chargehockey.settings.GameDefaults.MIN_BUTTON_HEIGHT;


// todo add undo button
public class GameScreen implements Screen {
    private enum WinDialogBUTTON {
        BACK, NEXT
    }

    // Helper DragListener for adding charges
    // Add a charge by dragging or by clicking on it to add it to the center
    // Propagate the events down to the newly added charge (game_stage) so it can be dragged
//    If click detection unnecessary:
//        charge_neg_button.addCaptureListener(new DragListener() {
//            @Override
//            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
//                if (!game_logic.is_playing()) {
//                    // x and y are in local button space coordinates
//                    Vector2 coords = new Vector2(event.getStageX(), event.getStageY());
//                    coords.y = Gdx.graphics.getHeight() - 1 - coords.y;  // (0,0) in top left corner
//                    game_stage.screenToStageCoordinates(coords);
//                    game_logic.add_charge(CHARGE.NEGATIVE, coords.x, coords.y);
//                }
//                event.stop();
//                return false;
//            }
//        });
    private class ChargeDragger extends DragListener {
        private boolean clicked = true;
        private Vector2 tmp_coords = new Vector2();
        private CHARGE charge_type;

        ChargeDragger(CHARGE type) {
            charge_type = type;
        }

        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            clicked = true;
            return super.touchDown(event, x, y, pointer, button);
        }

        @Override
        public void dragStart(InputEvent event, float x, float y, int pointer) {
            if (!game_logic.is_playing()) {
                // x and y are in local button space coordinates
                hud_stage.stageToScreenCoordinates(tmp_coords.set(event.getStageX(), event.getStageY()));
                game_stage.screenToStageCoordinates(tmp_coords);
                game_logic.place_charge(charge_type, tmp_coords.x, tmp_coords.y);

                hud_stage.stageToScreenCoordinates(tmp_coords.set(event.getStageX(), event.getStageY()));
                game_stage.touchDown((int)tmp_coords.x, (int)tmp_coords.y, pointer, getButton());
            }
            clicked = false;
        }

        @Override
        public void touchDragged(InputEvent event, float x, float y, int pointer) {
            super.touchDragged(event, x, y, pointer);

            hud_stage.stageToScreenCoordinates(tmp_coords.set(event.getStageX(), event.getStageY()));
            game_stage.touchDragged((int)tmp_coords.x, (int)tmp_coords.y, pointer);
        }

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
            super.touchUp(event, x, y, pointer, button);

            if (clicked) {
                if (!game_logic.is_playing())
                    game_logic.place_charge(charge_type);
            } else {
                hud_stage.stageToScreenCoordinates(tmp_coords.set(event.getStageX(), event.getStageY()));
                game_stage.touchUp((int)tmp_coords.x, (int)tmp_coords.y, pointer, button);
            }
        }
    }

    private final ChargeHockeyGame game;
    private final Level level;

    private final GameLogic game_logic;

    private boolean level_finished_changed = false;

    private final Stage game_stage, hud_stage;
    private final OrthographicCamera camera;
    private final CameraController camera_controller;

    private final LevelFrameBuffer fbo;
    private final GridCache grid_lines;

    private final SymmetryToolActor symmetry_tool;

    private Notification notification;

    private static boolean SHOW_GRID_LINES_SETTING = false;
    private static boolean SYMMETRY_TOOL_ENABLED_SETTING = false;

    private final WinDialog win_dialog;
    private final PlayButton play_button;

    private final InputMultiplexer multiplexer;

    public GameScreen(final ChargeHockeyGame game, final Level level) {
        this.game = game;
        this.level = level;

        camera = new OrthographicCamera();

        // the game_stage will span the whole screen (see resize())
        // ExtendViewport fits the world square on the screen and then extends the shorter dimension
        // to fill the whole screen
        game_stage = new Stage(new ExtendViewport(Grid.WORLD_WIDTH, Grid.WORLD_HEIGHT, camera), game.batch);
        camera.position.set(Grid.WORLD_WIDTH / 2, Grid.WORLD_HEIGHT / 2, 0);  // center camera
        camera.zoom = 0.8f;

        hud_stage = new Stage(new ScreenViewport(), game.batch);
        final Table button_table = new Table();

//        hud_stage.setDebugAll(true);
//        game_stage.setDebugAll(true);

        fbo = new LevelFrameBuffer(game, level);
        fbo.set_draw_pucks(false);
        fbo.update(game.batch);

        grid_lines = new GridCache(game);
        grid_lines.set_grid_line_alpha(0.8f);
        grid_lines.set_show_grid_lines(SHOW_GRID_LINES_SETTING);
        grid_lines.update(camera.zoom);

        symmetry_tool = new SymmetryToolActor(game);
        symmetry_tool.update_size(camera.zoom);
        symmetry_tool.set_enabled(SYMMETRY_TOOL_ENABLED_SETTING);
        game_stage.addActor(symmetry_tool);

        win_dialog = new WinDialog("WIN", game.skin);

        game_logic = new GameLogic(game, game_stage, level, new GameLogic.GameCallbacks() {
            @Override
            public void result_win() {
                if (!level.get_level_finished()) {
                    level.set_level_finished(true);
                    level_finished_changed = true;
                }

                toggle_playing();
                win_dialog.show(hud_stage);
            }

            @Override
            public void result_loss() {
                toggle_playing();
                game_logic.blink_collided_pucks();
            }

            @Override
            public void charge_zone_enter(ChargeActor charge) {
                button_table.setBackground(game.skin.getDrawable(CHARGE_ZONE_ACTIVE_BG));
            }

            @Override
            public void charge_zone_exit(ChargeActor charge) {
                button_table.setBackground(game.skin.getDrawable(CHARGE_ZONE_BG));
            }
        }, symmetry_tool);
        load_charge_state(Level.SAVE_TYPE.AUTO);
        game_logic.charge_state_changed();  // initialize tracking

        final Button menu_button = new Button(game.skin, "menu");
        menu_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new GameMenuScreen(game, GameScreen.this, level));
            }
        });
        menu_button.pad(ACTOR_PAD);

        Button symmetry_tool_button = new Button(game.skin, "symmetry_tool");
        symmetry_tool_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                symmetry_tool.set_enabled(!symmetry_tool.is_enabled());
                symmetry_tool.update_size(camera.zoom);
            }
        });
        symmetry_tool_button.pad(ACTOR_PAD);

        Button charge_pos_button = new Button(new TextureRegionDrawable(game.sprites.findRegion("charge_pos")));
        charge_pos_button.addListener(new ChargeDragger(CHARGE.POSITIVE));
        charge_pos_button.pad(ACTOR_PAD);

        Button charge_neg_button = new Button(new TextureRegionDrawable(game.sprites.findRegion("charge_neg")));
        charge_neg_button.addListener(new ChargeDragger(CHARGE.NEGATIVE));
        charge_neg_button.pad(ACTOR_PAD);

        play_button = new PlayButton();
        play_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toggle_playing(!game_logic.is_playing());  // from pause to play
            }
        });
        play_button.pad(ACTOR_PAD);

        Table hud_table = new Table();
        hud_table.setFillParent(true);

        button_table.setBackground(game.skin.getDrawable(CHARGE_ZONE_BG));
        button_table.defaults().size(IMAGE_BUTTON_SIZE).space(Value.percentWidth(0.125f, hud_table));
        button_table.add(play_button);
        button_table.add(charge_pos_button);
        button_table.add(charge_neg_button);

        hud_table.row().size(IMAGE_BUTTON_SIZE).pad(CELL_PAD).expandX();
        hud_table.add(symmetry_tool_button).left();
        hud_table.add(menu_button).right().row();
        hud_table.defaults().colspan(2);
        hud_table.add().expand().fill().row();
        hud_table.add(button_table).height(CHARGE_ZONE_HEIGHT).expandX().fill();

        hud_stage.addActor(hud_table);

        camera_controller = new GameCameraController(camera, game_stage);
        camera_controller.set_double_tap_zoom(true);
        InputAdapter back_key_processor = new InputAdapter() {  // same as menu_button
            @Override
            public boolean keyUp(int keycode) {
                if (keycode == Input.Keys.BACK) {
                    game.setScreen(new GameMenuScreen(game, GameScreen.this, level));
                }
                return true;
            }
        };

        // ignore all stage events when the game is playing. this prevents charges from being dragged around midgame.
        game_stage.addCaptureListener(new InputListener() {
            @Override
            public boolean handle(Event e) {
                if (game_logic.is_playing())
                    e.stop();
                return super.handle(e);
            }
        });
        multiplexer = new InputMultiplexer(hud_stage, game_stage, camera_controller.get_gesture_detector(), back_key_processor);
    }

    private void toggle_playing() {
        toggle_playing(false);
    }

    private void toggle_playing(boolean update_background) {
        if (!game_logic.has_charges()) {
            if (notification == null || !(notification instanceof NoChargesNotification))
                notification = new NoChargesNotification(game, hud_stage);
            notification.show(new Runnable() {
                @Override
                public void run() {
                    notification = null;
                }
            });
            return;
        }

        play_button.cycle_style();
        game_logic.set_playing(!game_logic.is_playing());

        // clear if going from pause to playing
        if (update_background) {
            fbo.update(game.batch);
        }

        Gdx.graphics.setContinuousRendering(game_logic.is_playing());
        Gdx.graphics.requestRendering();
        camera_controller.set_rendering(game_logic.is_playing());
    }

    private void save_changes() {
        save_charge_state(Level.SAVE_TYPE.AUTO);

        boolean symmetry_enabled = symmetry_tool.is_enabled();
        if (SYMMETRY_TOOL_ENABLED_SETTING != symmetry_enabled) {
            SYMMETRY_TOOL_ENABLED_SETTING = symmetry_enabled;
            SettingsFile.set_setting(SettingsFile.SETTINGS_KEY.GAME_SYMMETRY, SYMMETRY_TOOL_ENABLED_SETTING);
        }
    }

    void save_charge_state(Level.SAVE_TYPE save_type) {
        SymmetryToolActor.SymmetryToolState symmetry_tool_state = symmetry_tool.get_state();
        boolean symmetry_tool_changed = !symmetry_tool_state.equals(level.get_symmetry_tool_state());

        if (save_type == Level.SAVE_TYPE.QUICKSAVE || level_finished_changed || symmetry_tool_changed
                || !level.save_file_exists() || game_logic.charge_state_changed()) {
            level.set_symmetry_tool_state(symmetry_tool_state);
            level_finished_changed = false;
            level.write_save_file(save_type, game_logic.get_charges());
        }
    }

    boolean load_charge_state(Level.SAVE_TYPE save_type) {
        boolean success = game_logic.load_charge_state(save_type);

        SymmetryToolActor.SymmetryToolState symmetry_tool_state = level.get_symmetry_tool_state();
        if (symmetry_tool_state != null)
            symmetry_tool.set_state(symmetry_tool_state);

        if (success)  // the level was reset so reset the fbo
            fbo.update(game.batch);

        return success;
    }

    void restart_level() {
        game_logic.reset();
        fbo.update(game.batch);
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
    public void show() {
        Gdx.input.setInputProcessor(multiplexer);

        // check if any settings were changed
        game_logic.handle_charge_size_change();
        if (SHOW_GRID_LINES_SETTING != grid_lines.get_show_grid_lines()) {
            grid_lines.set_show_grid_lines(SHOW_GRID_LINES_SETTING);
            grid_lines.update(camera.zoom);
        }

        render(Gdx.graphics.getDeltaTime());
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

        grid_lines.set_projection_matrix(camera.combined);
        grid_lines.render();

        game.batch.begin();
        fbo.render(game.batch, 0, 0, Grid.WORLD_WIDTH, Grid.WORLD_HEIGHT);
        game.batch.end();

        game_stage.draw();

        hud_stage.getViewport().apply();
        hud_stage.act();
        hud_stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        GameDefaults.resize(width, height);

        game_stage.getViewport().update(width, height);
        hud_stage.getViewport().update(width, height, true);

        camera_controller.resize(game_stage.getViewport().getScreenWidth(), game_stage.getViewport().getScreenHeight());

        if (notification != null && notification.is_displayed())
            notification.resize();

        if (win_dialog.isVisible())
            win_dialog.resize();

        Gdx.graphics.requestRendering();
    }

    @Override
    public void pause() {
        save_changes();
    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        save_changes();
        if (game_logic.is_playing())
            toggle_playing();
    }

    @Override
    public void dispose() {
        save_changes();
        fbo.dispose();
        grid_lines.dispose();
        game_stage.dispose();
        hud_stage.dispose();
    }

    public static void set_grid_lines_setting(boolean value) {
        SHOW_GRID_LINES_SETTING = value;
    }

    public static void set_symmetry_setting(boolean value) {
        SYMMETRY_TOOL_ENABLED_SETTING = value;
    }

    private class GameCameraController extends CameraController {
        private float prev_zoom;

        GameCameraController(OrthographicCamera camera, Stage stage) {
            super(camera, stage);

            prev_zoom = camera.zoom;
        }

        @Override
        protected void on_zoom_change(float zoom) {
            if (Math.abs(zoom - prev_zoom) >= 0.1f) {
                prev_zoom = zoom;

                if (symmetry_tool.is_enabled())
                    symmetry_tool.update_size(zoom);

                if (SHOW_GRID_LINES_SETTING) {
                    int grid_line_spacing = GridCache.get_grid_line_spacing(zoom);
                    if (grid_lines.get_grid_line_spacing() != grid_line_spacing)
                        grid_lines.update(zoom, grid_line_spacing);
                }
            }
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
            setResizable(false);
            setMovable(false);

            pad(IMAGE_FONT_SIZE);

            getTitleTable().clear();  // hide the dumb title

            Image star_image = new Image(game.skin.getDrawable("star"));
            star_image.setScaling(Scaling.fit);

            Table content_table = getContentTable();
            content_table.add(new Label("LEVEL PASSED!", game.skin, "borderless")).row();
            content_table.add(star_image).prefSize(2 * IMAGE_BUTTON_SIZE).minSize(0).pad(CELL_PAD);

            Button back_button = new Button(skin, "back");
            back_button.pad(ACTOR_PAD);
            Button next_level_button = new Button(skin, "next");
            next_level_button.pad(ACTOR_PAD);

            getButtonTable().defaults().size(2 * MIN_BUTTON_HEIGHT, MIN_BUTTON_HEIGHT).padTop(CELL_PAD).space(CELL_PAD).expandX();
            button(back_button, WinDialogBUTTON.BACK);
            button(next_level_button, WinDialogBUTTON.NEXT);

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
        protected void result(Object object) {
            hide();
            if (object == WinDialogBUTTON.NEXT) {
                GameScreen.this.hide();
                dispose();
                game.setScreen(new LevelSelectorScreen(game, level));
            }
        }

        void resize() {
            pack();
            setPosition(Math.round((hud_stage.getWidth() - getWidth()) / 2), Math.round((hud_stage.getHeight() - getHeight()) / 2));
        }
    }
}
