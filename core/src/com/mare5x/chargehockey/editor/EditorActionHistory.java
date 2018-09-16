package com.mare5x.chargehockey.editor;

import com.mare5x.chargehockey.game.ActionHistory;
import com.mare5x.chargehockey.game.UndoableChargeAction;

class EditorActionHistory extends ActionHistory {
    EditorActionHistory(UndoableChargeAction.ChargeActionInterface charge_interface) {
        super(charge_interface);
    }
}
