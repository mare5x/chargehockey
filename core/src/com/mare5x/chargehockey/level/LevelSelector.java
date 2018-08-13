package com.mare5x.chargehockey.level;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.mare5x.chargehockey.ChargeHockeyGame;
import com.mare5x.chargehockey.level.Level.LEVEL_TYPE;
import com.mare5x.chargehockey.settings.GameDefaults;

public class LevelSelector {
    private final LEVEL_TYPE level_type;

    private final LevelList list;
    private final ScrollPane scroll_pane;

    private final LevelFrameBuffer preview_fbo;

    private Level selected_level = null;

    public LevelSelector(final ChargeHockeyGame game, LEVEL_TYPE level_type) {
        this.level_type = level_type;

        LevelList.LevelListCallback selection_listener = new LevelList.LevelListCallback() {
            @Override
            public void changed(String level_name) {
                if (level_name != null) {
                    load_level(level_name);
                    if (selected_level != null) {
                        preview_fbo.set_level(selected_level);
                        preview_fbo.update(game.batch);
                    }
                } else {
                    preview_fbo.clear();
                }
            }

            @Override
            void long_pressed(String level_name) {
                on_long_press(level_name);
            }
        };
        list = new LevelList(game, selection_listener, level_type);

        scroll_pane = new ScrollPane(list, game.skin);
        scroll_pane.setScrollingDisabled(true, false);
        scroll_pane.setVariableSizeKnobs(true);

        preview_fbo = new LevelFrameBuffer(game, null, true);
        preview_fbo.set_puck_alpha(1);
    }

    /** Override this method to handle long pressing. The entry will be selected
     *  if LevelList.select_on_long_press is true. */
    public void on_long_press(String level_name) { }

    public void set_select_on_long_press(boolean select_on_long_press) {
        list.set_select_on_long_press(select_on_long_press);
    }

    public void set_multiple_select(boolean multiple_select) {
        list.set_multi_select_enabled(multiple_select);
    }

    public Table get_selector_table() {
        Image preview_image = new Image(preview_fbo.get_texture_region());
        preview_image.setScaling(Scaling.fit);

        Table selector_table = new Table();
        selector_table.add(scroll_pane).expand().fill().padBottom(GameDefaults.ACTOR_PAD).row();
        selector_table.add(preview_image).size(Value.percentHeight(0.5f, selector_table));

        return selector_table;
    }

    public String get_selected_name() {
        return list.get_selected_name();
    }

    public Array<String> get_selected_names() {
        return list.get_selected_names();
    }

    public void remove_selected_level() {
        remove_selected_level(true);
    }

    private void remove_selected_level(boolean remove_entry) {
        if (selected_level == null) return;

        if (remove_entry) list.remove_selected_entry();
        preview_fbo.clear();
        remove_level_data(selected_level.get_name());

        selected_level = null;
    }

    /* Warning! the level data gets permanently removed. */
    private void remove_level_data(String level_name) {
        if (level_name != null) {
            FileHandle dir = Level.get_level_dir_fhandle(level_type, level_name);
            if (dir.exists())
                dir.deleteDirectory();
        }
    }

    public void add_level(String level_name) {
        if (!level_name.isEmpty()) {
            if (!list.contains(level_name)) {
                list.add_entry(level_name, false);
            } else {
                list.select(level_name);
            }

            scroll_to_selected();
        }
    }

    static boolean level_file_exists(LEVEL_TYPE level_type, final String level_name) {
        return level_name != null && !level_name.isEmpty() && Level.get_level_grid_fhandle(level_type, level_name).exists();
    }

    private Level load_selected_level(boolean force) {
        if (force) selected_level = null;
        return load_selected_level();
    }

    public Level load_selected_level() {
        if (selected_level != null)
            return selected_level;

        final String level_name = list.get_selected_name();
        return load_level(level_name);
    }

    private Level load_level(String level_name) {
        selected_level = null;
        if (level_name != null)
            selected_level = new Level(level_name, level_type);
        return selected_level;
    }

    // Returns true on success
    public boolean rename_selected_level(String new_name) {
        if (selected_level == null || new_name.isEmpty()) return false;
        if (new_name.equals(selected_level.get_name())) return true;
        if (list.contains(new_name)) return false;

        selected_level.rename(new_name);
        list.rename_selected_entry(new_name);

        return true;
    }

    public void select(String level_name) {
        list.select(level_name);
        scroll_to_selected();
    }

    private void scroll_to_selected() {
        Rectangle rect = list.get_selected_rect();

        // necessary to scroll to bottom
        scroll_pane.validate();
        scroll_pane.scrollTo(rect.x, rect.y, rect.width, rect.height, true, true);
    }

    /** Returns whether any level is currently selected. */
    public boolean is_selected() {
        return list.is_selected();
    }

    /** Returns whether there are any levels in the level list. */
    public boolean is_empty() {
        return list.is_empty();
    }

    /** Returns the number of levels. */
    public int get_level_count() {
        return list.get_size();
    }
}
