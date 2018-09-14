package com.mare5x.chargehockey.level;


import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ObjectMap;
import com.mare5x.chargehockey.ChargeHockeyGame;
import com.mare5x.chargehockey.actors.PuckActor;
import com.mare5x.chargehockey.level.Grid.GRID_ITEM;

public class GridSprites {
    private boolean preview = false;  // whether to load preview sprites

    private final ObjectMap<GRID_ITEM, Sprite> grid_sprites;
    private final ObjectMap<GRID_ITEM, Sprite> preview_grid_sprites;

    private final Sprite puck_sprite;
    private final Sprite grid_line;

    public GridSprites(ChargeHockeyGame game) {
        Sprite null_sprite = game.create_sprite("grid_null");
        Sprite wall_sprite = game.create_sprite("grid_wall");
        Sprite goal_sprite = game.create_sprite("grid_goal");
        Sprite bouncer_sprite = game.create_sprite("grid_bouncer");

        grid_sprites = new ObjectMap<GRID_ITEM, Sprite>(GRID_ITEM.values.length);
        grid_sprites.put(GRID_ITEM.NULL, null_sprite);
        grid_sprites.put(GRID_ITEM.WALL, wall_sprite);
        grid_sprites.put(GRID_ITEM.GOAL, goal_sprite);
        grid_sprites.put(GRID_ITEM.BOUNCER, bouncer_sprite);

        // p_ = preview
        Sprite p_null_sprite = game.create_sprite("p_grid_null");
        Sprite p_wall_sprite = game.create_sprite("p_grid_wall");
        Sprite p_goal_sprite = game.create_sprite("p_grid_goal");
        Sprite p_bouncer_sprite = game.create_sprite("p_grid_bouncer");

        preview_grid_sprites = new ObjectMap<GRID_ITEM, Sprite>(GRID_ITEM.values.length);
        preview_grid_sprites.put(GRID_ITEM.NULL, p_null_sprite);
        preview_grid_sprites.put(GRID_ITEM.WALL, p_wall_sprite);
        preview_grid_sprites.put(GRID_ITEM.GOAL, p_goal_sprite);
        preview_grid_sprites.put(GRID_ITEM.BOUNCER, p_bouncer_sprite);

        set_grid_tile_size(1);

        puck_sprite = game.create_sprite("sprite_puck");
        puck_sprite.setSize(PuckActor.SIZE, PuckActor.SIZE);

        grid_line = game.create_sprite("px_darkgrey");
    }

    void set_preview(boolean value) {
        preview = value;
    }

    void set_grid_tile_size(float size) {
        set_grid_tile_size(size, size);
    }

    void set_grid_tile_size(float width, float height) {
        for (ObjectMap.Entry<GRID_ITEM, Sprite> entry : grid_sprites.entries()) {
            entry.value.setSize(width, height);
        }

        for (ObjectMap.Entry<GRID_ITEM, Sprite> entry : preview_grid_sprites.entries()) {
            entry.value.setSize(width, height);
        }
    }
    
    void draw_tile(SpriteBatch batch, GRID_ITEM item, float row, float col) {
        if (item != GRID_ITEM.NULL) {
            Sprite sprite = get(item);
            sprite.setPosition(col, row);
            sprite.draw(batch);
        }
    }

    /** Returns the stored grid item sprite. If preview returns the preview version of the sprite. */
    public Sprite get(GRID_ITEM grid_item) {
        if (preview)
            return preview_grid_sprites.get(grid_item);
        return grid_sprites.get(grid_item);
    }

    Sprite get_puck() {
        return puck_sprite;
    }

    Sprite get_grid_line(float size) {
        grid_line.setSize(size, size);
        return grid_line;
    }
}
