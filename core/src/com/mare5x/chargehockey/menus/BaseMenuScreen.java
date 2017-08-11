package com.mare5x.chargehockey.menus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.mare5x.chargehockey.ChargeHockeyGame;
import com.mare5x.chargehockey.notifications.Notification;
import com.mare5x.chargehockey.notifications.TextNotification;


// todo add transition animations ...
// todo minWidth maxWidth constraints ...
public abstract class BaseMenuScreen implements Screen {
    // public static float MIN_BUTTON_HEIGHT;  // todo implement a minimum button height

    protected final ChargeHockeyGame game;
    protected final Stage stage;
    private final InputMultiplexer input_multiplexer;

    protected Notification notification = null;

    protected Table table;

    protected BaseMenuScreen(final ChargeHockeyGame game) {
        this.game = game;

        stage = new Stage(new StretchViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()), game.batch);

        table = new Table(game.skin);
        table.setFillParent(true);

        stage.addActor(table);

        InputAdapter back_key_processor = new InputAdapter() {  // same as return button
            @Override
            public boolean keyUp(int keycode) {
                if (keycode == Input.Keys.BACK) {
                    back_key_pressed();
                }
                return true;
            }
        };
        input_multiplexer = new InputMultiplexer(stage, back_key_processor);
    }

    protected void add_back_button() {
        add_back_button(1);
    }

    protected void add_back_button(int colspan) {
        Button back_button = new Button(game.skin, "back");
        back_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                back_key_pressed();
            }
        });
        back_button.pad(10);

        table.add(back_button).colspan(colspan).pad(15).size(Value.percentWidth(0.3f, table), Value.percentWidth(0.15f, table)).left().top().row();
    }

    abstract protected void back_key_pressed();

    protected TextButton make_text_button(String text) {
        return make_text_button(text, true);
    }

    protected TextButton make_text_button(String text, boolean wrap) {
        TextButton button = new TextButton(text, game.skin);
        button.pad(10);
        button.getLabel().setWrap(wrap);
        return button;
    }

    /** Displays a TextNotification, making sure only one is displayed. */
    protected void show_notification(String message) {
        show_notification(message, Notification.DEFAULT_SHOW_TIME);
    }

    /** Displays a TextNotification, making sure only one is displayed. */
    protected void show_notification(String message, float show_time) {
        if (notification != null)
            notification.remove();
        notification = new TextNotification(game, stage, message);
        notification.show(show_time);
    }

    protected void remove_notification() {
        if (notification != null)
            notification.remove();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(input_multiplexer);
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
        stage.getViewport().update(width, height, false);

        Gdx.graphics.requestRendering();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    abstract public void hide();

    @Override
    public void dispose() {
        stage.dispose();
    }
}
