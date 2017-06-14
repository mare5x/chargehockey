package com.mare5x.chargehockey;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.StringBuilder;

enum GRID_ITEM {
    NULL('n'),
    WALL('w'),
    GOAL('g');

    private char code;  // item abbreviation code used to save the items in the file (instead of using .ordinal())
    GRID_ITEM(char code) {
        this.code = code;
    }

    static public GRID_ITEM from_code(char code) {
        switch (code) {
            case 'n': return NULL;
            case 'w': return WALL;
            case 'g': return GOAL;
        }
        return NULL;
    }

    public char code() {
        return code;
    }

    public static final GRID_ITEM[] values = values();

    // get the number of elements
    public static int size() {
        return GRID_ITEM.values.length;
    }
}

class Grid {
    private static int WIDTH = ChargeHockeyGame.WORLD_WIDTH, HEIGHT = ChargeHockeyGame.WORLD_HEIGHT;

    private final Array<GRID_ITEM> grid;

    Grid() {
        grid = new Array<GRID_ITEM>(true, WIDTH * HEIGHT);
        for (int i = 0; i < WIDTH * HEIGHT; i++)
            grid.add(GRID_ITEM.NULL);
    }

    private int get_idx(int row, int col) {
        if (row >= 0 && row < HEIGHT && col >= 0 && col < WIDTH) {
            return row * WIDTH + col;
        }
        return -1;
    }

    void set_item(int row, int col, GRID_ITEM item) {
        int idx = get_idx(row, col);
        if (idx != -1)
            grid.set(idx, item);
    }

    GRID_ITEM get_item(int row, int col) {
        int idx = get_idx(row, col);
        if (idx != -1)
            return grid.get(idx);
        return GRID_ITEM.NULL;
    }

    String get_grid_string() {
        StringBuilder grid_str = new StringBuilder();
        for (int i = 0; i < grid.size; i++) {
            grid_str.append(grid.get(i).code());
        }
        return grid_str.toString();
    }

    void load_from_grid_string(String grid_str) {
        for (int i = 0; i < grid_str.length(); i++) {
            grid.set(i, GRID_ITEM.from_code(grid_str.charAt(i)));
        }
    }

    void set_size(int width, int height) {
        WIDTH = width;
        HEIGHT = height;
        grid.setSize(width * height);
    }

    int get_height() {
        return HEIGHT;
    }

    int get_width() {
        return WIDTH;
    }
}
