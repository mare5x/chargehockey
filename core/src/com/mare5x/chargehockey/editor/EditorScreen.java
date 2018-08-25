package com.mare5x.chargehockey.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mare5x.chargehockey.ChargeHockeyGame;
import com.mare5x.chargehockey.actors.ChargeActor;
import com.mare5x.chargehockey.actors.ChargeActor.CHARGE;
import com.mare5x.chargehockey.actors.SymmetryToolActor;
import com.mare5x.chargehockey.game.CameraController;
import com.mare5x.chargehockey.level.Grid;
import com.mare5x.chargehockey.level.Grid.GRID_ITEM;
import com.mare5x.chargehockey.level.GridCache;
import com.mare5x.chargehockey.level.Level;
import com.mare5x.chargehockey.level.LevelFrameBuffer;
import com.mare5x.chargehockey.notifications.EditorPaintTipNotification;
import com.mare5x.chargehockey.notifications.Notification;
import com.mare5x.chargehockey.settings.GameDefaults;
import com.mare5x.chargehockey.settings.SettingsFile;
import com.mare5x.chargehockey.settings.SettingsFile.SETTINGS_KEY;

import static com.mare5x.chargehockey.settings.GameDefaults.ACTOR_PAD;
import static com.mare5x.chargehockey.settings.GameDefaults.CELL_PAD;
import static com.mare5x.chargehockey.settings.GameDefaults.CHARGE_ZONE_ACTIVE_BG;
import static com.mare5x.chargehockey.settings.GameDefaults.CHARGE_ZONE_BG;
import static com.mare5x.chargehockey.settings.GameDefaults.CHARGE_ZONE_HEIGHT;
import static com.mare5x.chargehockey.settings.GameDefaults.IMAGE_BUTTON_SIZE;

public class EditorScreen implements Screen {
    private final ChargeHockeyGame game;

    private final InputMultiplexer multiplexer;

    private final Stage edit_stage, hud_stage;
    private final OrthographicCamera camera;  // camera of edit_stage
    private final EditCameraController camera_controller;

    private Notification notification;

    private final LevelFrameBuffer fbo;
    private final GridCache grid_lines;
    private final SymmetryToolActor symmetry_tool;

    private Level level;

    private static boolean SHOW_GRID_LINES_SETTING = true;
    private static boolean SYMMETRY_TOOL_ENABLED_SETTING = false;

    private boolean show_grid;
    private boolean level_changed = false;

    private final GridItemSelectorButton grid_item_button;
    private final Button puck_button;
    private final EditIcon edit_icon;

    private Array<ChargeActor> puck_actors;

    private final Vector2 tmp_v = new Vector2();

    private ChargeActor.ChargeDragAreaHelper charge_drag_area_helper;

    private final Table button_table = new Table();

    // callback function for ChargeActor pucks
    private final ChargeActor.DragCallback drag_callback = new ChargeActor.DragCallback() {
        @Override
        public void out_of_bounds(ChargeActor charge, boolean dragged) {
            ChargeActor partner = charge.get_partner();
            remove_puck(charge);
            if (partner != null && ((dragged && symmetry_tool.is_enabled()) || partner.check_out_of_world()))
                remove_puck(partner);
        }

        @Override
        public void drag_started(ChargeActor charge) {
            level_changed = true;
        }

        @Override
        public void drag(ChargeActor charge) {
            float x = charge.get_x();
            float y = charge.get_y();

            float round_x = MathUtils.round(x);
            float round_y = MathUtils.round(y);

            if (Math.abs(x - round_x) < 0.3f * Math.max(0.5f, camera.zoom))
                x = round_x;
            if (Math.abs(y - round_y) < 0.3f * Math.max(0.5f, camera.zoom))
                y = round_y;

            charge.set_position(x, y);

            if (symmetry_tool.is_enabled()) {
                ChargeActor partner = charge.get_partner();
                if (partner != null) {
                    symmetry_tool.get_symmetrical_pos(tmp_v.set(x, y));
                    partner.set_position(tmp_v.x, tmp_v.y);
                }
            }
        }

        @Override
        public void enter_charge_zone(ChargeActor charge) {
            button_table.setBackground(game.skin.getDrawable(CHARGE_ZONE_ACTIVE_BG));
        }

        @Override
        public void exit_charge_zone(ChargeActor charge) {
            button_table.setBackground(game.skin.getDrawable(CHARGE_ZONE_BG));
        }

        @Override
        public void enter_drag_area(ChargeActor charge) {
            charge_drag_area_helper.enter_drag_area(charge);
        }

        @Override
        public void exit_drag_area(ChargeActor charge) {
            charge_drag_area_helper.exit_drag_area();
        }
    };

