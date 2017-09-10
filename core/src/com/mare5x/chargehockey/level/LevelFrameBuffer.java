package com.mare5x.chargehockey.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.mare5x.chargehockey.ChargeHockeyGame;
import com.mare5x.chargehockey.actors.ChargeActor;
import com.mare5x.chargehockey.actors.PuckActor;
import com.mare5x.chargehockey.level.Grid.GRID_ITEM;


// Wrapper for a FrameBuffer
// TODO fix camera rounding errors (PHYSICALLY IMPOSSIBLE)
public class LevelFrameBuffer {
    private static final int _FBO_SIZE = 1024;
    private static final int _PREVIEW_FBO_SIZE = 256;

    private static int FBO_SIZE;
    private static float WORLD_UNIT_TX;  // 1 world unit = world_unit_tx texels
    public static float ONE_TX; // 1 texel in world units

    public static float GRID_TILE_SIZE = 1 + ONE_TX;  // in world units

    private final ChargeHockeyGame game;

    private final FrameBuffer fbo;
    private final TextureRegion fbo_region;
    private final OrthographicCamera fbo_camera;

    private boolean draw_pucks = true;
    private final Sprite puck_sprite;
    private float puck_alpha = 0.5f;

    private Level level;

    public LevelFrameBuffer(final ChargeHockeyGame game, final Level level) {
        this(game, level, _FBO_SIZE);
    }

    LevelFrameBuffer(final ChargeHockeyGame game, final Level level, boolean preview) {
        this(game, level, preview ? _PREVIEW_FBO_SIZE : _FBO_SIZE);
    }

    private LevelFrameBuffer(final ChargeHockeyGame game, final Level level, int size) {
        this.game = game;
        this.level = level;

        FBO_SIZE = size;
        WORLD_UNIT_TX = FBO_SIZE / ChargeHockeyGame.WORLD_WIDTH;  // 1 world unit = world_unit_tx texels
        ONE_TX = 1 / WORLD_UNIT_TX;  // 1 texel
        GRID_TILE_SIZE = 1 + ONE_TX;

        game.grid_sprites.set_grid_tile_size(GRID_TILE_SIZE);
        game.grid_sprites.set_preview(is_preview(FBO_SIZE));

        puck_sprite = game.grid_sprites.get_puck();

        // For pixel perfect rendering, the width and height of the FBO must be a multiple of the world width * sprite size.
        // Here each tile has a size of 16*16 px.
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, FBO_SIZE, FBO_SIZE, false);
        fbo.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        fbo_region = new TextureRegion(fbo.getColorBufferTexture());
        fbo_region.flip(false, true);  // FBO uses lower left, TextureRegion uses upper-left

        fbo_camera = new OrthographicCamera(ChargeHockeyGame.WORLD_WIDTH, ChargeHockeyGame.WORLD_HEIGHT);
        fbo_camera.position.set(fbo_camera.viewportWidth / 2, fbo_camera.viewportHeight / 2, 0);  // center camera
        fbo_camera.update();

