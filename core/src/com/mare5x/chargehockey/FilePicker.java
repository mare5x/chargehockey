package com.mare5x.chargehockey;


import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;


abstract class FilePicker {
    interface FileFilter {
        boolean is_valid(FileHandle path);
    }

    interface EventListener {
        void dir_changed(FileHandle path);
    }

    private final List<String> dir_list;
    private final ScrollPane scroll_pane;

    private final FileFilter filter;
    private EventListener event_listener = null;

    private FileHandle current_dir;

    FilePicker(ChargeHockeyGame game) {
        this(game, new FileFilter() {
            @Override
            public boolean is_valid(FileHandle path) {
                return true;
            }
        });
    }

    FilePicker(ChargeHockeyGame game, FileFilter filter) {
        dir_list = new List<String>(game.skin);
        scroll_pane = new ScrollPane(dir_list, game.skin);
        this.filter = filter;

        // this is a click listener because the change listener conflicted with the scroll pane's dragging
        dir_list.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                show_selected_child();
            }
        });

//        show_dir(Gdx.files.external(""));
        show_dir(get_root_path());
    }

    void set_event_listener(EventListener event_listener) {
        this.event_listener = event_listener;
    }

    boolean is_valid(FileHandle path) {
        return filter.is_valid(path);
    }

    FileHandle get_current_path() {
        return current_dir;
    }

    FileHandle get_selected_path() {
        FileHandle path = get_selected_child();
        if (path != null) return path;
        return current_dir;
    }

    private FileHandle get_selected_child() {
        String selected = dir_list.getSelected();
        if (selected != null) return current_dir.child(selected);
        return null;
    }

    private void show_selected_child() {
        FileHandle child = get_selected_child();
        if (child != null && child.isDirectory())
            show_dir(child);
    }

    void show_parent_dir() {
        show_dir(current_dir.parent());
    }

    private void show_dir(FileHandle dir) {
        dir_list.clearItems();  // even if dir isn't a directory, the dir_list must get cleared

        if (!dir.isDirectory()) return;

        current_dir = dir;

        Array<String> dir_list_items = dir_list.getItems();
        for (FileHandle path : dir.list()) {
            if (filter.is_valid(path))
                dir_list_items.add(path.name());
        }

        dir_list.invalidateHierarchy();

        if (event_listener != null)
            event_listener.dir_changed(dir);
    }

    abstract FileHandle get_root_path();

    void refresh() {
        show_dir(current_dir);
    }

    ScrollPane get_display() {
        return scroll_pane;
    }
}
