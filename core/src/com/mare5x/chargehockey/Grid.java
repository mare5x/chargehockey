package com.mare5x.chargehockey;

import com.badlogic.gdx.utils.Array;

enum GRID_ITEM {
    NULL,
    WALL,
    GOAL;
}

class Grid {
    private final static int WIDTH = ChargeHockeyGame.WORLD_WIDTH, HEIGHT = ChargeHockeyGame.WORLD_HEIGHT;

    private final Array<GRID_ITEM> grid;

    public Grid() {
        grid = new Array<GRID_ITEM>(true, WIDTH * HEIGHT);
        for (int i = 0; i < WIDTH * HEIGHT; i++)
            grid.add(GRID_ITEM.NULL);
    }

    public int get_idx(int row, int col) {
        if (row >= 0 && row < HEIGHT && col >= 0 && col < WIDTH) {
            return row * WIDTH + col;
        }
        return -1;
    }

    public void set_item(int row, int col, GRID_ITEM item) {
        int idx = get_idx(row, col);
        if (idx != -1)
            grid.set(idx, item);
    }

    public GRID_ITEM get_item(int row, int col) {
        int idx = get_idx(row, col);
        if (idx != -1)
            return grid.get(idx);
        return GRID_ITEM.NULL;
    }
}
