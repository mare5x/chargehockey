package com.mare5x.chargehockey;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;

import java.util.Locale;


class LevelSelector {
    private final ChargeHockeyGame game;

    private final LEVEL_TYPE level_type;

    private final List<String> list;
    private final ScrollPane scroll_pane;

    LevelSelector(ChargeHockeyGame game, LEVEL_TYPE level_type) {
        this.game = game;
        this.level_type = level_type;

        list = new List<String>(game.skin);
        init_list();

        scroll_pane = new ScrollPane(list, game.skin);
        scroll_pane.setVariableSizeKnobs(true);
    }

    private void init_list() {
        FileHandle dir = get_levels_dir_fhandle(level_type);
        if (!dir.exists())
            dir.mkdirs();

        for (FileHandle child : dir.list()) {
            list.getItems().add(child.nameWithoutExtension());
        }
    }

    private String get_selected_name() {
        int selected_idx = list.getSelectedIndex();
        if (selected_idx != -1)
            return list.getItems().get(selected_idx);
        return null;
    }

    ScrollPane get_display() {
        return scroll_pane;
    }

    void remove_selected_level() {
        int selected_idx = list.getSelectedIndex();
        if (selected_idx != -1) {
            String name = list.getItems().get(selected_idx);
            FileHandle dir = get_level_dir_fhandle(level_type, name);
            if (dir.exists())
                dir.deleteDirectory();

            list.getItems().removeIndex(selected_idx);
        }
    }

    void remove_level_save(String level_name) {
        FileHandle file = get_level_save_fhandle(level_type, level_name);
        if (file.exists())
            file.delete();
    }

    void add_level(String level_name) {
        if (!level_exists(level_name)) {
            create_level_file(level_name);

            list.getItems().add(level_name);
            list.invalidateHierarchy();  // reset the layout (add scroll bars to scroll pane)
        }
    }

    private void create_level_file(String level_name) {
        FileHandle file = get_level_grid_fhandle(level_type, level_name);

        if (file.exists())
            return;

        file.writeBytes(Level.get_empty_level_data(), false);
    }

    private boolean level_exists(final String level_name) {
        return list.getItems().contains(level_name, false);
    }

    Level load_selected_level() {
        final String level_name = get_selected_name();
        if (level_name != null) {
            Level level = new Level(game, level_name, level_type);
            level.load_grid_from_data(read_level_data(level_name));
            return level;
        }
        return null;
    }

    // Assumes that the level file exists!
    private byte[] read_level_data(String level_name) {
        FileHandle file = get_level_grid_fhandle(level_type, level_name);

        return file.readBytes();
    }

    static FileHandle get_level_grid_fhandle(LEVEL_TYPE level_type, String level_name) {
        return Gdx.files.local(String.format(Locale.US, "levels/%s/%s/%s.grid", level_type.name(), level_name, level_name));
    }

    static FileHandle get_level_save_fhandle(LEVEL_TYPE level_type, String name) {
        return Gdx.files.local(String.format(Locale.US, "levels/%s/%s/%s.save", level_type.name(), name, name));
    }

    static FileHandle get_level_dir_fhandle(LEVEL_TYPE level_type, String name) {
        return Gdx.files.local(String.format(Locale.US, "levels/%s/%s/", level_type.name(), name));
    }

    static FileHandle get_levels_dir_fhandle(LEVEL_TYPE level_type) {
        return Gdx.files.local(String.format(Locale.US, "levels/%s/", level_type));
    }
}
