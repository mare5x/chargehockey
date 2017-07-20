package com.mare5x.chargehockey;


import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

class CustomMenuScreen extends BaseMenuScreen {
    private final ChargeHockeyGame game;

    private final FilePickerScreen.FilePickerCallback callback;
    private static final FilePicker.FileFilter import_filter = new FilePicker.FileFilter() {
            @Override
            public boolean is_valid(FileHandle path) {
                return path.isDirectory() || path.extension().equals("grid");
            }
        };

    CustomMenuScreen(final ChargeHockeyGame game) {
        super(game);

        this.game = game;

        callback = new FilePickerScreen.FilePickerCallback() {
            @Override
            public void on_result(FileHandle path) {
                System.out.println(path.path());
            }
        };

        TextButton edit_button = new TextButton("EDIT", game.skin);
        edit_button.pad(10);
        edit_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new EditorMenuScreen(game));
                dispose();
            }
        });

        TextButton import_button = new TextButton("IMPORT", game.skin);
        import_button.pad(10);
        import_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new FilePickerScreen(game, CustomMenuScreen.this, callback, import_filter));
            }
        });

        TextButton export_button = new TextButton("EXPORT", game.skin);
        export_button.pad(10);
        export_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new ExportScreen(game));
                dispose();
            }
        });

        add_back_button(game.skin);
        table.add().expand().row();
        Value width = Value.percentWidth(0.6f, table);
        table.add(edit_button).pad(15).uniform().width(width).row();
        table.add(import_button).pad(15).uniform().width(width).row();
        table.add(export_button).pad(15).uniform().width(width).row();
        table.add().expand();
    }

    @Override
    protected void back_key_pressed() {
        game.setScreen(game.menu_screen);
        dispose();
    }

    @Override
    public void hide() {
    }
}
