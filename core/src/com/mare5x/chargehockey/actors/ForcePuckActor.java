package com.mare5x.chargehockey.actors;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.mare5x.chargehockey.ChargeHockeyGame;

/** Puck sprite that can show (force) vectors. */
public class ForcePuckActor extends ChargeActor {
    final TextureAtlas.AtlasRegion vector_region;

    private final ObjectMap<ChargeActor, Sprite> force_sprites = new ObjectMap<ChargeActor, Sprite>();

    private float puck_alpha = 1f;
    private float vector_alpha = 1f;

    // force vector colors
    public static final Color POS_RED = new Color(1, 0, 0, 1);
    public static final Color NEG_BLUE = new Color(0, 0.58f, 1, 1);

    // vector sprite settings
    static final float _MAX_LENGTH = ChargeHockeyGame.WORLD_WIDTH * 0.8f;
    private static final float _MIN_VEC_HEIGHT = 0.6f;
    private static final float _VEC_HEIGHT_SCL = 1.5f;  // manually check blank_vector.png arrow's tail height

    private static boolean DRAW_FORCES = true;

    public static final float SIZE = 1;
    public static float RADIUS = SIZE / 2f;

    ForcePuckActor(ChargeHockeyGame game, CHARGE type, DragCallback callback) {
        super(game, type, callback);

        vector_region = game.sprites.findRegion("blank_vector");
    }

    public ForcePuckActor(ChargeHockeyGame game) {
        this(game, CHARGE.PUCK, null);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (DRAW_FORCES) {
            for (Sprite force_sprite : force_sprites.values())
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

    void prepare_vector_sprite(Sprite sprite, Vector2 vector, float length) {
        if (MathUtils.isZero(length, 10e-5f)) {
            sprite.setSize(0, 0);
            return;
        }
        float height = Math.max(length / (_MAX_LENGTH), _MIN_VEC_HEIGHT);  // sprite width
        height *= _VEC_HEIGHT_SCL;
        sprite.setBounds(get_x(), get_y() - height / 2, length, height);
        sprite.setOrigin(0, height / 2);  // rotate around the center of the puck
        sprite.setRotation(vector.angle());
    }

    void reset_sprites() {
        for (Sprite force_sprite : force_sprites.values())
            force_sprite.setSize(0, 0);
    }

    public void clear_sprites() {
        force_sprites.clear();
    }

    private Sprite get_force_sprite(ChargeActor charge, Vector2 force) {
        Sprite force_sprite = force_sprites.get(charge, new Sprite(vector_region));
        float len = Math.min(force.scl(1 / get_weight() / 2).len(), _MAX_LENGTH);  // shorten the length
        prepare_vector_sprite(force_sprite, force, len);
        if (charge.get_type() == CHARGE.POSITIVE)
            force_sprite.setColor(POS_RED);
        else
            force_sprite.setColor(NEG_BLUE);
        force_sprite.setAlpha(Math.min(len * 2 / _MAX_LENGTH, 0.8f));
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
