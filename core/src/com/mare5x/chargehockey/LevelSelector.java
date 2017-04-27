package com.mare5x.chargehockey;

import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;

public class LevelSelector {
    private final ChargeHockeyGame game;

    private final List<String> list;
    private final ScrollPane scroll_pane;

    public LevelSelector(ChargeHockeyGame game) {
        this.game = game;

        list = new List<String>(game.skin);
        init_list();

        scroll_pane = new ScrollPane(list, game.skin);
        scroll_pane.setVariableSizeKnobs(true);
    }

    // TODO
    private void init_list() {
        list.setItems("item1", "item2", "asdfasd");
    }

    public String get_selected_name() {
        int selected_idx = list.getSelectedIndex();
        if (selected_idx != -1)
            return list.getItems().get(selected_idx);
        return "";
    }

    public ScrollPane get_display() {
        return scroll_pane;
    }

    public void remove_selected_level() {
        int selected_idx = list.getSelectedIndex();
        if (selected_idx != -1) {
            // TODO remove saved level file
            list.getItems().removeIndex(selected_idx);
        }
    }

    public void add_level(String level_name) {
        list.getItems().add(level_name);
        list.invalidateHierarchy();  // reset the layout (add scroll bars to scroll pane)
    }
}
