package com.mare5x.chargehockey.level;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.SnapshotArray;
import com.badlogic.gdx.utils.Sort;
import com.badlogic.gdx.utils.StreamUtils;
import com.mare5x.chargehockey.ChargeHockeyGame;
import com.mare5x.chargehockey.level.Level.LEVEL_TYPE;

import java.io.BufferedReader;
import java.io.IOException;

import static com.mare5x.chargehockey.settings.GameDefaults.ACTOR_PAD;
import static com.mare5x.chargehockey.settings.GameDefaults.MAX_BUTTON_WIDTH;
import static com.mare5x.chargehockey.settings.GameDefaults.MIN_BUTTON_HEIGHT;

/* A group of LevelListEntry entries. */
class LevelList extends VerticalGroup {
    private static class LevelData {
        private final String name;
        private final boolean level_finished;

        LevelData(String name, boolean finished) {
            this.name = name;
            level_finished = finished;
        }

        LevelData(String name, LEVEL_TYPE level_type) {
            this.name = name;
            level_finished = check_if_finished(name, level_type);
        }

        private boolean check_if_finished(String name, LEVEL_TYPE level_type) {
            FileHandle save_file = Level.get_level_save_fhandle(level_type, name);
            if (!save_file.exists()) return false;

            BufferedReader reader = save_file.reader(8, "UTF-8");
            try {
                // the first line contains a flag that determines if the level has been completed
                String header = reader.readLine();
                return header != null && header.equals("1");
            } catch (IOException e) {
                throw new GdxRuntimeException("Error reading file", e);
            } finally {
                StreamUtils.closeQuietly(reader);
            }
        }

        String name() {
            return name;
        }

        boolean is_finished() {
            return level_finished;
        }
    }

    class LevelListEntry extends Table {
        private final TextButton name_button;
        private String name;
        private boolean selected;

        LevelListEntry(String name, boolean level_finished) {
            this.name = name;

            name_button = new TextButton(name, game.skin, "checked");
            name_button.getLabel().setWrap(true);
            name_button.pad(5);
            // All listeners are notified of events, so simply use a click and longPress listener
            name_button.addListener(new ActorGestureListener() {
                @Override
                public boolean longPress(Actor actor, float x, float y) {
                    if (select_on_long_press)
                        LevelList.this.select(LevelListEntry.this);
                    callbacks.long_pressed(get_name());
                    return false;
                }
            });
            name_button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    LevelList.this.select(LevelListEntry.this);
                }
            });

            add(name_button).minHeight(MIN_BUTTON_HEIGHT).width(MAX_BUTTON_WIDTH).padRight(ACTOR_PAD);

            add(new Image(game.skin.getDrawable(level_finished ? "star" : "star_empty"))).size(MIN_BUTTON_HEIGHT);

            pad(5);
        }

        final String get_name() { return name; }

        void set_name(String new_name) {
            name = new_name;
            name_button.setText(new_name);
        }

        int get_index() {
            return LevelList.this.getChildren().indexOf(LevelListEntry.this, true);
        }

        boolean is_selected() { return selected; }

        void unselect() {
            selected = false;
            --entries_selected;
            name_button.setChecked(false);
        }

        void select() {
            selected = true;
            ++entries_selected;
            name_button.setChecked(true);
        }

        void toggle() {
            if (is_selected()) unselect();
            else select();
        }
    }

    abstract static class LevelListCallback {
        abstract void changed(String level_name);  // Called whenever the selection changes
        void long_pressed(String level_name) {  }  // select_on_long_press determines if this entry gets selected
    }

    private final ChargeHockeyGame game;
    private final LevelListCallback callbacks;

    private int entries_selected = 0;
    private LevelListEntry selected_entry = null;

    private boolean select_on_long_press = false;
    private boolean multi_select_enabled = false;  // allows multiple entries to be selected

    LevelList(ChargeHockeyGame game, LevelListCallback callbacks, LEVEL_TYPE type) {
        super();

        this.game = game;
        this.callbacks = callbacks;

        FileHandle dir = Level.get_levels_dir_fhandle(type);
        if (!dir.exists())
            dir.mkdirs();

        // alphabetically sort the file list
        FileHandle[] file_list = dir.list();
        Sort.instance().sort(file_list, new NaturalOrderComparator());
        for (FileHandle child : file_list) {
            add_entry(new LevelData(child.name(), type));
        }

        invalidateHierarchy();
    }

    void set_select_on_long_press(boolean select_on_long_press) { this.select_on_long_press = select_on_long_press; }

    void set_multi_select_enabled(boolean multi_select_enabled) { this.multi_select_enabled = multi_select_enabled; }

    private LevelListEntry add_entry(LevelData level) {
        LevelListEntry entry = new LevelListEntry(level.name(), level.is_finished());
        addActor(entry);
        return entry;
    }

    void add_entry(String level_name, boolean level_finished) {
        LevelListEntry entry = add_entry(new LevelData(level_name, level_finished));
        invalidateHierarchy();
        select(entry);
    }

    void select(String level_name) {
        int index = find_index(level_name);
        LevelListEntry entry = (LevelListEntry) getChildren().get(index);
        select(entry);
    }

    private void select(LevelListEntry entry) {
        if (multi_select_enabled) entry.toggle();
        else entry.select();

        if (entry != selected_entry) {
            if (selected_entry != null && !multi_select_enabled) selected_entry.unselect();
            selected_entry = entry;
            callbacks.changed(entry.get_name());
        }

        // only in multi select mode can an entry be unselected
        if (!entry.is_selected()) {
            selected_entry = null;
            callbacks.changed(null);
        }
    }

    void remove_selected_entry() {
        if (selected_entry != null)
            remove_entry(selected_entry);
    }

    private void remove_entry(LevelListEntry entry) {
        removeActor(entry);
        if (entry == selected_entry)
            selected_entry = null;
        invalidateHierarchy();
    }

    void rename_selected_entry(String new_name) {
        if (selected_entry != null)
            rename_entry(selected_entry, new_name);
    }

    private void rename_entry(LevelListEntry entry, String new_name) {
        entry.set_name(new_name);
    }

    String get_selected_name() {
        if (selected_entry != null)
            return selected_entry.get_name();
        return null;
    }

    Array<String> get_selected_names() {
        Array<String> names = new Array<String>();

        SnapshotArray<Actor> entries = getChildren();
        Actor[] items = entries.begin();
        for (int i = 0, n = entries.size; i < n; ++i) {
            LevelListEntry entry = (LevelListEntry) items[i];
            if (entry.is_selected())
                names.add(entry.get_name());
        }
        entries.end();

        return names;
    }

    boolean is_selected() {
        return entries_selected > 0;
    }

    boolean is_empty() {
        return getChildren().size == 0;
    }

    boolean contains(String level_name) {
        return find_index(level_name) != -1;
    }

    private int find_index(String level_name) {
        SnapshotArray<Actor> entries = getChildren();
        Actor[] items = entries.begin();
        for (int i = 0, n = entries.size; i < n; ++i) {
            LevelListEntry entry = (LevelListEntry) items[i];
            if (entry.get_name().equals(level_name))
                return i;
        }
        entries.end();
        return -1;
    }

    Rectangle get_selected_rect() {
        Vector2 xy = selected_entry.localToParentCoordinates(new Vector2(0, 0));
        return new Rectangle(xy.x, xy.y, selected_entry.getPrefWidth(), selected_entry.getPrefHeight());
    }

    /* Returns the number of entries in the list. */
    int get_size() {
        return getChildren().size;
    }
}
