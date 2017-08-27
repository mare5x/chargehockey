package com.mare5x.chargehockey.level;

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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mare5x.chargehockey.ChargeHockeyGame;
import com.mare5x.chargehockey.actors.PuckActor;
import com.mare5x.chargehockey.level.Grid.GRID_ITEM;


// Wrapper for a FrameBuffer
// TODO fix camera rounding errors (PHYSICALLY IMPOSSIBLE)
public class LevelFrameBuffer {
    private static final int _FBO_SIZE = 1024;
    private static final int _PREVIEW_FBO_SIZE = 256;

    private static int FBO_SIZE;
    public static float WORLD_UNIT_TX;  //  = FBO_SIZE / ChargeHockeyGame.WORLD_WIDTH;  // 1 world unit = world_unit_tx texels
    private static float ONE_TX; // = 1 / WORLD_UNIT_TX;  // 1 texel

    private final ChargeHockeyGame game;

    private final FrameBuffer fbo;
    private final TextureRegion fbo_region;
    private final OrthographicCamera fbo_camera;

    private boolean draw_pucks = true;
    private final Sprite puck_sprite;
    private float puck_alpha = 0.5f;

    private boolean draw_grid_lines = false;
    private static float grid_line_sprite_size;  // 1 tx
    private final Sprite grid_line_sprite;
    private int grid_line_spacing = 1;  // determines after how many grid tiles a line is drawn
    private float grid_line_alpha = 1;

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

        grid_line_sprite_size = ONE_TX;  // 1 tx

        game.grid_sprites.set_preview(is_preview(size));

        puck_sprite = game.grid_sprites.get_puck();

        update_grid_line_size(1);
        grid_line_sprite = game.grid_sprites.get_grid_line(grid_line_sprite_size);
        grid_line_sprite.setAlpha(grid_line_alpha);

