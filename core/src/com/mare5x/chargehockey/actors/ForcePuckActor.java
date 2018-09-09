package com.mare5x.chargehockey.actors;


import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.mare5x.chargehockey.ChargeHockeyGame;

import static com.mare5x.chargehockey.settings.GameDefaults.NEG_BLUE;
import static com.mare5x.chargehockey.settings.GameDefaults.POS_RED;

/** Puck sprite that can show (force) vectors. */
public class ForcePuckActor extends ChargeActor {
    protected final ChargeHockeyGame game;

    private final ObjectMap<ChargeActor, VectorSprite> force_sprites = new ObjectMap<ChargeActor, VectorSprite>();

    private float puck_alpha = 1f;
    private float vector_alpha = 1f;

    private static boolean DRAW_FORCES = true;

    public static final float SIZE = 1;
    public static float RADIUS = SIZE / 2f;

    ForcePuckActor(ChargeHockeyGame game, CHARGE type, DragCallback callback) {
        super(game, type, callback);
        this.game = game;
    }

    public ForcePuckActor(ChargeHockeyGame game) {
        this(game, CHARGE.PUCK, null);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (DRAW_FORCES) {
            for (VectorSprite force_sprite : force_sprites.values())
                force_sprite.draw(batch, vector_alpha);
        }

        // draw the sprite after the vectors, so it's on top
        super.draw(batch, parentAlpha * puck_alpha);
    }

    public void set_alpha(float value) {
        puck_alpha = value;
        vector_alpha = value;
    }

    public void set_puck_alpha(float value) {
        puck_alpha = value;
    }

    public void set_vector_alpha(float value) {
        vector_alpha = value;
    }

    void reset_sprites() {
        for (VectorSprite force_sprite : force_sprites.values())
            force_sprite.setSize(0, 0);
    }

    public void clear_sprites() {
        force_sprites.clear();
    }

    private VectorSprite get_force_sprite(ChargeActor charge, Vector2 force) {
        VectorSprite force_sprite = force_sprites.get(charge, null);
        if (force_sprite == null) {
            force_sprite = new VectorSprite(game);
            if (charge.get_type() == CHARGE.POSITIVE)
                force_sprite.setColor(POS_RED);
            else
                force_sprite.setColor(NEG_BLUE);
        }
        float len = Math.min(force.len() * 4, VectorSprite.MAX_LENGTH);
        force_sprite.prepare(get_x(), get_y(), force.angle(), len);
        force_sprite.setAlpha(MathUtils.clamp(len / VectorSprite.MAX_LENGTH, 0.2f, 0.6f));
        return force_sprite;
    }

    public void set_force(ChargeActor charge, Vector2 force_vec) {
        force_sprites.put(charge, get_force_sprite(charge, force_vec));
    }

    public void remove_force(ChargeActor charge) {
        force_sprites.remove(charge);
    }

    public static void set_draw_forces(boolean draw) {
        DRAW_FORCES = draw;
    }

    public static boolean get_draw_forces() {
        return DRAW_FORCES;
    }
}
