package com.mare5x.chargehockey.menus;

import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.mare5x.chargehockey.ChargeHockeyGame;


public abstract class ScrollableMenuScreen extends BaseMenuScreen {
    private final ScrollPane scroll_pane;

    protected ScrollableMenuScreen(ChargeHockeyGame game) {
        super(game);

        // undo parent settings because of scrollpane
        table.setFillParent(false);
        table.remove();

        scroll_pane = new ScrollPane(table, game.skin);
        scroll_pane.setFillParent(true);
        scroll_pane.setScrollingDisabled(true, false);  // disable horizontal scrolling

        stage.addActor(scroll_pane);
    }

    abstract protected void back_key_pressed();

    @Override
    protected void fade_in() {
        super.fade_in();
        scroll_pane.setScrollPercentY(0);  // automatically scroll to the top
    }
}
