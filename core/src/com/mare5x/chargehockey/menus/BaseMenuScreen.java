package com.mare5x.chargehockey.menus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mare5x.chargehockey.ChargeHockeyGame;
import com.mare5x.chargehockey.editor.PermissionTools;
import com.mare5x.chargehockey.notifications.Notification;
import com.mare5x.chargehockey.notifications.TextNotification;

import static com.mare5x.chargehockey.settings.GameDefaults.ACTOR_PAD;
import static com.mare5x.chargehockey.settings.GameDefaults.CELL_PAD;
import static com.mare5x.chargehockey.settings.GameDefaults.MAX_BUTTON_WIDTH;
import static com.mare5x.chargehockey.settings.GameDefaults.MIN_BUTTON_HEIGHT;


// todo minWidth maxWidth constraints ...
public abstract class BaseMenuScreen implements Screen {
    protected final ChargeHockeyGame game;
    protected final Stage stage;
    private final InputMultiplexer input_multiplexer;

    private Notification notification;
    private final Runnable on_remove_notification = new Runnable() {
        @Override
        public void run() {
            notification = null;
        }
    };

    // the default TableLayout will only propagate resizing to its added children
    // treat it as a normal Table and add aspect ratio sensitive layouts with table.add_layout()
    // call table.set_resizable(true) to activate layout_portrait() and layout_landscape()
    protected TableLayout table;

    protected BaseMenuScreen(final ChargeHockeyGame game) {
        this.game = game;

        stage = new Stage(new ScreenViewport(), game.batch);

        table = new TableLayout(game.skin) {
            @Override
            public void portrait() { layout_portrait(); }

            @Override
            public void landscape() { layout_landscape(); }
        };
        table.set_resizable(false);
        table.set_resize_children(true);
        table.setFillParent(true);

        stage.addActor(table);

//        stage.setDebugAll(true);

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

    private Button make_back_button() {
        Button back_button = new Button(game.skin, "back");
        back_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                back_key_pressed();
            }
        });
        back_button.pad(ACTOR_PAD);
        return back_button;
    }

    /* Override to provide a seperate portrait layout. Safe to ignore. */
    protected void layout_portrait() { }

    /* Override to provide a seperate landscape layout. Safe to ignore. */
    protected void layout_landscape() { }

    protected void add_back_button() {
        add_back_button(1);
    }

    protected void add_back_button(int colspan) {
        add_back_button(colspan, true);
    }

    protected void add_back_button(int colspan, boolean new_row) {
        Button back_button = make_back_button();
        table.add(back_button).colspan(colspan).pad(CELL_PAD).size(2 * MIN_BUTTON_HEIGHT * 0.75f, MIN_BUTTON_HEIGHT * 0.75f).left().top();
        if (new_row)
            table.row();
    }

    abstract protected void back_key_pressed();

    protected TextButton make_text_button(String text) {
        return make_text_button(text, true);
    }

    protected TextButton make_text_button(String text, boolean wrap) {
        return make_text_button(game, text, wrap);
    }

    public static TextButton make_text_button(ChargeHockeyGame game, String text, boolean wrap) {
        TextButton button = new TextButton(text, game.skin);
        button.pad(ACTOR_PAD);
        button.getLabel().setWrap(wrap);
        return button;
    }

    /** Adds the button to the table using the default height, width and padding. */
    protected Cell<TextButton> add_text_button(TextButton button) {
        return add_button_to_table(table, button);
    }

    public static <T extends Actor> Cell<T> add_button_to_table(Table table, T button) {
        return table.add(button).pad(CELL_PAD).minHeight(MIN_BUTTON_HEIGHT).prefWidth(MAX_BUTTON_WIDTH);
    }

    protected Label make_label(String text) {
        return make_label(text, true);
    }

    protected Label make_label(String text, boolean borderless) {
        Label label = new Label(text, game.skin, borderless ? "borderless" : "default");
        label.setWrap(true);
        label.setAlignment(Align.center);
        return label;
    }

    /** Displays a TextNotification, making sure only one is displayed. */
    public void show_notification(String message) {
        show_notification(message, -1);
    }

    /** Displays a TextNotification, making sure only one is displayed. */
    protected void show_notification(String message, float show_time) {
        show_notification(new TextNotification(game, stage, message), show_time);
    }

    protected void show_notification(Notification notification) {
        show_notification(notification, -1);
    }

    protected void show_notification(final Notification notification, float show_time) {
        if (this.notification != null && this.notification.is_displayed())
            this.notification.hide();
        this.notification = notification;
        if (show_time > 0)
            notification.show(show_time, on_remove_notification);
        else
            notification.show(on_remove_notification);
    }

    /** Use this instead of game.setScreen to use a transition animation. */
    public void set_screen(final Screen screen) {
        set_screen(screen, false);
    }

    /** Use dispose if you don't dispose in hide(). */
    public void set_screen(final Screen screen, final boolean dispose) {
        fade_out(new Runnable() {
            @Override
            public void run() {
                game.setScreen(screen);
                if (dispose) dispose();
            }
        });
    }

    /** Sets the screen only if we have access to the storage permission, otherwise it attempts to
     *  gain access. */
    public void set_screen_permission_check(final Screen screen) {
        // get file storage access permission on android
        PermissionTools permission_tools = game.get_permission_tools();
        if (!permission_tools.check_storage_permission()) {
            permission_tools.request_storage_permission(new PermissionTools.RequestCallback() {
                public void granted() {
                    set_screen(screen);
                }

                public void denied() { }  // ignore
            });
        } else {
            set_screen(screen);
        }
    }

    protected void fade_in() {
        stage.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0.2f)));
    }

    protected void fade_out(Runnable runnable) {
        stage.addAction(Actions.sequence(Actions.fadeOut(0.2f), Actions.run(runnable)));
    }

    @Override
    public void show() {
        fade_in();
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
        stage.getViewport().update(width, height, true);

        table.resize(width, height);

        if (notification != null && notification.is_displayed())
            notification.resize();

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
