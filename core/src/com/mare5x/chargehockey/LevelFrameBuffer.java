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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;

// Wrapper for a FrameBuffer
// TODO fix camera rounding errors
class LevelFrameBuffer {
    private static FrameBuffer fbo = null;
    private static TextureRegion fbo_region = null;
    private final OrthographicCamera fbo_camera;

    private final ObjectMap<GRID_ITEM, Sprite> grid_sprites;
    private final Sprite puck_sprite;

    private float puck_alpha = 0.5f;
    private boolean draw_pucks = true;

    private Level level;

    LevelFrameBuffer(final ChargeHockeyGame game, final Level level) {
        this.level = level;

        Sprite null_sprite = game.sprites.createSprite("grid/grid_null");
        null_sprite.setSize(1, 1);

        Sprite wall_sprite = game.sprites.createSprite("grid/grid_wall");
        wall_sprite.setSize(1, 1);

        Sprite goal_sprite = game.sprites.createSprite("grid/grid_goal");
        goal_sprite.setSize(1, 1);

        grid_sprites = new ObjectMap<GRID_ITEM, Sprite>(GRID_ITEM.values.length);
        grid_sprites.put(GRID_ITEM.NULL, null_sprite);
        grid_sprites.put(GRID_ITEM.WALL, wall_sprite);
        grid_sprites.put(GRID_ITEM.GOAL, goal_sprite);

        puck_sprite = game.sprites.createSprite("puck");
        puck_sprite.setSize(1, 1);

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

    /* Update the FBO with the level data. **/
    void update(final SpriteBatch batch) {
        if (level == null) return;

        fbo.begin();

        Gdx.gl20.glClearColor(0, 0, 0, 1);
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

        set_projection_matrix(batch);

        batch.begin();

        // Draw the grid
        for (int row = 0; row < ChargeHockeyGame.WORLD_HEIGHT; row++) {
            for (int col = 0; col < ChargeHockeyGame.WORLD_WIDTH; col++) {
                GRID_ITEM item = level.get_grid_item(row, col);
                if (item != GRID_ITEM.NULL) {
                    Sprite sprite = grid_sprites.get(item);
                    sprite.setPosition(col, row);
                    sprite.draw(batch);
                }
            }
        }

        // Draw the pucks
        if (draw_pucks) {
            puck_sprite.setAlpha(puck_alpha);
            for (Vector2 pos : level.get_puck_positions()) {
                puck_sprite.setPosition(pos.x, pos.y);
                puck_sprite.draw(batch);
            }
        }

        batch.end();

        fbo.end();
    }

    /** Draws the pucks without clearing the buffer. */
    void draw_pucks(final SpriteBatch batch) {
        if (level == null) return;

        fbo.begin();
        set_projection_matrix(batch);

        batch.begin();

        puck_sprite.setAlpha(puck_alpha);
        for (Vector2 pos : level.get_puck_positions()) {
            puck_sprite.setPosition(pos.x, pos.y);
            puck_sprite.draw(batch);
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

    TextureRegion get_texture_region() {
        return fbo_region;
    }

    void set_puck_alpha(int alpha) {
        puck_alpha = alpha;
    }

    void set_draw_pucks(boolean val) {
        draw_pucks = val;
    }
}
