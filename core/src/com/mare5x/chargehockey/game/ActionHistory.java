package com.mare5x.chargehockey.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.mare5x.chargehockey.actors.ChargeActor;

public class ActionHistory {
    private final Array<UndoableChargeAction> charge_history = new Array<UndoableChargeAction>();

    public ActionHistory(UndoableChargeAction.ChargeActionInterface charge_interface) {
        UndoableChargeActionBase.actions_interface = charge_interface;
    }

    public void save(UndoableChargeAction action) {
        charge_history.add(action);
        Gdx.app.log("ActionHistory", "saved action");
    }

    public void undo() {
        if (charge_history.size == 0) return;

        UndoableChargeAction action = charge_history.pop();
        int uid = action.get_charge_uid();
        ChargeActor charge = UndoableChargeActionBase.actions_interface.find(uid);
        action.undo(charge);
    }

    public void clear() {
        charge_history.clear();
    }
}
