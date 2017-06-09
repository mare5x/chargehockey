package com.mare5x.chargehockey;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
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
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;


class EditorScreen implements Screen {
    private final ChargeHockeyGame game;

    private final InputMultiplexer multiplexer;

    private final Stage edit_stage, button_stage;
    private final OrthographicCamera camera;  // camera of edit_stage

    private final LevelFrameBuffer fbo;

    private Level level;

    private final GridItemSelectorButton grid_item_button;
    private final Button puck_button;

    private Sprite tmp_sprite;

    EditorScreen(final ChargeHockeyGame game, Level level) {
        this.game = game;
        this.level = level;

        camera = new OrthographicCamera();

        float edit_aspect_ratio = Gdx.graphics.getWidth() / (Gdx.graphics.getHeight() * 0.8f);
        edit_stage = new Stage(new FillViewport(edit_aspect_ratio * ChargeHockeyGame.WORLD_HEIGHT, ChargeHockeyGame.WORLD_HEIGHT, camera), game.batch);
        camera.position.set(ChargeHockeyGame.WORLD_WIDTH / 2, ChargeHockeyGame.WORLD_HEIGHT / 2, 0);  // center camera
        edit_stage.setDebugAll(true);

        fbo = new LevelFrameBuffer();

        button_stage = new Stage(new StretchViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight() * 0.2f), game.batch);
        button_stage.setDebugAll(true);

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
        button_table.setBackground(game.skin.getDrawable("px_black"));
        button_table.add(grid_item_button).pad(15).size(Value.percentHeight(0.6f, button_table)).uniform();
        button_table.add(puck_button).pad(15).fill().uniform();
        button_table.add(menu_button).pad(15).size(Value.percentHeight(0.6f, button_table)).expandX().right().uniform();

        button_stage.addActor(button_table);

        multiplexer = new InputMultiplexer(new GestureDetector(new EditGestureAdapter(camera)), edit_stage, button_stage);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(multiplexer);
    }

    private void render_background_fbo() {
        fbo.begin();

        Gdx.gl20.glClearColor(0, 0, 0, 1);
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

        fbo.set_projection_matrix(game.batch);

        game.batch.begin();
        game.batch.disableBlending();
        for (int row = 0; row < ChargeHockeyGame.WORLD_HEIGHT; row++) {
            for (int col = 0; col < ChargeHockeyGame.WORLD_WIDTH; col++) {
                GRID_ITEM item = level.get_grid_item(row, col);
                if (item != GRID_ITEM.NULL) {
                    tmp_sprite = level.get_item_sprite(item);
                    tmp_sprite.setPosition(col, row);
                    tmp_sprite.draw(game.batch);
                }
            }
        }
        game.batch.enableBlending();
        game.batch.end();

        fbo.end();
    }

    @Override
    public void render(float delta) {
        Gdx.gl20.glClearColor(0.1f, 0.1f, 0.1f, 1);  // dark brownish color
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        edit_stage.getViewport().apply();
        game.batch.setProjectionMatrix(camera.combined);

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

        render_background_fbo();
        Gdx.graphics.requestRendering();
    }

    @Override
    public void pause() {
        level.save_grid();
    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        level.save_grid();
        dispose();
    }

    @Override
    public void dispose() {
        fbo.dispose();
        edit_stage.dispose();
        button_stage.dispose();
    }

    private class EditGestureAdapter extends BaseGestureAdapter {
        final Vector2 tmp_coords = new Vector2();

        EditGestureAdapter(OrthographicCamera camera) {
            super(camera);
        }

        @Override
        public boolean tap(float x, float y, int count, int button) {
            edit_stage.screenToStageCoordinates(tmp_coords.set(x, y));
            System.out.printf("%f, %f, %d, %d\n", tmp_coords.x, tmp_coords.y, count, button);

            // ignore taps outside of edit_stage's camera
            if (!point_in_view(tmp_coords.x, tmp_coords.y)) {
                return false;
            }

            int row = (int) tmp_coords.y;
            int col = (int) tmp_coords.x;

            if (puck_button.isChecked())
                level.set_item(row, col, GRID_ITEM.PUCK);
            else
                level.set_item(row, col, grid_item_button.get_selected_item());

            // update the background every tap
            render_background_fbo();

            return false;
        }
    }

    private class GridItemSelectorButton extends Button {
        private int current_item_idx = GRID_ITEM.WALL.ordinal();  // start = wall index
        private ObjectMap<GRID_ITEM, ButtonStyle> style_table;

        GridItemSelectorButton() {
            super();

            style_table = new ObjectMap<GRID_ITEM, ButtonStyle>(GRID_ITEM.size());

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