    public EditorScreen(final ChargeHockeyGame game, Level level) {
        this.game = game;
        this.level = level;

        camera = new OrthographicCamera();

        edit_stage = new Stage(new ExtendViewport(Grid.WORLD_WIDTH, Grid.WORLD_HEIGHT, camera), game.batch);
        camera.position.set(Grid.WORLD_WIDTH / 2, Grid.WORLD_HEIGHT / 2, 0);  // center camera
        camera.zoom = 0.8f;

        hud_stage = new Stage(new ScreenViewport(), game.batch);

//        hud_stage.setDebugAll(true);
//        edit_stage.setDebugAll(true);

        fbo = new LevelFrameBuffer(game, level);
        fbo.set_draw_pucks(false);
        fbo.update(game.batch);
        
        grid_lines = new GridCache(game);
        grid_lines.set_grid_line_alpha(1);
        grid_lines.set_show_grid_lines(SHOW_GRID_LINES_SETTING);
        grid_lines.update(camera.zoom);

        symmetry_tool = new SymmetryToolActor(game);
        symmetry_tool.update_size(camera.zoom);
        symmetry_tool.set_enabled(SYMMETRY_TOOL_ENABLED_SETTING);
        edit_stage.addActor(symmetry_tool);

        charge_drag_area_helper = new ChargeActor.ChargeDragAreaHelper(symmetry_tool);

        // add interactive pucks from the stored puck positions
        puck_actors = new Array<ChargeActor>(level.get_puck_states().size * 2);
        for (ChargeActor.ChargeState puck_state: level.get_puck_states()) {
            ChargeActor puck1 = add_puck(puck_state.x, puck_state.y);
            if (puck_state.partner != null) {
                ChargeActor puck2 = add_puck(puck_state.partner.x, puck_state.partner.y);

                puck1.set_partner(puck2);
                puck2.set_partner(puck1);
            }
        }

        Button menu_button = new Button(game.skin, "menu");
        menu_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new EditorSubScreen(game, EditorScreen.this));
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

