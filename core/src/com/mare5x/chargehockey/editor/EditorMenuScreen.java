package com.mare5x.chargehockey.editor;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.mare5x.chargehockey.ChargeHockeyGame;
import com.mare5x.chargehockey.level.Level;
import com.mare5x.chargehockey.level.Level.LEVEL_TYPE;
import com.mare5x.chargehockey.level.LevelSelector;
import com.mare5x.chargehockey.menus.BaseMenuScreen;
import com.mare5x.chargehockey.menus.TableLayout;
import com.mare5x.chargehockey.notifications.EditorNoLevelsNotification;
import com.mare5x.chargehockey.settings.GameDefaults;

import java.util.Locale;

import static com.mare5x.chargehockey.settings.GameDefaults.ACTOR_PAD;
import static com.mare5x.chargehockey.settings.GameDefaults.CELL_PAD;
import static com.mare5x.chargehockey.settings.GameDefaults.MAX_BUTTON_WIDTH;
import static com.mare5x.chargehockey.settings.GameDefaults.MIN_BUTTON_HEIGHT;


class EditorMenuScreen extends BaseMenuScreen {
    private final AddInputDialog add_input_dialog;
    private final EditInputDialog edit_input_dialog;
    private final LevelSelector level_selector;

    private enum DIALOG_BUTTON {
        CANCEL, CONFIRM
    }

    EditorMenuScreen(final ChargeHockeyGame game) {
        this(game, null);
    }

