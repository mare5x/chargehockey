package com.mare5x.chargehockey.editor;

import com.badlogic.gdx.utils.Array;
import com.mare5x.chargehockey.level.Grid;

class MultiTileSetAction extends UndoableTileActionBase implements TileStateSaver {
    private final Array<TileState> prev_states;

    MultiTileSetAction() {
        this(16);
    }

    MultiTileSetAction(int size) {
        prev_states = new Array<TileState>(size);
    }

    @Override
    public void save_tile(int row, int col, Grid.GRID_ITEM item) {
        prev_states.add(new TileState(row, col, item));
    }

    @Override
    public void undo() {
        boolean update = false;
        for (TileState state : prev_states) {
            update = editor_interface.place_tile(state) || update;
        }
        if (update)
            editor_interface.update_grid();
    }
}
