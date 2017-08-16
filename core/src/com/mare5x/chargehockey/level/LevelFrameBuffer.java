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
import com.badlogic.gdx.utils.ObjectMap;
import com.mare5x.chargehockey.ChargeHockeyGame;
import com.mare5x.chargehockey.actors.PuckActor;
import com.mare5x.chargehockey.level.Grid.GRID_ITEM;


// Wrapper for a FrameBuffer
// TODO fix camera rounding errors
public class LevelFrameBuffer {
    private final FrameBuffer fbo;
    private final TextureRegion fbo_region;
    private final OrthographicCamera fbo_camera;

    private final ObjectMap<Grid.GRID_ITEM, Sprite> grid_sprites;

    private final Sprite puck_sprite;
    private float puck_alpha = 0.5f;
    private boolean draw_pucks = true;

    private final Sprite grid_line_sprite;
    private static boolean DRAW_GRID_LINES_SETTING = false;
    private boolean draw_grid_lines = false;  // ability to override the setting
    private static float grid_line_sprite_size = 1f / (1024f / ChargeHockeyGame.WORLD_WIDTH);  // 1 px
    private int grid_line_spacing = 1;  // determines after how many grid tiles a line is drawn

    private Level level;

    public LevelFrameBuffer(final ChargeHockeyGame game, final Level level) {
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
        puck_sprite.setSize(PuckActor.SIZE, PuckActor.SIZE);

        grid_line_sprite = game.skin.getSprite("pixels/px_purple");
        grid_line_sprite.setSize(grid_line_sprite_size, grid_line_sprite_size);

        // For pixel perfect rendering, the width and height of the FBO must be a multiple of the world width * sprite size.
        // Here each tile has a size of 16*16 px.
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, 1024, 1024, false);
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
            grid_line_sprite.setAlpha(0.5f);
            for (int col = 0; col < ChargeHockeyGame.WORLD_WIDTH; col += grid_line_spacing) {
                grid_line_sprite.setPosition(col, 0);
                grid_line_sprite.draw(batch);
            }
            // fix for the line on the edge, so it stays on screen
            grid_line_sprite.setPosition(ChargeHockeyGame.WORLD_WIDTH - grid_line_sprite_size, 0);
            grid_line_sprite.draw(batch);

            grid_line_sprite.setAlpha(1f);  // center vertical ilne
            grid_line_sprite.setPosition(ChargeHockeyGame.WORLD_WIDTH / 2, 0);
            grid_line_sprite.draw(batch);

            grid_line_sprite.setAlpha(0.8f);  // quarter vertical lines
            grid_line_sprite.setPosition(ChargeHockeyGame.WORLD_WIDTH / 4f, 0);
            grid_line_sprite.draw(batch);
            grid_line_sprite.setPosition(3 * ChargeHockeyGame.WORLD_WIDTH / 4f, 0);
            grid_line_sprite.draw(batch);

            // horizontal lines
            grid_line_sprite.setSize(ChargeHockeyGame.WORLD_WIDTH, grid_line_sprite_size);

            grid_line_sprite.setAlpha(1);  // center horizontal line
            grid_line_sprite.setPosition(0, ChargeHockeyGame.WORLD_HEIGHT / 2);
            grid_line_sprite.draw(batch);

            grid_line_sprite.setAlpha(0.8f);  // quarter horizontal lines
            grid_line_sprite.setPosition(0, ChargeHockeyGame.WORLD_HEIGHT / 4f);
            grid_line_sprite.draw(batch);
            grid_line_sprite.setPosition(0, 3 * ChargeHockeyGame.WORLD_HEIGHT / 4f);
            grid_line_sprite.draw(batch);

            grid_line_sprite.setAlpha(0.5f);
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
                    Sprite sprite = grid_sprites.get(item);
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

    /** THIS IS JUST A 'SETTING', TO APPLY IT CALL set_draw_grid_lines(...)!!! */
    public static void set_grid_lines_setting(boolean val) {
        DRAW_GRID_LINES_SETTING = val;
    }

    public static boolean get_grid_lines_setting() {
        return DRAW_GRID_LINES_SETTING;
    }
}
