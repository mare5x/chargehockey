package com.mare5x.chargehockey;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;


class GameLogic {
    private final ChargeHockeyGame game;
    private final Stage game_stage;
    private final Level level;

    private boolean is_playing = false;

    private Array<ChargeActor> charge_actors;

    GameLogic(ChargeHockeyGame game, Stage game_stage, Level level) {
        this.game = game;
        this.game_stage = game_stage;
        this.level = level;

        charge_actors = new Array<ChargeActor>();

//        level.get_pucks();
    }

    // Add a charge of type charge_type to the center of the camera position.
    final ChargeActor add_charge(CHARGE charge_type) {
        ChargeActor charge = new ChargeActor(game, charge_type);
        charge.setPosition(game_stage.getCamera().position.x, game_stage.getCamera().position.y);

        charge_actors.add(charge);
        game_stage.addActor(charge);

        return charge;
    }

    void update() {
        if (!is_playing())
            return;

//        for (Puck puck : pucks) {
//            for (ChargeActor charge : charge_actors) {
//                apply_force(puck, charge);
//            }
//        }
    }

    void set_playing(boolean value) {
        is_playing = value;
    }

    boolean is_playing() {
        return is_playing;
    }
}
