package com.mare5x.chargehockey.editor;


import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mare5x.chargehockey.ChargeHockeyGame;
import com.mare5x.chargehockey.menus.BaseMenuScreen;


public class CustomMenuScreen extends BaseMenuScreen {
    private final FilePickerScreen.FilePickerCallback import_callback;
    private final Importer importer;

    public CustomMenuScreen(final ChargeHockeyGame game) {
        super(game);

        importer = new Importer(game, stage);

        import_callback = new FilePickerScreen.FilePickerCallback() {
            @Override
            public void on_result(FileHandle path) {
                importer.handle_import(path);
            }
        };

        TextButton edit_button = make_text_button("EDIT");
        edit_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                set_screen(new EditorMenuScreen(game), true);
            }
        });

        TextButton import_button = make_text_button("IMPORT");
        import_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                set_screen(new FilePickerScreen(game, CustomMenuScreen.this, import_callback, Importer.get_filter()));
            }
        });

        TextButton export_button = make_text_button("EXPORT");
        export_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                set_screen(new ExportScreen(game), true);
            }
        });

        add_back_button();
        table.add().expand().row();
        add_text_button(edit_button).row();
        add_text_button(import_button).row();
        add_text_button(export_button).row();
        table.add().expand();
    }

    @Override
    protected void back_key_pressed() {
        set_screen(game.menu_screen, true);
    }

    @Override
    public void hide() {

    }
}
