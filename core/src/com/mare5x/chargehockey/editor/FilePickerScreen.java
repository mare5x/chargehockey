package com.mare5x.chargehockey.editor;


import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mare5x.chargehockey.ChargeHockeyGame;
import com.mare5x.chargehockey.actors.ScrollableLabel;
import com.mare5x.chargehockey.menus.BaseMenuScreen;

class FilePickerScreen extends BaseMenuScreen {
    interface FilePickerCallback {
        void on_result(FileHandle path);
    }

    private final Screen parent_screen;
    private final FilePicker file_picker;

    FilePickerScreen(ChargeHockeyGame game, Screen parent_screen, final FilePickerCallback callback) {
        this(game, parent_screen, callback, null);
    }

    // MUST HAVE STORAGE PERMISSIONS! (set screen with set_screen_permission_check())
    FilePickerScreen(ChargeHockeyGame game, Screen parent_screen, final FilePickerCallback callback, FilePicker.FileFilter filter) {
        super(game);

        this.parent_screen = parent_screen;

        final ScrollableLabel path_label = new ScrollableLabel(game);

        if (filter != null)
            file_picker = game.get_file_picker(filter);
        else
            file_picker = game.get_file_picker();
        file_picker.set_event_listener(new FilePicker.EventListener() {
            @Override
            public void dir_changed(FileHandle path) {
                path_label.setText(path.file().getAbsolutePath());
            }
        });
        path_label.setText(file_picker.get_current_path().file().getAbsolutePath());

        TextButton back_button = make_text_button("BACK", false);
        back_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                file_picker.show_parent_dir();
            }
        });

        TextButton result_button = make_text_button("SELECT", false);
        result_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                FileHandle selected_path = file_picker.get_selected_path();
                if (selected_path.file().isFile() && file_picker.is_valid(selected_path))
                    callback.on_result(selected_path);
                else
                    callback.on_result(file_picker.get_current_path());
                back_key_pressed();
            }
        });

        add_back_button(2);
        table.add(path_label.get()).colspan(2).pad(15).minHeight(MIN_BUTTON_HEIGHT).width(Value.percentWidth(0.9f, table)).center().row();
        table.add(file_picker.get_display()).colspan(2).pad(15).expand().fill().row();
        table.add(back_button).pad(15).height(MIN_BUTTON_HEIGHT).fillX();
        table.add(result_button).pad(15).height(MIN_BUTTON_HEIGHT).expandX().fillX();
    }

    @Override
    public void show() {
        super.show();
        file_picker.refresh();
    }

    @Override
    protected void back_key_pressed() {
        set_screen(parent_screen);
    }

    @Override
    public void hide() {
        dispose();
    }
}
