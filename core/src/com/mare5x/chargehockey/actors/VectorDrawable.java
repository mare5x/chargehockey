package com.mare5x.chargehockey.actors;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.mare5x.chargehockey.ChargeHockeyGame;

public class VectorDrawable extends TextureRegionDrawable {
    private static TextureRegion tail_region;
    private static TextureRegion head_region;

    private boolean flipped;

    public VectorDrawable(ChargeHockeyGame game, boolean flipped) {
        super();

        this.flipped = flipped;

        if (tail_region == null) tail_region = game.skin.getRegion("sprite_vector_tail");
        if (head_region == null) head_region = game.skin.getRegion("sprite_vector_head");

        setRegion(tail_region);
    }

    @Override
    public void draw(Batch batch, float x, float y, float width, float height) {
        float head_width = height;
        float tail_width = head_width / 2;
        float len = width - head_width;
        batch.draw(tail_region, (flipped ? x + head_width : x), y + (head_width / 2 - tail_width / 2), len, tail_width);
        batch.draw(head_region, (flipped ? x : x + len), y, head_width / 2, head_width / 2, head_width, head_width, 1, 1, (flipped ? 180 : 0));
    }
}
