package com.mare5x.chargehockey.game;

import com.mare5x.chargehockey.actors.ChargeActor;

import static com.mare5x.chargehockey.actors.ChargeActor.ChargeState;

public class ChargeMoveAction extends UndoableChargeActionBase {
    private final ChargeState prev_state;

    public ChargeMoveAction(ChargeState prev_state) {
        this.prev_state = prev_state;
    }

    @Override
    public int get_charge_uid() {
        return prev_state.uid;
    }

    @Override
    public void undo(ChargeActor charge) {
        charge.set_position(prev_state.x, prev_state.y);
        if (prev_state.partner != null) {
            ChargeActor partner = charge.get_partner();
            if (partner == null) { // charge was removed
                partner = actions_interface.add_charge(prev_state.partner);
                charge.set_partner(partner);
                partner.set_partner(charge);
            }
            partner.set_position(prev_state.partner.x, prev_state.partner.y);
        }
    }
}
