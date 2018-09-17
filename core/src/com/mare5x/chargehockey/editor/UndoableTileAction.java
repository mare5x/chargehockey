package com.mare5x.chargehockey.editor;

import com.mare5x.chargehockey.level.Grid.GRID_ITEM;

interface UndoableTileAction {
    interface EditorInterface {
        boolean place_tile(TileState state);
        void update_grid();
    }

    void undo();
}

abstract class UndoableTileActionBase implements UndoableTileAction {
    static EditorInterface editor_interface;
}

interface TileStateSaver {
    void save_tile(int row, int col, GRID_ITEM item);
}

class TileState {
    int row, col;
    GRID_ITEM item;

    TileState(int row, int col, GRID_ITEM item) {
        this.row = row;
        this.col = col;
        this.item = item;
    }
}
