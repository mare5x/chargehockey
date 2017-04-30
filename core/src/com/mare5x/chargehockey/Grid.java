package com.mare5x.chargehockey;

import com.badlogic.gdx.utils.Array;

import java.util.Arrays;

enum GRID_ITEM {
    NULL,
    WALL,
    GOAL,
    PUCK;

    public static final GRID_ITEM[] values = values();
    // get the number of elements excluding PUCK
    public static int size() {
        return PUCK.ordinal();
    }
}

class Grid {
    private final static int WIDTH = ChargeHockeyGame.WORLD_WIDTH, HEIGHT = ChargeHockeyGame.WORLD_HEIGHT;

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

    byte[] get_byte_data() {
        byte[] data = new byte[grid.size];

        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (grid.get(i).ordinal());
        }

        return data;
    }

    // Load a grid using a byte array, as if from get_byte_data().
    void from_byte_data(byte[] data) {
        for (int i = 0; i < data.length; i++) {
            grid.set(i, GRID_ITEM.values[data[i]]);
        }
    }
}
