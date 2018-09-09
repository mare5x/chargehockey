package com.mare5x.chargehockey.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.mare5x.chargehockey.ChargeHockeyGame;
import com.mare5x.chargehockey.level.Grid;

import static com.mare5x.chargehockey.settings.GameDefaults.PHYSICS_EPSILON;

public class VectorSprite extends Sprite {
    private static TextureRegion tail_region;
    private static TextureAtlas.AtlasRegion head_region;

    private final Sprite head_sprite;

    public static final float MAX_LENGTH = Grid.WORLD_WIDTH;
    private static final float MIN_TAIL_WIDTH = 0.6f;

    VectorSprite(ChargeHockeyGame game) {
        super();
        if (tail_region == null) tail_region = game.skin.getRegion("pixels/px_white");
        if (head_region == null) head_region = game.sprites.findRegion("vector_head");
        setRegion(tail_region);
        head_sprite = new Sprite();
        head_sprite.setRegion(head_region);
    }

    @Override
    public void draw(Batch batch) {
        super.draw(batch);
        head_sprite.draw(batch);
    }

    void prepare(float x, float y, float rotation, float length) {
        if (MathUtils.isZero(length, PHYSICS_EPSILON)) {
            setSize(0, 0);
            head_sprite.setSize(0, 0);
            return;
        }
        float tail_width = Math.max(length / (MAX_LENGTH) * 0.8f, MIN_TAIL_WIDTH);  // sprite width
        float head_width = tail_width * 2;
        length = Math.max(0, length - head_width);
        setBounds(x, y - tail_width / 2, length, tail_width);
        setOrigin(0, tail_width / 2);  // rotate around the center of the puck
        setRotation(rotation);

        head_sprite.setBounds(x + MathUtils.cosDeg(rotation) * length, y + MathUtils.sinDeg(rotation) * length - head_width / 2, head_width, head_width);
        head_sprite.setOrigin(0, head_width / 2);
        head_sprite.setRotation(rotation);
    }

    @Override
    public void setColor(Color tint) {
        super.setColor(tint);
        head_sprite.setColor(tint);
    }

    @Override
    public void setAlpha(float a) {
        super.setAlpha(a);
        head_sprite.setAlpha(a);
    }
}
