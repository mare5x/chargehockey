package com.mare5x.chargehockey.menus;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.mare5x.chargehockey.ChargeHockeyGame;

/** A horizontally scrollable Label.
 *  Usage:
 *   ScrollableLabel label;
 *   table.add(label.get()); */
public class ScrollableLabel extends Label {
    private final ScrollPane scroll_pane;

    public ScrollableLabel(ChargeHockeyGame game) {
        super("", game.skin, "borderless");
        scroll_pane = new ScrollPane(this, game.skin, "border");
        scroll_pane.setScrollingDisabled(false, true);  // only horizontal scrolling
    }

    public final ScrollPane get() { return scroll_pane; }

    @Override
    public void setText(CharSequence newText) {
        super.setText(newText);
        scroll_pane.validate();  // necessary
        scroll_pane.setScrollPercentX(1.0f);  // scroll right
    }
}
