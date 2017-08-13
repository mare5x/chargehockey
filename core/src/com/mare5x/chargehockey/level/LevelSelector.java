package com.mare5x.chargehockey.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.utils.Scaling;
import com.mare5x.chargehockey.ChargeHockeyGame;
import com.mare5x.chargehockey.level.Level.LEVEL_TYPE;

import java.util.Locale;


// todo sort levels by date/name ...
public class LevelSelector {
    private final LEVEL_TYPE level_type;

    private final LevelList list;
    private final ScrollPane scroll_pane;

    private final LevelFrameBuffer preview_fbo;

    private Level selected_level = null;

    public LevelSelector(final ChargeHockeyGame game, LEVEL_TYPE level_type) {
        this.level_type = level_type;

        LevelList.SelectionListener selection_listener = new LevelList.SelectionListener() {
            @Override
            public void changed(String selected) {
                selected_level = load_selected_level(true);
                if (selected_level != null) {
                    preview_fbo.set_level(selected_level);
                    preview_fbo.update(game.batch);
                }
            }
        };
        list = new LevelList(game, selection_listener, level_type);

        scroll_pane = new ScrollPane(list, game.skin);
        scroll_pane.setScrollingDisabled(true, false);
        scroll_pane.setVariableSizeKnobs(true);

        preview_fbo = new LevelFrameBuffer(game, null);
        preview_fbo.set_puck_alpha(1);
        preview_fbo.set_draw_grid_lines(false);
    }

    public Table get_selector_table() {
        Image preview_image = new Image(preview_fbo.get_texture_region());
        preview_image.setScaling(Scaling.fit);

        Table selector_table = new Table();
        selector_table.add(scroll_pane).expand().fill().padBottom(10).row();
        selector_table.add(preview_image).size(Value.percentHeight(0.5f, selector_table));

        return selector_table;
    }

    public String get_selected_name() {
        return list.get_selected_name();
    }

    public void remove_selected_level() {
        String name = get_selected_name();
        if (name != null) {
            FileHandle dir = get_level_dir_fhandle(level_type, name);
            if (dir.exists())
                dir.deleteDirectory();

            list.remove_selected_level();
            preview_fbo.clear();
        }
        selected_level = null;
    }

    void remove_level_save(String level_name) {
        FileHandle file = get_level_save_fhandle(level_type, level_name);
        if (file.exists())
            file.delete();
    }

    public void add_level(String level_name) {
        if (!level_name.isEmpty()) {
            if (!list.contains(level_name)) {
                list.add_level(level_name, false);
            } else {
                list.select(level_name);
            }

            // necessary to actually scroll to the bottom
            scroll_pane.validate();
            scroll_pane.setScrollPercentY(list.get_selected_percent());  // scroll to the selected item
        }
    }

    static boolean level_file_exists(LEVEL_TYPE level_type, final String level_name) {
        return level_name != null && !level_name.isEmpty() && get_level_grid_fhandle(level_type, level_name).exists();
    }

    private Level load_selected_level(boolean force) {
        if (force) selected_level = null;
        return load_selected_level();
    }

    public Level load_selected_level() {
        if (selected_level != null)
            return selected_level;

        final String level_name = get_selected_name();
        if (level_name != null) {
            return new Level(level_name, level_type);
        }
        return null;
    }

    /** Returns whether any level is currently selected. */
    public boolean is_selected() {
        return list.get_selected_name() != null;
    }

    /** Returns whether there are any levels in the level list. */
    public boolean is_empty() {
        return list.get_level_list_size() == 0;
    }

    public static FileHandle get_level_grid_fhandle(LEVEL_TYPE level_type, String level_name) {
        if (level_type == LEVEL_TYPE.CUSTOM)
            return Gdx.files.local(String.format(Locale.US, "LEVELS/%s/%s/%s.grid", level_type.name(), level_name, level_name));
        return Gdx.files.internal(String.format(Locale.US, "LEVELS/%s/%s/%s.grid", level_type.name(), level_name, level_name));
    }

    /** Gets the level's save file, which is always in Gdx.files.local, since it has to be writable. */
    static FileHandle get_level_save_fhandle(LEVEL_TYPE level_type, String name) {
        return get_level_save_fhandle(level_type, name, Level.SAVE_TYPE.AUTO);
    }

    public static FileHandle get_level_save_fhandle(LEVEL_TYPE level_type, String name, Level.SAVE_TYPE save_type) {
        if (save_type == Level.SAVE_TYPE.QUICKSAVE)
            return Gdx.files.local(String.format(Locale.US, "LEVELS/%s/%s/%s.csave", level_type.name(), name, name));
        return Gdx.files.local(String.format(Locale.US, "LEVELS/%s/%s/%s.save", level_type.name(), name, name));  // AUTO
    }

    public static FileHandle get_level_dir_fhandle(LEVEL_TYPE level_type, String name) {
        if (level_type == LEVEL_TYPE.CUSTOM)
            return Gdx.files.local(String.format(Locale.US, "LEVELS/%s/%s/", level_type.name(), name));
        return Gdx.files.internal(String.format(Locale.US, "LEVELS/%s/%s/", level_type.name(), name));
    }

    public static FileHandle get_levels_dir_fhandle(LEVEL_TYPE level_type) {
        if (level_type == LEVEL_TYPE.CUSTOM)
            return Gdx.files.local(String.format(Locale.US, "LEVELS/%s/", level_type));
        return Gdx.files.internal(String.format(Locale.US, "LEVELS/%s/", level_type));
    }
}
