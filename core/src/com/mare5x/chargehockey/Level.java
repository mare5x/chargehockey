package com.mare5x.chargehockey;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.ObjectMap;

public class Level {
    private final ChargeHockeyGame game;

    private final String name;

    private final ObjectMap<GRID_ITEM, Sprite> grid_sprites;

    private final Grid grid;

    public Level(final String level_name, final ChargeHockeyGame game) {
        this.name = level_name;
        this.game = game;

        this.grid = new Grid();

        Sprite null_sprite = new Sprite(game.sprites.findRegion("grid_null"));
        null_sprite.setSize(1, 1);

        Sprite wall_sprite = new Sprite(game.sprites.findRegion("grid_wall"));
        wall_sprite.setSize(1, 1);

        Sprite goal_sprite = new Sprite(game.sprites.findRegion("grid_goal"));
        goal_sprite.setSize(1, 1);

        grid_sprites = new ObjectMap<GRID_ITEM, Sprite>(3);
        grid_sprites.put(GRID_ITEM.NULL, null_sprite);
        grid_sprites.put(GRID_ITEM.WALL, wall_sprite);
        grid_sprites.put(GRID_ITEM.GOAL, goal_sprite);
    }

    public void set_item(int row, int col, GRID_ITEM item) {
        grid.set_item(row, col, item);
    }

    public final Sprite get_item_sprite(GRID_ITEM item) {
        return grid_sprites.get(item);
    }

    public final GRID_ITEM get_grid_item(int row, int col) {
        return grid.get_item(row, col);
    }
}
