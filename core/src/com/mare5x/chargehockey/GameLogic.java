package com.mare5x.chargehockey;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;


enum CHARGE {
    POSITIVE, NEGATIVE
}

class GameLogic {
    private final ChargeHockeyGame game;
    private final Stage game_stage;

    private boolean is_playing = false;

    private Array<ChargeActor> charge_actors;

    GameLogic(ChargeHockeyGame game, Stage game_stage) {
        this.game = game;
        this.game_stage = game_stage;

        charge_actors = new Array<ChargeActor>();
    }

    // Add a charge of type charge_type to the center of the camera position.
    void add_charge(CHARGE charge_type) {
        ChargeActor charge = new ChargeActor(game, charge_type);
        charge.setPosition(game_stage.getCamera().position.x, game_stage.getCamera().position.y);

        charge_actors.add(charge);
        game_stage.addActor(charge);
    }

    void update() {

    }

    void set_playing(boolean value) {
        is_playing = value;
    }

    boolean is_playing() {
        return is_playing;
    }
}
