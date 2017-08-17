package com.mare5x.chargehockey.level;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
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

import static com.mare5x.chargehockey.menus.BaseMenuScreen.MIN_BUTTON_HEIGHT;


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
            FileHandle save_file = LevelSelector.get_level_save_fhandle(level_type, name);
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

    /** Called whenever the selection changes. */
    interface SelectionListener {
        void changed(String selected);
    }

    private final ChargeHockeyGame game;
    private final SelectionListener selection_listener;

    private final Array<String> level_list = new Array<String>();

    private int selected_index = -1;
    private TextButton selected_button = null;

    LevelList(ChargeHockeyGame game, SelectionListener selection_listener, LEVEL_TYPE type) {
        super();

        this.game = game;
        this.selection_listener = selection_listener;

        FileHandle dir = LevelSelector.get_levels_dir_fhandle(type);
        if (!dir.exists())
            dir.mkdirs();

        // alphabetically sort the file list
        FileHandle[] file_list = dir.list();
        Sort.instance().sort(file_list, new NaturalOrderComparator());
        for (FileHandle child : file_list) {
            add_level(new LevelData(child.name(), type));
        }

        invalidateHierarchy();
    }

    private TextButton add_level(LevelData level) {
        final String name = level.name();
        level_list.add(name);

        final Table entry = new Table();

        final TextButton name_button = new TextButton(name, game.skin, "checked");
        name_button.getLabel().setWrap(true);
        name_button.pad(5);
        name_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                select(getChildren().indexOf(entry, true), name_button);
            }
        });

        entry.add(name_button).minHeight(MIN_BUTTON_HEIGHT).width(Value.percentWidth(0.6f, this)).padRight(10).expandX().fill();

        if (level.is_finished()) {
            entry.add(new Image(game.skin.getDrawable("star"))).size(MIN_BUTTON_HEIGHT);
        } else {
            entry.add(new Image(game.skin.getDrawable("star_empty"))).size(MIN_BUTTON_HEIGHT);
        }

        entry.pad(5);

        addActor(entry);

        return name_button;
    }

    void add_level(String level_name, boolean level_finished) {
        TextButton name_button = add_level(new LevelData(level_name, level_finished));
        invalidateHierarchy();
        select(level_list.size - 1, name_button);
    }

    void select(String level_name) {
        int index = level_list.indexOf(level_name, false);
        // ugly hack to select the appropriate button
        TextButton name_button = (TextButton) ((Table) (getChildren().get(index))).getChildren().get(0);
        select(index, name_button);
    }

    private void select(int index, TextButton name_button) {
        name_button.setChecked(true);
        if (index != selected_index) {
            if (selected_button != null) selected_button.setChecked(false);  // uncheck the previously selected button
            selected_index = index;
            selected_button = name_button;
            selection_listener.changed(level_list.get(index));
        }
    }

    void remove_selected_level() {
        remove_level(selected_index);
    }

    private void remove_level(int index) {
        removeActor(getChildren().get(index));
        level_list.removeIndex(index);
        if (index == selected_index)
            selected_index = -1;
        selected_button = null;
        invalidateHierarchy();
    }

    String get_selected_name() {
        if (selected_index != -1)
            return level_list.get(selected_index);
        return null;
    }

    boolean contains(String level_name) {
        return level_list.contains(level_name, false);
    }

    float get_selected_percent() {
        if (selected_index == -1 || level_list.size < 2)
            return 0;

        // because selected_button.getY() doesn't work, I have to manually calculate it
        float selected_y = 0;
        SnapshotArray<Actor> items = getChildren();
        for (int i = 0; i < selected_index; i++) {
            Table entry = (Table) items.get(i);
            selected_y += entry.getPrefHeight() + entry.getPadBottom() + entry.getPadTop();
        }
        return selected_y / getPrefHeight();
    }

    int get_level_list_size() {
        return level_list.size;
    }
}
