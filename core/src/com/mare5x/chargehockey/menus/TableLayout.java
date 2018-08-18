package com.mare5x.chargehockey.menus;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;

public abstract class TableLayout extends Table {
    private Array<TableLayout> children = new Array<TableLayout>(1);  // TableLayout children that will get resized

    private float prev_aspect_ratio = -1;

    private boolean resizable = true;  // set to false to ignore resizing of this instance (doesn't affect children)
    private boolean resize_children = true;  // set to false to ignore resizing children

    public TableLayout() {
        super();
    }

    public TableLayout(Skin skin) {
        super(skin);
    }

    public void set_resizable(boolean val) {
        resizable = val;
    }

    public void set_resize_children(boolean val) {
        resize_children = val;
    }

    public void add_layout(TableLayout child) {
        children.add(child);
    }

    public abstract void portrait();

    public abstract void landscape();

    private void resize(boolean portrait) {
        if (resize_children) {
            for (TableLayout child : children)
                child.resize(portrait);
        }
        if (resizable) {
            clear();
            if (portrait) portrait();
            else landscape();
        }
    }

    /* Resize only if the aspect ratio drastically changes. */
    public void resize(int width, int height) {
        float aspect_ratio = width / (float) height;
        if (aspect_ratio > 1.0f) {
            if (prev_aspect_ratio < 0 || prev_aspect_ratio <= 1.0f)
                resize(false);
        }
        else {  // <= 1.0f
            if (prev_aspect_ratio < 0 || prev_aspect_ratio > 1.0f)
                resize(true);
        }
        prev_aspect_ratio = aspect_ratio;
    }

    public void resize_children(int width, int height) {
        for (TableLayout child : children)
            child.resize(width, height);
    }
}
