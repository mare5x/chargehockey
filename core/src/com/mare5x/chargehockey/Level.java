package com.mare5x.chargehockey;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.ObjectMap;

import java.util.Arrays;

class Level {
    private final ChargeHockeyGame game;

    private final String name;

    private final ObjectMap<GRID_ITEM, Sprite> grid_sprites;

    private final Grid grid;

    Level(final String level_name, final ChargeHockeyGame game) {
        this.name = level_name;
        this.game = game;

        this.grid = new Grid();

        Sprite null_sprite = new Sprite(game.sprites.findRegion("grid_null"));
        null_sprite.setSize(1, 1);

        Sprite wall_sprite = new Sprite(game.sprites.findRegion("grid_wall"));
        wall_sprite.setSize(1, 1);

        Sprite goal_sprite = new Sprite(game.sprites.findRegion("grid_goal"));
        goal_sprite.setSize(1, 1);

        Sprite puck_sprite = new Sprite(game.sprites.findRegion("puck"));
        puck_sprite.setSize(1, 1);

        grid_sprites = new ObjectMap<GRID_ITEM, Sprite>(GRID_ITEM.values.length);
        grid_sprites.put(GRID_ITEM.NULL, null_sprite);
        grid_sprites.put(GRID_ITEM.WALL, wall_sprite);
        grid_sprites.put(GRID_ITEM.GOAL, goal_sprite);
        grid_sprites.put(GRID_ITEM.PUCK, puck_sprite);
    }

    void set_item(int row, int col, GRID_ITEM item) {
        grid.set_item(row, col, item);
    }

    final Sprite get_item_sprite(GRID_ITEM item) {
        return grid_sprites.get(item);
    }

    final GRID_ITEM get_grid_item(int row, int col) {
        return grid.get_item(row, col);
    }

    static byte[] get_empty_level_data() {
        byte[] level_data = new byte[ChargeHockeyGame.WORLD_WIDTH * ChargeHockeyGame.WORLD_HEIGHT];
        Arrays.fill(level_data, (byte) GRID_ITEM.NULL.ordinal());

        return level_data;
    }

    byte[] get_level_data() {
        return grid.get_byte_data();
    }

    void save() {
        FileHandle file = LevelSelector.get_level_fhandle(name);

        file.writeBytes(get_level_data(), false);
    }

    void load_from_data(byte[] level_data) {
        grid.from_byte_data(level_data);
    }
}
