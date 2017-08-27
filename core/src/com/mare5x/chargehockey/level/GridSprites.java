package com.mare5x.chargehockey.level;


import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.ObjectMap;
import com.mare5x.chargehockey.ChargeHockeyGame;
import com.mare5x.chargehockey.actors.PuckActor;

public class GridSprites {
    private boolean preview = false;  // whether to load preview sprites

    private final ObjectMap<Grid.GRID_ITEM, Sprite> grid_sprites;
    private final ObjectMap<Grid.GRID_ITEM, Sprite> preview_grid_sprites;
    private final Sprite puck_sprite;
    private final Sprite grid_line;

    public GridSprites(ChargeHockeyGame game) {
        Sprite null_sprite = game.sprites.createSprite("grid/grid_null");
        null_sprite.setSize(1, 1);

        Sprite wall_sprite = game.sprites.createSprite("grid/grid_wall");
        wall_sprite.setSize(1, 1);

        Sprite goal_sprite = game.sprites.createSprite("grid/grid_goal");
        goal_sprite.setSize(1, 1);

        grid_sprites = new ObjectMap<Grid.GRID_ITEM, Sprite>(Grid.GRID_ITEM.values.length);
        grid_sprites.put(Grid.GRID_ITEM.NULL, null_sprite);
        grid_sprites.put(Grid.GRID_ITEM.WALL, wall_sprite);
        grid_sprites.put(Grid.GRID_ITEM.GOAL, goal_sprite);

        // p_ = preview
        Sprite p_null_sprite = game.sprites.createSprite("grid/p_grid_null");
        p_null_sprite.setSize(1, 1);

        Sprite p_wall_sprite = game.sprites.createSprite("grid/p_grid_wall");
        p_wall_sprite.setSize(1, 1);

        Sprite p_goal_sprite = game.sprites.createSprite("grid/p_grid_goal");
        p_goal_sprite.setSize(1, 1);

        preview_grid_sprites = new ObjectMap<Grid.GRID_ITEM, Sprite>(Grid.GRID_ITEM.values.length);
        preview_grid_sprites.put(Grid.GRID_ITEM.NULL, p_null_sprite);
        preview_grid_sprites.put(Grid.GRID_ITEM.WALL, p_wall_sprite);
        preview_grid_sprites.put(Grid.GRID_ITEM.GOAL, p_goal_sprite);

        puck_sprite = game.sprites.createSprite("puck");
        puck_sprite.setSize(PuckActor.SIZE, PuckActor.SIZE);

        grid_line = new Sprite(game.skin.getRegion("pixels/px_purple"));
    }

    void set_preview(boolean value) {
        preview = value;
    }

    /** Returns the stored grid item sprite. If preview returns the preview version of the sprite. */
    public Sprite get(Grid.GRID_ITEM grid_item) {
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