package com.mare5x.chargehockey.level;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteCache;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.mare5x.chargehockey.ChargeHockeyGame;


/** Draws the grid lines and the background. This used to be a part of the LevelFrameBuffer but I had to seperate the two
 * since if I wanted to update the grid lines on the fbo, the fbo had to be cleared (including the puck
 * trace path).
 */
public class GridCache {
    private final SpriteCache cache = new SpriteCache();

    private int background_id = -1;
    private int grid_lines_id = -1;

    private boolean draw_grid = false;

    private float grid_line_sprite_size;
    private final Sprite grid_line_sprite;
    private int grid_line_spacing = 1;  // determines after how many grid tiles a line is drawn
    private float grid_line_alpha = 1;

    public GridCache(ChargeHockeyGame game) {
        grid_line_sprite_size = LevelFrameBuffer.ONE_TX;

        grid_line_sprite = game.grid_sprites.get_grid_line(grid_line_sprite_size);

        init_background(game.skin.getRegion("pixels/px_black"));
    }

    private void init_background(TextureRegion bg) {
        if (background_id != -1)
            cache.beginCache(background_id);
        else
            cache.beginCache();

        cache.add(bg, 0, 0, ChargeHockeyGame.WORLD_WIDTH, ChargeHockeyGame.WORLD_HEIGHT);

        background_id = cache.endCache();
    }

    private void init_grid_lines() {
        if (grid_lines_id != -1)
            cache.beginCache(grid_lines_id);
        else
            cache.beginCache();

        // Draw the grid's lines (optional)
        // vertical lines
        grid_line_sprite.setSize(grid_line_sprite_size, ChargeHockeyGame.WORLD_HEIGHT);
        grid_line_sprite.setAlpha(0.5f * grid_line_alpha);
        for (int col = 0; col < ChargeHockeyGame.WORLD_WIDTH; col += grid_line_spacing) {
            grid_line_sprite.setPosition(col, 0);
            cache.add(grid_line_sprite);
        }
        // fix for the line on the edge, so it stays on screen
        grid_line_sprite.setPosition(ChargeHockeyGame.WORLD_WIDTH - grid_line_sprite_size, 0);
        cache.add(grid_line_sprite);

        grid_line_sprite.setAlpha(1f * grid_line_alpha);  // center vertical ilne
        grid_line_sprite.setPosition(ChargeHockeyGame.WORLD_WIDTH / 2, 0);
        cache.add(grid_line_sprite);

        grid_line_sprite.setAlpha(0.8f * grid_line_alpha);  // quarter vertical lines
        grid_line_sprite.setPosition(ChargeHockeyGame.WORLD_WIDTH / 4f, 0);
        cache.add(grid_line_sprite);
        grid_line_sprite.setPosition(3 * ChargeHockeyGame.WORLD_WIDTH / 4f, 0);
        cache.add(grid_line_sprite);

        // horizontal lines
        grid_line_sprite.setSize(ChargeHockeyGame.WORLD_WIDTH, grid_line_sprite_size);

        grid_line_sprite.setAlpha(1 * grid_line_alpha);  // center horizontal line
        grid_line_sprite.setPosition(0, ChargeHockeyGame.WORLD_HEIGHT / 2);
        cache.add(grid_line_sprite);

        grid_line_sprite.setAlpha(0.8f * grid_line_alpha);  // quarter horizontal lines
        grid_line_sprite.setPosition(0, ChargeHockeyGame.WORLD_HEIGHT / 4f);
        cache.add(grid_line_sprite);
        grid_line_sprite.setPosition(0, 3 * ChargeHockeyGame.WORLD_HEIGHT / 4f);
        cache.add(grid_line_sprite);

        grid_line_sprite.setAlpha(0.5f * grid_line_alpha);
        for (int row = 0; row < ChargeHockeyGame.WORLD_HEIGHT; row += grid_line_spacing) {
            grid_line_sprite.setPosition(0, row);
            cache.add(grid_line_sprite);
        }
        grid_line_sprite.setPosition(0, ChargeHockeyGame.WORLD_HEIGHT - grid_line_sprite_size);
        cache.add(grid_line_sprite);

        grid_lines_id = cache.endCache();
    }

    public void update(float zoom) {
        update(zoom, get_grid_line_spacing(zoom));
    }

    public void update(float zoom, int spacing) {
        float prev_size = grid_line_sprite_size;
        int prev_spacing = grid_line_spacing;

        set_grid_line_spacing(spacing);
        update_grid_line_size(zoom);

        // update if necessary
        if (prev_size != grid_line_sprite_size || prev_spacing != grid_line_spacing)
            init_grid_lines();
    }

    public void render() {
        cache.begin();

        cache.draw(background_id);

        if (draw_grid) {
            // otherwise alpha won't be shown
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            cache.draw(grid_lines_id);

            Gdx.gl.glDisable(GL20.GL_BLEND);
        }

        cache.end();
    }

    private void update_grid_line_size(float zoom) {
        // make sure grid_line_sprite_size is closer to 1 pixel NOT 1 texel, otherwise grid lines might
        // not even get drawn due to rounding 'errors'

        grid_line_sprite_size = Math.max(LevelFrameBuffer.ONE_TX,
                LevelFrameBuffer.FBO_SIZE / Math.min(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()) * LevelFrameBuffer.ONE_TX * 1.5f * zoom);
    }

    private void set_grid_line_spacing(int spacing) {
        grid_line_spacing = MathUtils.clamp(spacing, 1, ChargeHockeyGame.WORLD_WIDTH / 2);
    }

    public int get_grid_line_spacing() {
        return grid_line_spacing;
    }

    public void set_grid_line_alpha(float alpha) {
        if (grid_line_alpha != alpha) {
            grid_line_alpha = alpha;
            init_grid_lines();
        }
    }

    public void set_show_grid_lines(boolean show) {
        draw_grid = show;
    }

    public boolean get_show_grid_lines() {
        return draw_grid;
    }

    public static int get_grid_line_spacing(float zoom) {
        if (zoom <= 0.6f) return 1;
        else if (zoom <= 1f) return 2;
        else if (zoom <= 1.4f) return 4;
        else return 8;
    }

    public void set_projection_matrix(Matrix4 matrix) {
        cache.setProjectionMatrix(matrix);
    }

    public void dispose() {
        cache.dispose();
    }
}
