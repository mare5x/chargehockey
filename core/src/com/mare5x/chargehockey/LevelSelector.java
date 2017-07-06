package com.mare5x.chargehockey;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Scaling;

import java.util.Locale;


class LevelSelector {
    private final ChargeHockeyGame game;

    private final LEVEL_TYPE level_type;

    private final List<String> list;

    private final LevelFrameBuffer preview_fbo;

    private Level selected_level = null;

    LevelSelector(final ChargeHockeyGame game, LEVEL_TYPE level_type) {
        this.game = game;
        this.level_type = level_type;

        list = new List<String>(game.skin);
        list.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                selected_level = load_selected_level(true);
                if (selected_level != null) {
                    preview_fbo.set_level(selected_level);
                    preview_fbo.update(game.batch);
                }
            }
        });
        init_list();

        preview_fbo = new LevelFrameBuffer(game, null);
        preview_fbo.set_puck_alpha(1);
        preview_fbo.set_draw_grid_lines(false);
    }

    Table get_selector_table() {
        ScrollPane scroll_pane = new ScrollPane(list, game.skin);
        scroll_pane.setVariableSizeKnobs(true);

        Image preview_image = new Image(preview_fbo.get_texture_region());
        preview_image.setScaling(Scaling.fit);

        Table selector_table = new Table();
        selector_table.add(scroll_pane).expand().fill().padBottom(10).row();
        selector_table.add(preview_image).size(Value.percentHeight(0.5f, selector_table));

        return selector_table;
    }

    private void init_list() {
        FileHandle dir = get_levels_dir_fhandle(level_type);
        if (!dir.exists())
            dir.mkdirs();

        for (FileHandle child : dir.list()) {
            list.getItems().add(child.nameWithoutExtension());
        }

        list.invalidateHierarchy();
    }

    private String get_selected_name() {
        int selected_idx = list.getSelectedIndex();
        if (selected_idx != -1)
            return list.getItems().get(selected_idx);
        return null;
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
        if (!level_name.isEmpty() && !level_exists(level_name)) {
            list.getItems().add(level_name);
            list.invalidateHierarchy();  // reset the layout (add scroll bars to scroll pane)
        }
    }

    private boolean level_exists(final String level_name) {
        return list.getItems().contains(level_name, false);
    }

    static boolean level_file_exists(LEVEL_TYPE level_type, final String level_name) {
        return level_name != null && !level_name.isEmpty() && get_level_grid_fhandle(level_type, level_name).exists();
    }

    private Level load_selected_level(boolean force) {
        if (force) selected_level = null;
        return load_selected_level();
    }

    Level load_selected_level() {
        if (selected_level != null)
            return selected_level;

        final String level_name = get_selected_name();
        if (level_name != null) {
            return new Level(level_name, level_type);
        }
        return null;
    }

    static FileHandle get_level_grid_fhandle(LEVEL_TYPE level_type, String level_name) {
        return Gdx.files.local(String.format(Locale.US, "LEVELS/%s/%s/%s.grid", level_type.name(), level_name, level_name));
    }

    static FileHandle get_level_save_fhandle(LEVEL_TYPE level_type, String name) {
        return Gdx.files.local(String.format(Locale.US, "LEVELS/%s/%s/%s.save", level_type.name(), name, name));
    }

    static FileHandle get_level_dir_fhandle(LEVEL_TYPE level_type, String name) {
        return Gdx.files.local(String.format(Locale.US, "LEVELS/%s/%s/", level_type.name(), name));
    }

    static FileHandle get_levels_dir_fhandle(LEVEL_TYPE level_type) {
        return Gdx.files.local(String.format(Locale.US, "LEVELS/%s/", level_type));
    }
}
