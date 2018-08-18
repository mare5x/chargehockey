package com.mare5x.chargehockey.notifications;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mare5x.chargehockey.ChargeHockeyGame;
import com.mare5x.chargehockey.menus.BaseMenuScreen;

/* Only for testing purposes! */
public class NotificationTestScreen extends BaseMenuScreen {
    public NotificationTestScreen(final ChargeHockeyGame game) {
        super(game);

        TextButton text_button = make_text_button("TextNotification");
        text_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                show_notification("TextNotification");
            }
        });

        TextButton long_text_button = make_text_button("Long TextNotification");
        long_text_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                show_notification("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec sodales mi id dui sodales, a pharetra purus tempor. Sed feugiat suscipit tortor, non tempus lacus bibendum a. Integer vel arcu felis. Donec dictum velit elit, non mollis ipsum consequat id. Vestibulum vitae felis blandit, rutrum tortor vel, dictum nisl. Donec at metus ullamcorper, ultricies justo eu, porttitor libero. Interdum et malesuada fames ac ante ipsum primis in faucibus. In et orci nulla. Mauris a ullamcorper dui. Aliquam in sodales risus. Phasellus convallis molestie ipsum, nec viverra mauris dignissim varius. In et facilisis urna.\n" +
                        "\n" +
                        "Donec nunc magna, ultricies a facilisis nec, vulputate sed sem. Cras luctus volutpat lacus nec commodo. Etiam blandit tempor sem, sed efficitur purus. Nunc sit amet arcu eleifend, lacinia urna nec, mollis augue. Quisque suscipit sapien quis aliquam accumsan. Curabitur erat orci, blandit a eros in, lacinia vulputate neque. Aliquam commodo magna sit amet molestie semper. Praesent eu pulvinar arcu, gravida blandit metus.");
            }
        });

        TextButton no_charges_button = make_text_button("NoChargesNotification");
        no_charges_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                show_notification(new NoChargesNotification(game, stage));
            }
        });

        TextButton editor_paint_button = make_text_button("EditorPaintNotification");
        editor_paint_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                show_notification(new EditorPaintTipNotification(game, stage));
            }
        });

        TextButton editor_no_levels_button = make_text_button("EditorNoLevelsNotification");
        editor_no_levels_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                show_notification(new EditorNoLevelsNotification(game, stage));
            }
        });

        add_back_button();
        add_text_button(text_button).row();
        add_text_button(long_text_button).row();
        add_text_button(no_charges_button).row();
        add_text_button(editor_paint_button).row();
        add_text_button(editor_no_levels_button).row();
    }

    @Override
    protected void back_key_pressed() {
        set_screen(game.menu_screen);
    }

    @Override
    public void hide() {
        dispose();
    }
}
