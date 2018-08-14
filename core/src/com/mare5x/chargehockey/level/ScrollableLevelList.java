package com.mare5x.chargehockey.level;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.mare5x.chargehockey.ChargeHockeyGame;

/* Usage:
    ScrollableLevelList list;
    table.add(list.get());
 */
class ScrollableLevelList extends LevelList {
    private final ScrollPane scroll_pane;

    ScrollableLevelList(ChargeHockeyGame game, LevelListCallback callbacks, Level.LEVEL_TYPE type) {
        super(game, callbacks, type);

        scroll_pane = new ScrollPane(this, game.skin);
        scroll_pane.setScrollingDisabled(true, false);
        scroll_pane.setVariableSizeKnobs(true);
    }

    final ScrollPane get() {
        return scroll_pane;
    }

    // TODO fix
    void scroll_to_selected() {
        Rectangle rect = get_selected_rect();

        // necessary to scroll to bottom
        scroll_pane.validate();
        scroll_pane.scrollTo(rect.x, rect.y, rect.width, rect.height, true, true);
    }
}
