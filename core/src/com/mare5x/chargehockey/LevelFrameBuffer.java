package com.mare5x.chargehockey;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;

// Wrapper for a FrameBuffer
// TODO fix camera rounding errors
class LevelFrameBuffer {
    private static FrameBuffer fbo = null;
    private static TextureRegion fbo_region = null;
    private final OrthographicCamera fbo_camera;

    private Sprite sprite;

    private Level level;

    LevelFrameBuffer(final Level level) {
        this.level = level;

        // For pixel perfect rendering, the width and height of the FBO must be a multiple of the world width * sprite size.
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, 1024, 1024, false);
        fbo.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        fbo_region = new TextureRegion(fbo.getColorBufferTexture());
        fbo_region.flip(false, true);  // FBO uses lower left, TextureRegion uses upper-left

        fbo_camera = new OrthographicCamera(ChargeHockeyGame.WORLD_WIDTH, ChargeHockeyGame.WORLD_HEIGHT);
        fbo_camera.position.set(ChargeHockeyGame.WORLD_WIDTH / 2, ChargeHockeyGame.WORLD_HEIGHT / 2, 0);  // center camera
        fbo_camera.update();
    }

    // NOTE: Remember to call end().
    void begin() {
        fbo.begin();
    }

    void end() {
        fbo.end();
    }

    void set_projection_matrix(SpriteBatch batch) {
        batch.setProjectionMatrix(fbo_camera.combined);
    }

    /*  Update the FBO with the level data.
    **/
    void update(final SpriteBatch batch) {
        if (level == null) return;

        fbo.begin();

        Gdx.gl20.glClearColor(0, 0, 0, 1);
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

        set_projection_matrix(batch);

        batch.begin();
        for (int row = 0; row < ChargeHockeyGame.WORLD_HEIGHT; row++) {
            for (int col = 0; col < ChargeHockeyGame.WORLD_WIDTH; col++) {
                GRID_ITEM item = level.get_grid_item(row, col);
                if (item != GRID_ITEM.NULL) {
                    sprite = level.get_item_sprite(item);
                    sprite.setPosition(col, row);
                    sprite.draw(batch);
                }
            }
        }
        batch.end();

        fbo.end();
    }

    /** NOTE: call update() after changing the level. */
    void set_level(final Level new_level) {
        level = new_level;
    }

    void render(Batch batch, float x, float y, float w, float h) {
        batch.draw(fbo_region, x, y, w, h);
    }

    void dispose() {
        fbo.dispose();  // this also disposes the bound texture
    }
}
