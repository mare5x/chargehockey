package com.mare5x.chargehockey;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;


class EditorScreen implements Screen {
    private final ChargeHockeyGame game;

    private final InputMultiplexer multiplexer;

    private final Stage edit_stage, button_stage;
    private final OrthographicCamera camera;

    private Level level;

    private final GridItemSelectorButton grid_item_button;

    private Sprite sprite;
    private final TextureRegion bg;

    EditorScreen(final ChargeHockeyGame game, Level level) {
        this.game = game;
        this.level = level;

        camera = new OrthographicCamera();

        float edit_aspect_ratio = Gdx.graphics.getWidth() / (Gdx.graphics.getHeight() * 0.8f);
        edit_stage = new Stage(new FillViewport(edit_aspect_ratio * ChargeHockeyGame.WORLD_HEIGHT, ChargeHockeyGame.WORLD_HEIGHT, camera), game.batch);
        camera.position.set(ChargeHockeyGame.WORLD_WIDTH / 2, ChargeHockeyGame.WORLD_HEIGHT / 2, 0);  // center camera
        edit_stage.setDebugAll(true);

        button_stage = new Stage(new StretchViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight() * 0.2f), game.batch);
        button_stage.setDebugAll(true);

        Button back_button = new Button(game.skin, "back");
        back_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("back_button", "clicked");
                game.setScreen(game.menu_screen);
            }
        });
        back_button.pad(10);

        grid_item_button = new GridItemSelectorButton();
        grid_item_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("grid_item_button", "clicked");
                grid_item_button.cycle_next_style();
            }
        });
        grid_item_button.pad(10);

        Table button_table = new Table();
        button_table.setFillParent(true);
        button_table.setBackground(game.skin.getDrawable("px_black"));
        button_table.add(grid_item_button).pad(15).size(Value.percentHeight(0.4f, button_table));
        button_table.add(back_button).pad(15).size(Value.percentHeight(0.8f, button_table), Value.percentHeight(0.4f, button_table)).expandX().right();

        button_stage.addActor(button_table);

        bg = game.skin.getRegion("px_black");

        multiplexer = new InputMultiplexer(new GestureDetector(new EditGestureAdapter()), edit_stage, button_stage);
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
        edit_stage.act();
        edit_stage.draw();

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        game.batch.draw(bg, 0, 0, ChargeHockeyGame.WORLD_WIDTH, ChargeHockeyGame.WORLD_HEIGHT);  // background color

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

        button_stage.getViewport().apply();
        button_stage.act();
        button_stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        edit_stage.getViewport().setScreenBounds(0, (int) (height * 0.2f), width, (int) (height * 0.8f));

        button_stage.getViewport().setScreenBounds(0, 0, width, (int) (height * 0.2f));
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        level.save();
        dispose();
    }

    @Override
    public void dispose() {
        edit_stage.dispose();
        button_stage.dispose();
    }

    private class EditGestureAdapter extends GestureDetector.GestureAdapter {
        final Vector2 tmp_coords = new Vector2();
        static final int BORDER = 16;
        float prev_zoom_distance = 0;

        @Override
        public boolean tap(float x, float y, int count, int button) {
            edit_stage.screenToStageCoordinates(tmp_coords.set(x, y));
            System.out.printf("%f, %f, %d, %d\n", tmp_coords.x, tmp_coords.y, count, button);

            int row = (int) tmp_coords.y;
            int col = (int) tmp_coords.x;

            level.set_item(row, col, grid_item_button.get_selected_item());

            return false;
        }

        @Override
        public boolean pan(float x, float y, float deltaX, float deltaY) {
            camera.translate(-deltaX / camera.viewportWidth * camera.zoom * 2, deltaY / camera.viewportHeight * camera.zoom * 2);

            // camera.position is in the center of the camera

            float xw = camera.position.x - camera.viewportWidth / 2f * camera.zoom;
            float xe = camera.position.x + camera.viewportWidth / 2f * camera.zoom;
            float yn = camera.position.y + camera.viewportHeight / 2f * camera.zoom;
            float ys = camera.position.y - camera.viewportHeight / 2f * camera.zoom;

            if (xw < -BORDER * camera.zoom)
                camera.translate(-BORDER - xw, 0);
            else if (xe > ChargeHockeyGame.WORLD_WIDTH + BORDER * camera.zoom)
                camera.translate(ChargeHockeyGame.WORLD_WIDTH + (BORDER * camera.zoom) - xe, 0);
            if (ys < -BORDER * camera.zoom)
                camera.translate(0, -BORDER - ys);
            else if (yn > ChargeHockeyGame.WORLD_HEIGHT + BORDER * camera.zoom)
                camera.translate(0, ChargeHockeyGame.WORLD_HEIGHT + (BORDER * camera.zoom) - yn);

            return true;
        }

        @Override
        public boolean zoom(float initialDistance, float distance) {
            float new_zoom_distance = distance - initialDistance;
            float amount = (new_zoom_distance - prev_zoom_distance) / camera.viewportHeight;
            prev_zoom_distance = new_zoom_distance;

            camera.zoom -= amount * 0.2f;

            if (camera.zoom < 0.1f)
                camera.zoom = 0.1f;
            else if (camera.zoom > 1.8f)
                camera.zoom = 1.8f;

            return true;
        }

        @Override
        public void pinchStop() {
            prev_zoom_distance = 0;
        }
    }

    private class GridItemSelectorButton extends Button {
        private int current_item_idx = 1;  // start = wall index
        private ObjectMap<GRID_ITEM, ButtonStyle> style_table;

        GridItemSelectorButton() {
            super();

            style_table = new ObjectMap<GRID_ITEM, ButtonStyle>(GRID_ITEM.values.length);

            TextureRegionDrawable drawable = new TextureRegionDrawable(game.sprites.findRegion("grid_null"));
            ButtonStyle style = new ButtonStyle(drawable, drawable, null);
            style_table.put(GRID_ITEM.NULL, style);

            drawable = new TextureRegionDrawable(game.sprites.findRegion("grid_wall"));
            style = new ButtonStyle(drawable, drawable, null);
            style_table.put(GRID_ITEM.WALL, style);

            drawable = new TextureRegionDrawable(game.sprites.findRegion("grid_goal"));
            style = new ButtonStyle(drawable, drawable, null);
            style_table.put(GRID_ITEM.GOAL, style);

            setStyle(get_style(GRID_ITEM.WALL));
        }

        void cycle_next_style() {
            current_item_idx++;
            if (current_item_idx >= GRID_ITEM.values.length)
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
