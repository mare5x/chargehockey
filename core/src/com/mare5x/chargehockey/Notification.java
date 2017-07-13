package com.mare5x.chargehockey;


import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

abstract class Notification extends Actor {
    final Stage stage;
    private final Sprite border, background;

    // NOTE: stage must use screen coordinates.
    Notification(ChargeHockeyGame game, Stage stage) {
        this.stage = stage;

        // the default position and size is at the top of the screen
        float width = stage.getWidth() * 0.8f;
        float height = stage.getHeight() * 0.25f;
        setBounds(stage.getWidth() / 2 - width / 2, stage.getHeight() * 0.95f - height, width, height);

        border = game.skin.getSprite("pixels/px_green");
        border.setAlpha(0.8f);
        background = game.skin.getSprite("pixels/px_black");
        layout_background();
    }

    @Override
    public void setSize(float width, float height) {
        super.setSize(width, height);

        setPosition(stage.getWidth() / 2 - width / 2, stage.getHeight() * 0.95f - height);
        layout_background();
    }

    private void layout_background() {
        float w = getWidth(), h = getHeight();
        border.setBounds(getX(), getY(), w, h);
        background.setBounds(getX() + 2, getY() + 2, w - 4, h - 4);
    }

    /** Returns a default fade in/out action. */
    Action get_action() {
        return Actions.sequence(
                Actions.alpha(0.4f),
                Actions.show(),
                Actions.fadeIn(0.4f),
                Actions.delay(1.5f),
                Actions.fadeOut(0.6f),
                Actions.hide(),
                Actions.removeActor()
        );
    }

    @Override
    /* The default implementation draws the notification border. Override and call super. */
    public void draw(Batch batch, float parentAlpha) {
        border.draw(batch, parentAlpha);
        background.draw(batch);
    }

    /** Adds the notification content to the stage for a brief duration, then removes itself. */
    abstract void show();

    /** Immediately removes this notification (actor) from the stage. */
    void hide() {
        clearActions();
        remove();
    }
}
