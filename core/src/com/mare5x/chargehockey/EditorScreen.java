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
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;


class EditorScreen implements Screen {
    private final ChargeHockeyGame game;

    private final InputMultiplexer multiplexer;

    private final Stage edit_stage, button_stage;
    private final OrthographicCamera camera;

    private final Sprite sprite;
    private final TextureRegion bg;

    public EditorScreen(final ChargeHockeyGame game) {
        this.game = game;

        camera = new OrthographicCamera();

        float edit_aspect_ratio = Gdx.graphics.getWidth() / (Gdx.graphics.getHeight() * 0.8f);
        edit_stage = new Stage(new FillViewport(edit_aspect_ratio * game.WORLD_HEIGHT, game.WORLD_HEIGHT, camera), game.batch);
        camera.position.set(game.WORLD_WIDTH / 2, game.WORLD_HEIGHT / 2, 0);  // center camera
        edit_stage.setDebugAll(true);

        button_stage = new Stage(new StretchViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight() * 0.2f), game.batch);
        button_stage.setDebugAll(true);

        Button back_button = new Button(game.skin, "back");
        back_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("back_button", "clicked");
                game.setScreen(game.menu_screen);
                dispose();
            }
        });
        back_button.pad(10);

        Table button_table = new Table();
        button_table.setFillParent(true);
        button_table.setBackground(game.skin.getDrawable("px_black"));

        button_table.add(back_button).pad(15).size(Value.percentHeight(0.8f, button_table), Value.percentHeight(0.4f, button_table)).expandX().right();

        button_stage.addActor(button_table);

        sprite = new Sprite(game.sprites.findRegion("pos_red64"));
        sprite.setSize(4, 4);
        sprite.setOriginCenter();
        sprite.setPosition(32, 32);

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
        game.batch.draw(bg, 0, 0, game.WORLD_WIDTH, game.WORLD_HEIGHT);  // background color
        sprite.draw(game.batch);
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

            return false;
        }

        @Override
        public boolean pan(float x, float y, float deltaX, float deltaY) {
            camera.translate(-deltaX / camera.viewportWidth * camera.zoom, deltaY / camera.viewportHeight * camera.zoom);

            if (camera.position.x < BORDER)
                camera.position.set(BORDER, camera.position.y, 0);
            else if (camera.position.x > (game.WORLD_WIDTH - BORDER))
                camera.position.set(game.WORLD_WIDTH - BORDER, camera.position.y, 0);
            if (camera.position.y < BORDER)
                camera.position.set(camera.position.x, BORDER, 0);
            else if (camera.position.y > (game.WORLD_HEIGHT - BORDER))
                camera.position.set(camera.position.x, game.WORLD_HEIGHT - BORDER, 0);

            return true;
        }

        @Override
        public boolean zoom(float initialDistance, float distance) {
            float new_zoom_distance = distance - initialDistance;
            float amount = (new_zoom_distance - prev_zoom_distance) / camera.viewportHeight;
            prev_zoom_distance = new_zoom_distance;

            camera.zoom -= amount * 0.2f;

            if (camera.zoom < 0.2f)
                camera.zoom = 0.2f;
            else if (camera.zoom > 2)
                camera.zoom = 2;

            return true;
        }

        @Override
        public void pinchStop() {
            prev_zoom_distance = 0;
        }
    }
}
