package com.mare5x.chargehockey.editor;

import com.mare5x.chargehockey.level.Grid;

class TileSetAction extends UndoableTileActionBase {
    private TileState prev_state;

    TileSetAction(int row, int col, Grid.GRID_ITEM item) {
        this.prev_state = new TileState(row, col, item);
    }

    @Override
    public void undo() {
        if (editor_interface.place_tile(prev_state))
            editor_interface.update_grid();
    }
}