        grid_item_button = new GridItemSelectorButton();
        grid_item_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                grid_item_button.cycle_style();
                puck_button.setChecked(false);
            }
        });
        grid_item_button.pad(ACTOR_PAD);

        final Button show_grid_button = new Button(game.skin, "grid");
        show_grid_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                show_grid = show_grid_button.isChecked();
                grid_lines.set_show_grid_lines(show_grid);
                grid_lines.update(camera.zoom);
            }
        });
        show_grid = SHOW_GRID_LINES_SETTING;
        show_grid_button.setChecked(show_grid);
        show_grid_button.pad(ACTOR_PAD);

        puck_button = new Button(new TextureRegionDrawable(game.sprites.findRegion("puck")));
        // Helper DragListener for adding pucks with the puck button (see GameScreen for ChargeDragger)
        // Add a puck by dragging or by clicking on it to add it to the center
        // Propagate the events down to the newly added puck (edit_stage) so it can be dragged
        puck_button.addListener(new DragListener() {
            private boolean clicked = true;
            private Vector2 tmp_coords = new Vector2();

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                clicked = true;
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void dragStart(InputEvent event, float x, float y, int pointer) {
                // x and y are in local button space coordinates
                hud_stage.stageToScreenCoordinates(tmp_coords.set(event.getStageX(), event.getStageY()));
                edit_stage.screenToStageCoordinates(tmp_coords);
                place_puck(tmp_coords.x, tmp_coords.y, true);

                hud_stage.stageToScreenCoordinates(tmp_coords.set(event.getStageX(), event.getStageY()));
                edit_stage.touchDown((int)tmp_coords.x, (int)tmp_coords.y, pointer, getButton());

                clicked = false;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                super.touchDragged(event, x, y, pointer);

                hud_stage.stageToScreenCoordinates(tmp_coords.set(event.getStageX(), event.getStageY()));
                edit_stage.touchDragged((int)tmp_coords.x, (int)tmp_coords.y, pointer);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);

                if (clicked) {
                    place_puck();
                } else {
                    hud_stage.stageToScreenCoordinates(tmp_coords.set(event.getStageX(), event.getStageY()));
                    edit_stage.touchUp((int)tmp_coords.x, (int)tmp_coords.y, pointer, button);
                }
            }
        });
        puck_button.pad(ACTOR_PAD);

        edit_icon = new EditIcon();

        Table hud_table = new Table();
        hud_table.setFillParent(true);

        button_table.setBackground(game.skin.getDrawable(CHARGE_ZONE_BG));
        button_table.defaults().size(IMAGE_BUTTON_SIZE).space(Value.percentWidth(0.125f, hud_table)).expandX();
        button_table.add(grid_item_button).left();
        button_table.add(puck_button).right();
        button_table.pad(0, CELL_PAD, 0, CELL_PAD);

        hud_table.row().size(IMAGE_BUTTON_SIZE).pad(CELL_PAD).expandX();
        hud_table.add(symmetry_tool_button).left();
        hud_table.add(edit_icon).center();
        hud_table.add(menu_button).right().row();

        hud_table.row().size(IMAGE_BUTTON_SIZE).pad(CELL_PAD);
        hud_table.add(show_grid_button).colspan(3).expandX().right().row();

        hud_table.defaults().colspan(3);
        hud_table.add().expand().fill().row();
        hud_table.add(button_table).height(CHARGE_ZONE_HEIGHT).expandX().fill();

        hud_stage.addActor(hud_table);

        camera_controller = new EditCameraController(camera, edit_stage);
        InputAdapter back_key_processor = new InputAdapter() {  // same as menu_button
            @Override
            public boolean keyUp(int keycode) {
                if (keycode == Input.Keys.BACK) {
                    game.setScreen(new EditorSubScreen(game, EditorScreen.this));
                }
                return true;
            }
        };
        multiplexer = new InputMultiplexer(hud_stage, edit_stage, camera_controller.get_gesture_detector(), back_key_processor);
    }

    private ChargeActor add_puck(float x, float y) {
        ChargeActor puck = new ChargeActor(game, CHARGE.PUCK, drag_callback, symmetry_tool);
        puck.set_position(x, y);
        edit_stage.addActor(puck);
        puck_actors.add(puck);

        level_changed = true;

        return puck;
    }

    /* Add a puck to the center of the screen. */
    private void place_puck() {
        place_puck(edit_stage.getCamera().position.x, edit_stage.getCamera().position.y, false);
    }

    /** Place a puck at the given position, keeping the symmetry tool and dragging in mind.
     *  Dragging determines whether to perform out of bounds checking now or when dragging is finished. */
    private void place_puck(float x, float y, boolean dragged) {
        ChargeActor puck1 = add_puck(x, y);

        if (symmetry_tool.is_enabled()) {
            symmetry_tool.get_symmetrical_pos(tmp_v.set(x, y));

            ChargeActor puck2 = add_puck(tmp_v.x, tmp_v.y);

            puck1.set_partner(puck2);
            puck2.set_partner(puck1);

            if (!dragged && puck2.check_out_of_world())
                remove_puck(puck2);
        }

        if (!dragged && puck1.check_out_of_world())
            remove_puck(puck1);
    }

    private void remove_puck(ChargeActor puck) {
        ChargeActor partner = puck.get_partner();
        if (partner != null) {
            partner.set_partner(null);
            puck.set_partner(null);
        }

        puck_actors.removeValue(puck, true);
        puck.clear();
        puck.remove();

        level_changed = true;
    }

    /** Places a tile at the given position, taking the symmetry tool into account. */
    private void place_tile(float x, float y, GRID_ITEM item) {
        int row1 = (int) y;
        int col1 = (int) x;

        int row2 = row1;
        int col2 = col1;
        if (symmetry_tool.is_enabled()) {
            symmetry_tool.get_symmetrical_pos(tmp_v.set(x, y));
            row2 = (int) tmp_v.y;
            col2 = (int) tmp_v.x;
        }

        // only update the fbo if a new tile was just placed
        if (level.get_grid_item(row1, col1) != item || level.get_grid_item(row2, col2) != item) {
            level.set_item(row1, col1, item);
            level.set_item(row2, col2, item);

            fbo.update(game.batch);

            level_changed = true;
        }
    }

    private void save_changes() {
        if (level_changed) {
            level_changed = false;

            level.set_level_finished(false);  // reset the flag
            level.save_level(puck_actors);
            level.write_save_header();  // reset save file header
        }
        if (SHOW_GRID_LINES_SETTING != show_grid || SYMMETRY_TOOL_ENABLED_SETTING != symmetry_tool.is_enabled()) {
            SHOW_GRID_LINES_SETTING = show_grid;
            SYMMETRY_TOOL_ENABLED_SETTING = symmetry_tool.is_enabled();

            SettingsFile settings = new SettingsFile();
            settings.put(SETTINGS_KEY.EDITOR_GRID_LINES, SHOW_GRID_LINES_SETTING);
            settings.put(SETTINGS_KEY.EDITOR_SYMMETRY, SYMMETRY_TOOL_ENABLED_SETTING);
            settings.save();
        }
    }

    void clear_level() {
        level.clear_grid();

        for (ChargeActor puck : puck_actors) {
            puck.remove();
            puck.clear();
        }
        puck_actors.clear();

        fbo.update(game.batch);

        level_changed = true;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(multiplexer);

        camera_controller.set_rendering(false);
        Gdx.graphics.setContinuousRendering(false);
        Gdx.graphics.requestRendering();
    }

    @Override
    public void render(float delta) {
        Gdx.gl20.glClearColor(0.1f, 0.1f, 0.1f, 1);  // dark brownish color
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera_controller.update(delta);

        charge_drag_area_helper.update(delta);

        edit_stage.getViewport().apply();
        game.batch.setProjectionMatrix(camera.combined);

        grid_lines.set_projection_matrix(camera.combined);
        grid_lines.render();

        game.batch.begin();
        fbo.render(game.batch, 0, 0, Grid.WORLD_WIDTH, Grid.WORLD_HEIGHT);
        game.batch.end();

        edit_stage.act();
        edit_stage.draw();

        hud_stage.getViewport().apply();
        hud_stage.act();
        hud_stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        GameDefaults.resize(width, height);

        edit_stage.getViewport().update(width, height);
        hud_stage.getViewport().update(width, height, true);

        camera_controller.resize(edit_stage.getViewport().getScreenWidth(), edit_stage.getViewport().getScreenHeight());

        if (notification != null && notification.is_displayed())
            notification.resize();

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
        Gdx.graphics.setContinuousRendering(false);
    }

    @Override
    public void dispose() {
        save_changes();
        fbo.dispose();
        grid_lines.dispose();
        edit_stage.dispose();
        hud_stage.dispose();
    }

    Level get_level() {
        return level;
    }

    void show_paint_tip() {
        if (notification == null || !(notification instanceof EditorPaintTipNotification))
            notification = new EditorPaintTipNotification(game, hud_stage);
        notification.show(2.5f, new Runnable() {
            @Override
            public void run() {
                notification = null;
            }
        });
    }

    public static void set_grid_lines_setting(boolean value) {
        SHOW_GRID_LINES_SETTING = value;
    }

    public static void set_symmetry_setting(boolean value) {
        SYMMETRY_TOOL_ENABLED_SETTING = value;
    }

    private class EditCameraController extends CameraController {
        private float prev_zoom;

        EditCameraController(OrthographicCamera camera, Stage stage) {
            super(camera, stage);

            prev_zoom = camera.zoom;
        }

        @Override
        protected boolean on_tap(float x, float y, int count, int button) {
            // ignore taps outside of edit_stage's camera and outside the world
            if (!point_in_view(x, y) || !Grid.WORLD_RECT.contains(tmp_v.set(x, y))) {
                return true;
            }

            super.on_tap(x, y, count, button);

            // finish moving
            if (is_moving()) return true;

            place_tile(x, y, grid_item_button.get_selected_item());

            return true;
        }

        @Override
        protected void on_zoom_change(float zoom) {
            if (Math.abs(zoom - prev_zoom) >= 0.1f) {
                prev_zoom = zoom;

                if (symmetry_tool.is_enabled())
                    symmetry_tool.update_size(zoom);

                if (show_grid) {
                    int grid_line_spacing = GridCache.get_grid_line_spacing(zoom);
                    if (grid_lines.get_grid_line_spacing() != grid_line_spacing)
                        grid_lines.update(zoom, grid_line_spacing);
                }
            }
        }

        @Override
        protected void on_long_press_start() {
            super.on_long_press_start();
            edit_icon.show_on();
        }

        @Override
        protected void on_long_press_held(float x, float y) {
            place_tile(x, y, grid_item_button.get_selected_item());
        }

        @Override
        protected void on_long_press_end() {
            super.on_long_press_end();
            edit_icon.show_off();
        }
    }

    private class GridItemSelectorButton extends Button {
        private int current_item_idx = GRID_ITEM.WALL.ordinal();  // start = wall index
        private ObjectMap<GRID_ITEM, ButtonStyle> style_table;

        GridItemSelectorButton() {
            super();

            style_table = new ObjectMap<GRID_ITEM, ButtonStyle>(GRID_ITEM.size());

            TextureRegionDrawable drawable = new TextureRegionDrawable(game.sprites.findRegion("grid/grid_null"));
            ButtonStyle style = new ButtonStyle(drawable, drawable, null);
            style_table.put(GRID_ITEM.NULL, style);

            drawable = new TextureRegionDrawable(game.sprites.findRegion("grid/grid_wall"));
            style = new ButtonStyle(drawable, drawable, null);
            style_table.put(GRID_ITEM.WALL, style);

            drawable = new TextureRegionDrawable(game.sprites.findRegion("grid/grid_goal"));
            style = new ButtonStyle(drawable, drawable, null);
            style_table.put(GRID_ITEM.GOAL, style);

            setStyle(get_style(GRID_ITEM.WALL));
        }

        void cycle_style() {
            current_item_idx++;
            if (current_item_idx >= GRID_ITEM.size())
                current_item_idx = 0;
            setStyle(get_style(GRID_ITEM.values[current_item_idx]));
        }

        private ButtonStyle get_style(GRID_ITEM item) {
            return style_table.get(item);
        }

        final GRID_ITEM get_selected_item() {
            return GRID_ITEM.values[current_item_idx];
        }
    }

    private class EditIcon extends Image {
        EditIcon() {
            super(game.skin, "edit_on");
            setVisible(false);
        }

        private Action get_action() {
            return Actions.sequence(
                Actions.alpha(0.2f),
                Actions.show(),
                Actions.fadeIn(0.6f),
                Actions.delay(1),
                Actions.fadeOut(0.6f),
                Actions.hide()
            );
        }

        void show_on() {
            setDrawable(game.skin, "edit_on");
            clearActions();
            addAction(get_action());
        }

        void show_off() {
            setDrawable(game.skin, "edit_off");
            clearActions();
            addAction(get_action());
        }
    }
}