    EditorMenuScreen(final ChargeHockeyGame game, final Level selected_level) {
        super(game);

        add_input_dialog = new AddInputDialog(stage, "ADD LEVEL", game.skin);
        edit_input_dialog = new EditInputDialog(stage, "EDIT LEVEL", game.skin);

        level_selector = new LevelSelector(game, LEVEL_TYPE.CUSTOM) {
            @Override
            public void on_long_press(String level_name) {
                edit_input_dialog.show(level_name);
            }
        };
        level_selector.set_select_on_long_press(true);

        Button play_button = new Button(game.skin, "play");
        play_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                final Level level = level_selector.load_selected_level();
                if (level != null) {
                    EditorScreen editor = new EditorScreen(game, level);
                    set_screen(editor, true);
                    if (level_selector.get_level_count() == 1)
                        editor.show_paint_tip();
                } else if (level_selector.is_empty()) {
                    remove_notification();
                    notification = new EditorNoLevelsNotification(game, stage);
                    notification.show();
                } else {
                    show_notification("FIRST, SELECT THE LEVEL YOU WISH TO EDIT", 2);
                }
            }
        });
        play_button.pad(ACTOR_PAD);

        Button add_button = new Button(game.skin, "add");
        add_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                add_input_dialog.show();
            }
        });
        add_button.pad(ACTOR_PAD);

        TableLayout level_selector_layout = level_selector.get_table_layout();
        table.add_layout(level_selector_layout);

        add_back_button(1, false);
        table.add(add_button).pad(CELL_PAD).size(MIN_BUTTON_HEIGHT).expandX().right().row();
        table.defaults().colspan(2);
        table.add(level_selector_layout).pad(CELL_PAD).expand().fill().row();
        table.add(play_button).pad(CELL_PAD).size(1.75f * MIN_BUTTON_HEIGHT);

        if (level_selector.is_empty()) {
            remove_notification();
            notification = new EditorNoLevelsNotification(game, stage);
            notification.show();
        } else if (selected_level != null) {
            // workaround for scrolling to selected level (TableLayout initialized after resize)
            table.resize(stage.getViewport().getScreenWidth(), stage.getViewport().getScreenHeight());
            table.validate();
            level_selector.select(selected_level.get_name());
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if (add_input_dialog.isVisible())
            add_input_dialog.resize();
        if (edit_input_dialog.isVisible())
            edit_input_dialog.resize();
    }

    @Override
    protected void back_key_pressed() {
        set_screen(new CustomMenuScreen(game), true);
    }

    @Override
    public void hide() { }  // DISPOSE IN SET_SCREEN

    private class AddInputDialog extends Dialog {
        final TextField name_input;
        final Stage stage;

        AddInputDialog(Stage stage, String title, Skin skin) {
            super(title, skin);

            this.stage = stage;

            setModal(true);
            setResizable(false);
            setMovable(false);

            pad(GameDefaults.FONT_SIZE * 1.5f * GameDefaults.DENSITY);

            getTitleTable().clear();

            name_input = new TextField("LEVEL NAME", game.skin);
            name_input.setAlignment(Align.center);

            Table content_table = getContentTable();
            content_table.defaults().pad(CELL_PAD).minHeight(MIN_BUTTON_HEIGHT).prefWidth(get_input_width()).minWidth(0);
            content_table.add(name_input).row();

            Button cancel_button = new Button(game.skin, "cancel");
            cancel_button.pad(ACTOR_PAD);
            Button confirm_button = new Button(game.skin, "confirm");
            confirm_button.pad(ACTOR_PAD);

            getButtonTable().defaults().size(MIN_BUTTON_HEIGHT).padTop(CELL_PAD).space(CELL_PAD).expandX().center();
            button(cancel_button, DIALOG_BUTTON.CANCEL);
            button(confirm_button, DIALOG_BUTTON.CONFIRM);
        }

        @Override
        public float getPrefWidth() {
            return Math.min(stage.getWidth() * 0.8f, MAX_BUTTON_WIDTH * 1.25f);
        }

        Value get_input_width() {
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

            super.setPosition(stage.getWidth() / 2 - getWidth() / 2, (float) (stage.getHeight() * 0.8 - getHeight()));

            name_input.setText(level_name);

            name_input.selectAll();  // select everything, so it's ready to be overwritten
            stage.setKeyboardFocus(name_input);
            set_keyboard_visible(true);

            return this;
        }

        void set_keyboard_visible(boolean visible) {
            name_input.getOnscreenKeyboard().show(visible);
        }

        void resize() {
            pack();
            super.setPosition(stage.getWidth() / 2 - getWidth() / 2, (float) (stage.getHeight() * 0.8 - getHeight()));
        }

        @Override
        protected void result(Object object) {
            if (object.equals(DIALOG_BUTTON.CONFIRM)) {
                on_confirm();
            }
            hide();
        }

        @Override
        public void hide() {
            set_keyboard_visible(false);
            super.hide();
        }

        protected void on_confirm() {
            level_selector.add_level(name_input.getText());
        }
    }

    private class EditInputDialog extends AddInputDialog {
        String level_name;

        EditInputDialog(Stage stage, String title, Skin skin) {
            super(stage, title, skin);

            TextButton delete_button = make_text_button("DELETE");
            delete_button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    level_selector.remove_selected_level();
                    hide();
                }
            });
            TextButton export_button = make_text_button("EXPORT");
            export_button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    Exporter exporter = new Exporter(game, EditorMenuScreen.this, new Exporter.ExporterCallback() {
                        @Override
                        public void on_success(FileHandle path) {
                            show_notification(String.format(Locale.US, "EXPORTED TO: %s", path.file().getAbsolutePath()));
                        }

                        @Override
                        public void on_failure(FileHandle path) {
                            show_notification("FAILED TO EXPORT");
                        }
                    });
                    exporter.export(level_name);
                    set_keyboard_visible(false);
                }
            });

            Table content_table = getContentTable();  // see inherited defaults()
            content_table.add(delete_button).row();
            content_table.add(export_button);
        }

        @Override
        public Dialog show(String level_name) {
            this.level_name = level_name;
            return super.show(level_name);
        }

        @Override
        protected void on_confirm() {
            if (!level_selector.rename_selected_level(name_input.getText())) {
                show_notification("ERROR RENAMING LEVEL");
            }
        }
    }
}
