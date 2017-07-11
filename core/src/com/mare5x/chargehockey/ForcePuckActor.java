package com.mare5x.chargehockey;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ObjectMap;

/** Puck sprite that can show (force) vectors. */
class ForcePuckActor extends ChargeActor {
    final TextureAtlas.AtlasRegion vector_region;

    private final ObjectMap<ChargeActor, Sprite> force_sprites = new ObjectMap<ChargeActor, Sprite>();

    private float alpha = 1f;

    // force vector colors
    private static final Color POS_RED = new Color(1, 0, 0, 1);
    private static final Color NEG_BLUE = new Color(0, 0.58f, 1, 1);

    // vector sprite settings
    static final float _MAX_LENGTH = ChargeHockeyGame.WORLD_WIDTH;
    private static final float _MIN_VEC_HEIGHT = 0.6f;
    private static final float _VEC_HEIGHT_SCL = 1.5f;  // manually check blank_vector.png arrow's tail height

    private static boolean DRAW_FORCES = true;

    ForcePuckActor(ChargeHockeyGame game, CHARGE type, DragCallback callback) {
        super(game, type, callback);

        vector_region = game.sprites.findRegion("blank_vector");
    }

    ForcePuckActor(ChargeHockeyGame game) {
        this(game, CHARGE.PUCK, null);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (DRAW_FORCES) {
            for (Sprite force_sprite : force_sprites.values())
                force_sprite.draw(batch, alpha);
        }

        // draw the sprite after the vectors, so it's on top
        super.draw(batch, parentAlpha * alpha);
    }

    void set_alpha(float value) {
        alpha = value;
    }

    void prepare_vector_sprite(Sprite sprite, Vector2 vector, float length) {
        if (MathUtils.isZero(length, 10e-5f)) {
            sprite.setSize(0, 0);
            return;
        }
        float height = Math.max(length / (_MAX_LENGTH), _MIN_VEC_HEIGHT);  // sprite width
        height *= _VEC_HEIGHT_SCL;
        sprite.setBounds(getX(Align.center), getY(Align.center) - height / 2, length, height);
        sprite.setOrigin(0, height / 2);  // rotate around the center of the puck
        sprite.setRotation(vector.angle());
    }

    void reset_sprites() {
        for (Sprite force_sprite : force_sprites.values())
            force_sprite.setSize(0, 0);
    }

    private Sprite get_force_sprite(ChargeActor charge, Vector2 force) {
        Sprite force_sprite = force_sprites.get(charge, new Sprite(vector_region));
        float len = Math.min(force.scl(1 / get_weight()).len(), _MAX_LENGTH);
        prepare_vector_sprite(force_sprite, force, len);
        if (charge.get_type() == CHARGE.POSITIVE)
            force_sprite.setColor(POS_RED);
        else
            force_sprite.setColor(NEG_BLUE);
        force_sprite.setAlpha(Math.min(len / _MAX_LENGTH, 0.8f));
        return force_sprite;
    }

    void set_force(ChargeActor charge, Vector2 force_vec) {
        force_sprites.put(charge, get_force_sprite(charge, force_vec));
    }

    void remove_force(ChargeActor charge) {
        force_sprites.remove(charge);
    }

    static void set_draw_forces(boolean draw) {
        DRAW_FORCES = draw;
    }

    static boolean get_draw_forces() {
        return DRAW_FORCES;
    }
}