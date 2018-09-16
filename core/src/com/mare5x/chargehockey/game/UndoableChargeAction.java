package com.mare5x.chargehockey.game;

import com.mare5x.chargehockey.actors.ChargeActor;

public interface UndoableChargeAction {
    interface ChargeActionInterface {
        ChargeActor add_charge(ChargeActor.ChargeState state);

        void remove_charge(ChargeActor charge);

        ChargeActor find(int uid);
    }

    void undo(ChargeActor charge);

    int get_charge_uid();
}

abstract class UndoableChargeActionBase implements UndoableChargeAction {
    static ChargeActionInterface actions_interface;
}
