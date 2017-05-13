package com.mare5x.chargehockey;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.StretchViewport;


class LevelSelectorScreen implements Screen {
    private final ChargeHockeyGame game;

    private final LevelSelector level_selector;

    private final Stage stage;

    LevelSelectorScreen(final ChargeHockeyGame game, LEVEL_TYPE level_type) {
        this.game = game;

        stage = new Stage(new StretchViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()), game.batch);
        stage.setDebugAll(true);

        level_selector = new LevelSelector(game, level_type);

        Button back_button = new Button(game.skin, "back");
        back_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(game.menu_screen);
            }
        });
        back_button.pad(10);

        Button play_button = new Button(game.skin, "play");
        play_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                final Level level = level_selector.load_selected_level();
                if (level != null) {
                    game.setScreen(new GameScreen(game, level));
                }
            }
        });
        play_button.pad(10);

        Table table = new Table();
        table.setFillParent(true);

        table.pad(50 * ChargeHockeyGame.DENSITY, 15 * ChargeHockeyGame.DENSITY, 50 * ChargeHockeyGame.DENSITY, 15 * ChargeHockeyGame.DENSITY);

        table.add(back_button).pad(15).expandX().size(Value.percentWidth(1f, play_button), Value.percentWidth(0.5f, play_button)).row();
        table.add(level_selector.get_display()).pad(15).expand().fill();
        table.row();
        table.add(play_button).pad(15).size(Value.percentWidth(0.3f, table));

        stage.addActor(table);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl20.glClearColor(0, 0, 0, 1);
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height);

        Gdx.graphics.requestRendering();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