        clear();
    }

    // NOTE: Remember to call end().
    public void begin() {
        fbo.begin();
    }

    public void end() {
        fbo.end();
    }

    public void set_projection_matrix(SpriteBatch batch) {
        batch.setProjectionMatrix(fbo_camera.combined);
    }

    /* Update the FBO with the level data and CLEARS the FBO! **/
    public void update(final SpriteBatch batch) {
        if (level == null) return;

        fbo.begin();

        // the fbo background is transparent so that it doesn't cover the background grid lines
        Gdx.gl20.glClearColor(0, 0, 0, 0);
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

        set_projection_matrix(batch);

        batch.begin();

        draw_grid(batch);

        // Draw the pucks (optional)
        if (draw_pucks) {
            puck_sprite.setAlpha(puck_alpha);
            for (ChargeActor.ChargeState state : level.get_puck_states()) {
                puck_sprite.setPosition(state.x - PuckActor.RADIUS, state.y - PuckActor.RADIUS);
                puck_sprite.draw(batch);
                if (state.partner != null) {
                    puck_sprite.setPosition(state.partner.x - PuckActor.RADIUS, state.partner.y - PuckActor.RADIUS);
                    puck_sprite.draw(batch);
                }
            }
        }

        batch.end();

        fbo.end();

        Gdx.graphics.requestRendering();
    }

    private void draw_grid(final SpriteBatch batch) {
        // Draw the grid
        game.grid_sprites.set_grid_tile_size(GRID_TILE_SIZE);
        for (int row = 0; row < ChargeHockeyGame.WORLD_HEIGHT - 1; row++) {
            for (int col = 0; col < ChargeHockeyGame.WORLD_WIDTH - 1; col++) {
                GRID_ITEM item = level.get_grid_item(row, col);
                game.grid_sprites.draw_tile(batch, item, row, col);
            }
        }
        // specially draw the top and right border tiles, otherwise they get cut off by ONE_TX
        // note: this makes the border sprites squished by ONE_TX
        // right border
        game.grid_sprites.set_grid_tile_size(GRID_TILE_SIZE - ONE_TX, GRID_TILE_SIZE);
        for (int row = 0; row < ChargeHockeyGame.WORLD_HEIGHT - 1; row++) {
            int col = ChargeHockeyGame.WORLD_WIDTH - 1;
            GRID_ITEM item = level.get_grid_item(row, col);
            game.grid_sprites.draw_tile(batch, item, row, col);
        }
        // top border
        game.grid_sprites.set_grid_tile_size(GRID_TILE_SIZE, GRID_TILE_SIZE - ONE_TX);
        for (int col = 0; col < ChargeHockeyGame.WORLD_WIDTH - 1; col++) {
            int row = ChargeHockeyGame.WORLD_HEIGHT - 1;
            GRID_ITEM item = level.get_grid_item(row, col);
            game.grid_sprites.draw_tile(batch, item, row, col);
        }
        // top right
        GRID_ITEM item = level.get_grid_item(ChargeHockeyGame.WORLD_WIDTH - 1, ChargeHockeyGame.WORLD_HEIGHT - 1);
        if (item != GRID_ITEM.NULL) {
            Sprite sprite = game.grid_sprites.get(item);
            sprite.setSize(GRID_TILE_SIZE - ONE_TX, GRID_TILE_SIZE - ONE_TX);
            sprite.setPosition(ChargeHockeyGame.WORLD_WIDTH - 1, ChargeHockeyGame.WORLD_HEIGHT - 1);
            sprite.draw(batch);
        }
    }

    /** Clears the FBO to black. */
    public void clear() {
        fbo.begin();

        Gdx.gl20.glClearColor(0, 0, 0, 1);
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

        fbo.end();
    }

    /** Draws the pucks without clearing the buffer. */
    public void draw_pucks(final SpriteBatch batch) {
        if (level == null) return;

        fbo.begin();
        set_projection_matrix(batch);

        batch.begin();

        puck_sprite.setAlpha(puck_alpha);
        for (ChargeActor.ChargeState state : level.get_puck_states()) {
            puck_sprite.setPosition(state.x - PuckActor.RADIUS, state.y - PuckActor.RADIUS);
            puck_sprite.draw(batch);
            if (state.partner != null) {
                puck_sprite.setPosition(state.partner.x - PuckActor.RADIUS, state.partner.y - PuckActor.RADIUS);
                puck_sprite.draw(batch);
            }
        }

        batch.end();

        fbo.end();
    }

    /** NOTE: call update() after changing the level. */
    void set_level(final Level new_level) {
        level = new_level;
    }

    public void render(Batch batch, float x, float y, float w, float h) {
        game.batch.setColor(Color.WHITE);  // this fixes a weird alpha bug
        batch.draw(fbo_region, x, y, w, h);
    }

    public void dispose() {
        fbo.dispose();  // this also disposes the bound texture
    }

    TextureRegion get_texture_region() {
        return fbo_region;
    }

    void set_puck_alpha(int alpha) {
        puck_alpha = alpha;
    }

    public void set_draw_pucks(boolean val) {
        draw_pucks = val;
    }

    private static boolean is_preview(int size) {
        return size == _PREVIEW_FBO_SIZE;
    }
}
