package com.mare5x.chargehockey.game;

import com.mare5x.chargehockey.actors.ChargeActor;
import com.mare5x.chargehockey.actors.ChargeActor.ChargeState;

public class ChargeRemoveAction extends UndoableChargeActionBase {
    private final ChargeState prev_state;

    public ChargeRemoveAction(ChargeState prev_state) {
        this.prev_state = prev_state;
    }

    public void undo(ChargeActor charge) {
        charge = actions_interface.add_charge(prev_state);
        if (prev_state.partner != null) {
            ChargeActor partner = actions_interface.find(prev_state.partner.uid);
            if (partner == null)
                partner = actions_interface.add_charge(prev_state.partner);
            partner.set_partner(charge);
            charge.set_partner(partner);
        }
    }

    @Override
    public int get_charge_uid() {
        return prev_state.uid;
    }
}
