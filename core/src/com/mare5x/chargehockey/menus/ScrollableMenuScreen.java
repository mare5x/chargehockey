package com.mare5x.chargehockey.menus;

import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.mare5x.chargehockey.ChargeHockeyGame;


public abstract class ScrollableMenuScreen extends BaseMenuScreen {

    protected ScrollableMenuScreen(ChargeHockeyGame game) {
        super(game);

        // undo parent settings because of scrollpane
        table.setFillParent(false);
        table.remove();

        ScrollPane scroll_pane = new ScrollPane(table, game.skin);
        scroll_pane.setFillParent(true);
        scroll_pane.setScrollingDisabled(true, false);  // disable horizontal scrolling

        stage.addActor(scroll_pane);
    }

    abstract protected void back_key_pressed();

    abstract public void hide();
}
