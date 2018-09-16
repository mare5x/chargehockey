package com.mare5x.chargehockey.game;

import com.mare5x.chargehockey.actors.ChargeActor;

public class ChargeAddAction extends UndoableChargeActionBase {
    private int charge_uid;

    ChargeAddAction(int uid) {
        this.charge_uid = uid;
    }

    public void undo(ChargeActor charge) {
        ChargeActor partner = charge.get_partner();
        actions_interface.remove_charge(charge);
        if (partner != null)
            actions_interface.remove_charge(partner);
    }

    @Override
    public int get_charge_uid() {
        return charge_uid;
    }
}
