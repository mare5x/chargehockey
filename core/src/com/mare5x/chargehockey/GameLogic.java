package com.mare5x.chargehockey;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;


class GameLogic {
    private final ChargeHockeyGame game;
    private final Stage game_stage;
    private final Level level;

    private boolean is_playing = false;

    private Array<ChargeActor> charge_actors;
    private Array<ChargeActor> puck_actors;

    GameLogic(ChargeHockeyGame game, Stage game_stage, Level level) {
        this.game = game;
        this.game_stage = game_stage;
        this.level = level;

        charge_actors = new Array<ChargeActor>();
        puck_actors = new Array<ChargeActor>();

        // replace puck positions in level with null items and instead place puck charge actors
        for (Vector2 pos : level.get_puck_positions()) {
            level.set_item((int) (pos.y), (int) (pos.x), GRID_ITEM.NULL);

            ChargeActor puck = new ChargeActor(game, CHARGE.PUCK);
            puck.setPosition(pos.x, pos.y);

            puck_actors.add(puck);
            game_stage.addActor(puck);
        }
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

        for (ChargeActor puck : puck_actors) {
            for (ChargeActor charge : charge_actors) {
//                apply_force(puck, charge);
            }
        }
    }

    void set_playing(boolean value) {
        is_playing = value;
    }

    boolean is_playing() {
        return is_playing;
    }
}