        // For pixel perfect rendering, the width and height of the FBO must be a multiple of the world width * sprite size.
        // Here each tile has a size of 16*16 px.
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, FBO_SIZE, FBO_SIZE, false);
        fbo.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        fbo_region = new TextureRegion(fbo.getColorBufferTexture());
        fbo_region.flip(false, true);  // FBO uses lower left, TextureRegion uses upper-left

        fbo_camera = new OrthographicCamera(ChargeHockeyGame.WORLD_WIDTH, ChargeHockeyGame.WORLD_HEIGHT);
        fbo_camera.position.set(ChargeHockeyGame.WORLD_WIDTH / 2, ChargeHockeyGame.WORLD_HEIGHT / 2, 0);  // center camera
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

    /* Update the FBO with the level data. **/
    public void update(final SpriteBatch batch) {
        if (level == null) return;

        fbo.begin();

        Gdx.gl20.glClearColor(0, 0, 0, 1);
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

        set_projection_matrix(batch);

        batch.begin();

        // Draw the grid's lines (optional)
        if (draw_grid_lines) {
            // vertical lines
            grid_line_sprite.setSize(grid_line_sprite_size, ChargeHockeyGame.WORLD_HEIGHT);
            grid_line_sprite.setAlpha(0.5f * grid_line_alpha);
            for (int col = 0; col < ChargeHockeyGame.WORLD_WIDTH; col += grid_line_spacing) {
                grid_line_sprite.setPosition(col, 0);
                grid_line_sprite.draw(batch);
            }
            // fix for the line on the edge, so it stays on screen
            grid_line_sprite.setPosition(ChargeHockeyGame.WORLD_WIDTH - grid_line_sprite_size, 0);
            grid_line_sprite.draw(batch);

            grid_line_sprite.setAlpha(1f * grid_line_alpha);  // center vertical ilne
            grid_line_sprite.setPosition(ChargeHockeyGame.WORLD_WIDTH / 2, 0);
            grid_line_sprite.draw(batch);

            grid_line_sprite.setAlpha(0.8f * grid_line_alpha);  // quarter vertical lines
            grid_line_sprite.setPosition(ChargeHockeyGame.WORLD_WIDTH / 4f, 0);
            grid_line_sprite.draw(batch);
            grid_line_sprite.setPosition(3 * ChargeHockeyGame.WORLD_WIDTH / 4f, 0);
            grid_line_sprite.draw(batch);

            // horizontal lines
            grid_line_sprite.setSize(ChargeHockeyGame.WORLD_WIDTH, grid_line_sprite_size);

            grid_line_sprite.setAlpha(1 * grid_line_alpha);  // center horizontal line
            grid_line_sprite.setPosition(0, ChargeHockeyGame.WORLD_HEIGHT / 2);
            grid_line_sprite.draw(batch);

            grid_line_sprite.setAlpha(0.8f * grid_line_alpha);  // quarter horizontal lines
            grid_line_sprite.setPosition(0, ChargeHockeyGame.WORLD_HEIGHT / 4f);
            grid_line_sprite.draw(batch);
            grid_line_sprite.setPosition(0, 3 * ChargeHockeyGame.WORLD_HEIGHT / 4f);
            grid_line_sprite.draw(batch);

            grid_line_sprite.setAlpha(0.5f * grid_line_alpha);
            for (int row = 0; row < ChargeHockeyGame.WORLD_HEIGHT; row += grid_line_spacing) {
                grid_line_sprite.setPosition(0, row);
                grid_line_sprite.draw(batch);
            }
            grid_line_sprite.setPosition(0, ChargeHockeyGame.WORLD_HEIGHT - grid_line_sprite_size);
            grid_line_sprite.draw(batch);
        }

        // Draw the grid
        for (int row = 0; row < ChargeHockeyGame.WORLD_HEIGHT; row++) {
            for (int col = 0; col < ChargeHockeyGame.WORLD_WIDTH; col++) {
                GRID_ITEM item = level.get_grid_item(row, col);
                if (item != GRID_ITEM.NULL) {
                    Sprite sprite = game.grid_sprites.get(item);
                    sprite.setPosition(col, row);
                    sprite.draw(batch);
                }
            }
        }

        // Draw the pucks (optional)
        if (draw_pucks) {
            puck_sprite.setAlpha(puck_alpha);
            for (Vector2 pos : level.get_puck_positions()) {
                puck_sprite.setPosition(pos.x - PuckActor.RADIUS, pos.y - PuckActor.RADIUS);
                puck_sprite.draw(batch);
            }
        }

        batch.end();

        fbo.end();

        Gdx.graphics.requestRendering();
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
        for (Vector2 pos : level.get_puck_positions()) {
            puck_sprite.setPosition(pos.x - PuckActor.RADIUS, pos.y - PuckActor.RADIUS);
            puck_sprite.draw(batch);
        }

        batch.end();

        fbo.end();
    }

    /** NOTE: call update() after changing the level. */
    void set_level(final Level new_level) {
        level = new_level;
    }

    public void render(Batch batch, float x, float y, float w, float h) {
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

    public void update_grid_line_size(float zoom) {
        // make sure grid_line_sprite_size is 1 pixel NOT 1 texel, otherwise grid lines might
        // not even get drawn due to rounding 'errors'

        grid_line_sprite_size = Math.max(ONE_TX, FBO_SIZE / Math.min(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()) * ONE_TX * 1.5f * zoom);
    }

    public void set_grid_line_spacing(int spacing) {
        grid_line_spacing = MathUtils.clamp(spacing, 1, ChargeHockeyGame.WORLD_WIDTH / 2);
    }

    public int get_grid_line_spacing() {
        return grid_line_spacing;
    }

    public void set_draw_grid_lines(boolean val) {
        draw_grid_lines = val;
    }

    public boolean get_draw_grid_lines() {
        return draw_grid_lines;
    }

    public void set_grid_line_alpha(float alpha) {
        grid_line_alpha = alpha;
    }

    private static boolean is_preview(int size) {
        return size == _PREVIEW_FBO_SIZE;
    }
}
