package com.mare5x.chargehockey.editor;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mare5x.chargehockey.ChargeHockeyGame;
import com.mare5x.chargehockey.level.Level;
import com.mare5x.chargehockey.level.Level.LEVEL_TYPE;
import com.mare5x.chargehockey.level.LevelSelector;
import com.mare5x.chargehockey.menus.BaseMenuScreen;
import com.mare5x.chargehockey.menus.NameInputDialog;
import com.mare5x.chargehockey.menus.TableLayout;
import com.mare5x.chargehockey.notifications.EditorNoLevelsNotification;
import com.mare5x.chargehockey.notifications.Notification;

import java.util.Locale;

import static com.mare5x.chargehockey.settings.GameDefaults.ACTOR_PAD;
import static com.mare5x.chargehockey.settings.GameDefaults.CELL_PAD;
import static com.mare5x.chargehockey.settings.GameDefaults.MIN_BUTTON_HEIGHT;


class EditorMenuScreen extends BaseMenuScreen {
    private final AddInputDialog add_input_dialog;
    private final EditInputDialog edit_input_dialog;
    private final LevelSelector level_selector;

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
                    show_notification(new EditorNoLevelsNotification(game, stage));
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
            show_notification(new EditorNoLevelsNotification(game, stage));
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

    private class AddInputDialog extends NameInputDialog {
        AddInputDialog(Stage stage, String title, Skin skin) {
            super(stage, title, skin);
        }

        @Override
        public void on_confirm() {
            if (level_selector.is_empty())
                show_notification("LONG PRESS TO DELETE OR RENAME A LEVEL", 2 * Notification.DEFAULT_SHOW_TIME);
            level_selector.add_level(get_name());
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
        public void on_confirm() {
            if (!level_selector.rename_selected_level(get_name())) {
                show_notification("ERROR RENAMING LEVEL");
            }
        }
    }
}
