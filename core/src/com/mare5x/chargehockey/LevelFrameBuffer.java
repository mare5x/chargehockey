package com.mare5x.chargehockey;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;

// Wrapper for a FrameBuffer
// TODO fix camera rounding errors
class LevelFrameBuffer {
    private static FrameBuffer fbo = null;
    private static TextureRegion fbo_region = null;
    private final OrthographicCamera fbo_camera;

    LevelFrameBuffer() {
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

    void render(SpriteBatch batch, float x, float y, float w, float h) {
        batch.draw(fbo_region, x, y, w, h);
    }

    void dispose() {
        fbo.dispose();  // this also disposes the bound texture
    }
}
