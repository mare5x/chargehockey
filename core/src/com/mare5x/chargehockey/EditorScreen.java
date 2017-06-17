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
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;


class EditorScreen implements Screen {
    private final ChargeHockeyGame game;

    private final InputMultiplexer multiplexer;

    private final Stage edit_stage, button_stage;
    private final OrthographicCamera camera;  // camera of edit_stage
    private final EditCameraController camera_controller;

    private final LevelFrameBuffer fbo;

    private Level level;

    private final GridItemSelectorButton grid_item_button;
    private final Button puck_button;

    private Array<ChargeActor> puck_actors;

    // callback function for ChargeActor pucks
    private final DragCallback drag_callback = new DragCallback() {
        @Override
        public void out_of_bounds(ChargeActor charge) {
            puck_actors.removeValue(charge, true);
            charge.clear();
            charge.remove();
        }
    };

    EditorScreen(final ChargeHockeyGame game, Level level) {
        this.game = game;
        this.level = level;

        camera = new OrthographicCamera();

        float edit_aspect_ratio = Gdx.graphics.getWidth() / (Gdx.graphics.getHeight() * 0.8f);
        edit_stage = new Stage(new FillViewport(edit_aspect_ratio * ChargeHockeyGame.WORLD_HEIGHT, ChargeHockeyGame.WORLD_HEIGHT, camera), game.batch);
        camera.position.set(ChargeHockeyGame.WORLD_WIDTH / 2, ChargeHockeyGame.WORLD_HEIGHT / 2, 0);  // center camera

        fbo = new LevelFrameBuffer(game, level);
        fbo.set_draw_pucks(false);
        fbo.set_draw_grid_lines(false);

        // add interactive pucks from the stored puck positions
        puck_actors = new Array<ChargeActor>(level.get_puck_positions().size * 2);
        for (Vector2 puck_pos : level.get_puck_positions()) {
            ChargeActor puck = new ChargeActor(game, CHARGE.PUCK, drag_callback);
            puck.setPosition(puck_pos.x, puck_pos.y);
            puck_actors.add(puck);
            edit_stage.addActor(puck);
        }

        button_stage = new Stage(new StretchViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight() * 0.2f), game.batch);

        Button menu_button = new Button(game.skin, "menu");
        menu_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(game.menu_screen);
            }
        });
        menu_button.pad(10);

        grid_item_button = new GridItemSelectorButton();
        grid_item_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                grid_item_button.cycle_style();
                puck_button.setChecked(false);
            }
        });
        grid_item_button.pad(10);

        puck_button = new Button();
        TextureRegionDrawable puck_drawable_on = new TextureRegionDrawable(game.sprites.findRegion("puck"));
        Drawable puck_drawable_off = puck_drawable_on.tint(game.skin.getColor("grey"));
        puck_button.setStyle(new Button.ButtonStyle(puck_drawable_off, puck_drawable_off, puck_drawable_on));
        puck_button.pad(10);

        Table button_table = new Table();
        button_table.setFillParent(true);
        button_table.setBackground(game.skin.getDrawable("pixels/px_black"));
        button_table.add(grid_item_button).pad(15).size(Value.percentHeight(0.6f, button_table)).uniform();
        button_table.add(puck_button).pad(15).fill().uniform();
        button_table.add(menu_button).pad(15).size(Value.percentHeight(0.6f, button_table)).expandX().right().uniform();

        button_stage.addActor(button_table);

        camera_controller = new EditCameraController(camera);
        InputAdapter back_key_processor = new InputAdapter() {  // same as menu_button
            @Override
            public boolean keyUp(int keycode) {
                if (keycode == Input.Keys.BACK) {
                    game.setScreen(game.menu_screen);
                }
                return true;
            }
        };
        multiplexer = new InputMultiplexer(edit_stage, button_stage, new GestureDetector(camera_controller), back_key_processor);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void render(float delta) {
        Gdx.gl20.glClearColor(0.1f, 0.1f, 0.1f, 1);  // dark brownish color
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

        edit_stage.getViewport().apply();
        game.batch.setProjectionMatrix(camera.combined);

        camera_controller.update(delta);

        game.batch.begin();
        game.batch.disableBlending();
        fbo.render(game.batch, 0, 0, ChargeHockeyGame.WORLD_WIDTH, ChargeHockeyGame.WORLD_HEIGHT);
        game.batch.enableBlending();
        game.batch.end();

        edit_stage.act();
        edit_stage.draw();

        button_stage.getViewport().apply();
        button_stage.act();
        button_stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        edit_stage.getViewport().setScreenBounds(0, (int) (height * 0.2f), width, (int) (height * 0.8f));

        button_stage.getViewport().setScreenBounds(0, 0, width, (int) (height * 0.2f));

        fbo.update(game.batch);
        Gdx.graphics.requestRendering();
    }

    @Override
    public void pause() {
        level.save_level(puck_actors);
    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
//        level.save_grid();
        level.save_level(puck_actors);
        dispose();
    }

    @Override
    public void dispose() {
        fbo.dispose();
        edit_stage.dispose();
        button_stage.dispose();
    }

    private class EditCameraController extends CameraController {
        final Vector2 tmp_coords = new Vector2();

        EditCameraController(OrthographicCamera camera) {
            super(camera);
        }

        @Override
        public boolean tap(float x, float y, int count, int button) {
            super.tap(x, y, count, button);

            // first the camera must be stopped, then items can be set
            if (count == 1 && is_moving())
                return false;
            else
                stop_movement(true);

            edit_stage.screenToStageCoordinates(tmp_coords.set(x, y));
            System.out.printf("%f, %f, %d, %d\n", tmp_coords.x, tmp_coords.y, count, button);

            // ignore taps outside of edit_stage's camera and outside the world
            if (!point_in_view(tmp_coords.x, tmp_coords.y) || !ChargeHockeyGame.WORLD_RECT.contains(tmp_coords)) {
                return false;
            }

            int row = (int) tmp_coords.y;
            int col = (int) tmp_coords.x;

            if (puck_button.isChecked()) {
                ChargeActor charge = new ChargeActor(game, CHARGE.PUCK, drag_callback);
                charge.setPosition(tmp_coords.x, tmp_coords.y);
                edit_stage.addActor(charge);
                puck_actors.add(charge);
            }
            else
                level.set_item(row, col, grid_item_button.get_selected_item());

            // update the background every tap
            fbo.update(game.batch);

            return false;
        }

        @Override
        public boolean zoom(float initialDistance, float distance) {
            super.zoom(initialDistance, distance);

            if (!LevelFrameBuffer.get_grid_lines_setting())
                return true;

            if (camera.zoom <= 0.6f) {
                fbo.set_draw_grid_lines(true);
                fbo.update(game.batch);
            } else {
                if (fbo.get_draw_grid_lines()) {
                    fbo.set_draw_grid_lines(false);
                    fbo.update(game.batch);
                }
            }
            return true;
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
}
