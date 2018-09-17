package com.mare5x.chargehockey.editor;

import com.badlogic.gdx.utils.Array;
import com.mare5x.chargehockey.game.ActionHistory;
import com.mare5x.chargehockey.game.UndoableChargeAction;
import com.mare5x.chargehockey.level.Grid;

class EditorActionHistory extends ActionHistory implements TileStateSaver {
    enum ActionClass { CHARGE, TILE }

    private Array<UndoableTileAction> tile_history = new Array<UndoableTileAction>();

    // Keeps track of which type of action is to be undone next.
    private Array<ActionClass> type_tracker = new Array<ActionClass>();

    EditorActionHistory(UndoableChargeAction.ChargeActionInterface charge_interface, UndoableTileAction.EditorInterface editor_interface) {
        super(charge_interface);
        UndoableTileActionBase.editor_interface = editor_interface;
    }

    @Override
    public void save(UndoableChargeAction action) {
        super.save(action);
        type_tracker.add(ActionClass.CHARGE);
    }

    public void save(UndoableTileAction action) {
        tile_history.add(action);
        type_tracker.add(ActionClass.TILE);
    }

    @Override
    public void save_tile(int row, int col, Grid.GRID_ITEM item) {
        save(new TileSetAction(row, col, item));
    }

    @Override
    public void undo() {
        if (type_tracker.size == 0) return;

        ActionClass action_type = type_tracker.pop();
        if (action_type == ActionClass.CHARGE)
            super.undo();
        else {
            UndoableTileAction action = tile_history.pop();
            action.undo();
        }
    }

    @Override
    public void clear() {
        super.clear();
        tile_history.clear();
    }
}
