package com.mare5x.chargehockey.level;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.StringBuilder;

public class Grid {
    public enum GRID_ITEM {
        NULL('n'),
        WALL('w'),
        GOAL('g'),
        BOUNCER('b');

        private char code;  // item abbreviation code used to save the items in the file (instead of using .ordinal())
        GRID_ITEM(char code) {
            this.code = code;
        }

        static public GRID_ITEM from_code(char code) {
            switch (code) {
                case 'n': return NULL;
                case 'w': return WALL;
                case 'g': return GOAL;
                case 'b': return BOUNCER;
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

    public static int WORLD_WIDTH = 64;
    public static int WORLD_HEIGHT = 64;
    public static final Rectangle WORLD_RECT = new Rectangle(0, 0, WORLD_WIDTH, WORLD_HEIGHT);

    public static final float UNIT = 1.0f;

    private final Array<GRID_ITEM> grid;

    Grid() {
        grid = new Array<GRID_ITEM>(true, WORLD_WIDTH * WORLD_HEIGHT);
        for (int i = 0; i < WORLD_WIDTH * WORLD_HEIGHT; i++)
            grid.add(GRID_ITEM.NULL);
    }

    private int get_idx(int row, int col) {
        if (row >= 0 && row < WORLD_HEIGHT && col >= 0 && col < WORLD_WIDTH) {
            return row * WORLD_WIDTH + col;
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

    /** A compressed string representation of the grid.
     * Tiles repeated more than once are compressed in the following manner:
     * NGRID_ITEM(code), where N is an integer > 1. */
    String get_grid_string() {
        StringBuilder grid_str = new StringBuilder();
        char prev = 'n', cur = 'n';
        for (int i = 1; i < grid.size; i++) {
            prev = grid.get(i - 1).code();
            cur = grid.get(i).code();
            int repeated = 1;
            while (prev == cur) {
                repeated++;
                if (i + 1 != grid.size)
                    cur = grid.get(++i).code();
                else
                    break;
            }
            if (repeated > 1)
                grid_str.append(repeated).append(prev);
            else
                grid_str.append(prev);
        }
        if (prev != cur)  // final edge case
            grid_str.append(cur);

        return grid_str.toString();
    }

    void load_from_grid_string(String grid_str) {
        int grid_idx = 0;
        for (int i = 0; i < grid_str.length(); i++) {
            int start_idx = i;
            char code = grid_str.charAt(i);
            String repeat_count_str = "";

            while (Character.isDigit(code)) {
                repeat_count_str += code;
                code = grid_str.charAt(++i);
            }

            int repeat_count = start_idx == i ? 1 : Integer.parseInt(repeat_count_str);
            GRID_ITEM current_tile = GRID_ITEM.from_code(code);
            for (int j = 0; j < repeat_count; j++)
                grid.set(grid_idx + j, current_tile);
            grid_idx += repeat_count;
        }
    }

    void set_size(int width, int height) {
        WORLD_WIDTH = width;
        WORLD_HEIGHT = height;
        grid.setSize(width * height);
    }

    int get_height() {
        return WORLD_HEIGHT;
    }

    int get_width() {
        return WORLD_WIDTH;
    }

    void clear() {
        for (int i = 0; i < grid.size; i++) {
            grid.set(i, GRID_ITEM.NULL);
        }
    }
}
