package com.mare5x.chargehockey.notifications;


import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mare5x.chargehockey.ChargeHockeyGame;

// todo keep track of added notification; display multiple notifications simulataneously
public abstract class Notification extends Table {
    public static final float DEFAULT_SHOW_TIME = 1.5f;

    final Stage stage;

    // NOTE: stage must use screen coordinates.
    Notification(ChargeHockeyGame game, Stage stage) {
        super(game.skin);

//        setDebug(true, true);

        this.stage = stage;

        setBackground(game.skin.getDrawable("button_up"));

        // the default position and size is at the top of the screen
        float width = stage.getWidth() * 0.8f;
        float height = stage.getHeight() * 0.25f;
        setBounds(stage.getWidth() / 2 - width / 2, stage.getHeight() * 0.95f - height, width, height);

        // dismiss notifications with a click
        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hide();
            }
        });
    }

    @Override
    public void setSize(float width, float height) {
        super.setSize(width, height);

        setPosition(stage.getWidth() / 2 - width / 2, stage.getHeight() * 0.95f - height);
    }

    Action get_action() {
        return get_action(DEFAULT_SHOW_TIME);
    }

    /** Returns a default fade in/out action. */
    Action get_action(float time) {
        return Actions.sequence(
                Actions.alpha(0.4f),
                Actions.show(),
                Actions.fadeIn(0.4f),
                Actions.delay(time),
                Actions.fadeOut(0.6f),
                Actions.hide(),
                Actions.removeActor()
        );
    }

    float get_padding() {
        return 10 * ChargeHockeyGame.DENSITY;
    }

    public boolean is_displayed() {
        return hasParent();
    }

    @Override
    /* The default implementation draws the notification border. Override and call super. */
    public void draw(Batch batch, float parentAlpha) {
        float alpha = getColor().a;  // the action changes alpha through getColor

        super.draw(batch, parentAlpha * alpha);
    }

    public void show() {
        show(DEFAULT_SHOW_TIME);
    }

    /** Adds the notification content to the stage for 'time' duration, then removes itself. */
    public abstract void show(float time);

    /** Immediately removes this notification (actor) from the stage. */
    protected void hide() {
        clearActions();
        remove();
    }
}
