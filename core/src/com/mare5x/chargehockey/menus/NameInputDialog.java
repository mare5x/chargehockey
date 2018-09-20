package com.mare5x.chargehockey.menus;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.utils.Align;

import static com.mare5x.chargehockey.settings.GameDefaults.ACTOR_PAD;
import static com.mare5x.chargehockey.settings.GameDefaults.CELL_PAD;
import static com.mare5x.chargehockey.settings.GameDefaults.IMAGE_FONT_SIZE;
import static com.mare5x.chargehockey.settings.GameDefaults.MAX_BUTTON_WIDTH;
import static com.mare5x.chargehockey.settings.GameDefaults.MIN_BUTTON_HEIGHT;


public abstract class NameInputDialog extends Dialog {
    private enum DIALOG_BUTTON { CANCEL, CONFIRM }

    private final TextField name_input;
    private final Stage stage;

    public NameInputDialog(Stage stage, String title, Skin skin) {
        super(title, skin);

        this.stage = stage;

        setModal(true);
        setResizable(false);
        setMovable(false);

        pad(IMAGE_FONT_SIZE);

        getTitleTable().clear();

        name_input = new TextField("LEVEL NAME", skin);
        name_input.setAlignment(Align.center);

        Table content_table = getContentTable();
        content_table.defaults().pad(CELL_PAD).minHeight(MIN_BUTTON_HEIGHT).prefWidth(get_input_width()).minWidth(0);
        content_table.add(name_input).row();

        Button cancel_button = new Button(skin, "cancel");
        cancel_button.pad(ACTOR_PAD);
        Button confirm_button = new Button(skin, "confirm");
        confirm_button.pad(ACTOR_PAD);

        getButtonTable().defaults().size(MIN_BUTTON_HEIGHT).padTop(CELL_PAD).space(CELL_PAD).expandX();
        button(cancel_button, DIALOG_BUTTON.CANCEL);
        button(confirm_button, DIALOG_BUTTON.CONFIRM);

        addListener(new InputListener() {
            @Override
            public boolean keyUp(InputEvent event, int keycode) {
                if (keycode == Input.Keys.BACK)
                    result(DIALOG_BUTTON.CANCEL);
                return true;
            }
        });
    }

    @Override
    public float getPrefWidth() {
        return Math.min(stage.getWidth() * 0.8f, MAX_BUTTON_WIDTH * 1.25f);
    }

    private Value get_input_width() {
        return new Value() {
            @Override
            public float get(Actor context) {
                return getPrefWidth() - getPadLeft() - getPadRight();
            }
        };
    }

    public Dialog show() {
        return show("LEVEL NAME");
    }

    public Dialog show(String level_name) {
        super.show(stage);

        setPosition(Math.round((stage.getWidth() - getWidth()) / 2), Math.round(stage.getHeight() * 0.9f - getHeight()));

        name_input.setText(level_name);

        name_input.selectAll();  // select everything, so it's ready to be overwritten
        stage.setKeyboardFocus(name_input);
        set_keyboard_visible(true);

        return this;
    }

    public void set_keyboard_visible(boolean visible) {
        name_input.getOnscreenKeyboard().show(visible);
    }

    public void resize() {
        pack();
        setPosition(Math.round((stage.getWidth() - getWidth()) / 2), Math.round(stage.getHeight() * 0.9f - getHeight()));
    }

    @Override
    protected void result(Object object) {
        hide();
        if (object.equals(DIALOG_BUTTON.CONFIRM))
            on_confirm();
    }

    @Override
    public void hide() {
        set_keyboard_visible(false);
        super.hide();
    }

    public String get_name() {
        return name_input.getText();
    }

    public abstract void on_confirm();
}

