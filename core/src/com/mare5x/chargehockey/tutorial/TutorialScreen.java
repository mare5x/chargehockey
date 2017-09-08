package com.mare5x.chargehockey.tutorial;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.mare5x.chargehockey.ChargeHockeyGame;
import com.mare5x.chargehockey.actors.ForcePuckActor;
import com.mare5x.chargehockey.game.PlayMenuScreen;
import com.mare5x.chargehockey.menus.ScrollableMenuScreen;

public class TutorialScreen extends ScrollableMenuScreen {
    private final TextureAtlas.AtlasRegion puck_region;
    private final Image pos_img;
    private final Image neg_img;

    public TutorialScreen(ChargeHockeyGame game) {
        super(game);

        puck_region = game.sprites.findRegion("puck");
        pos_img = new Image(game.sprites.findRegion("charge_pos"));
        neg_img = new Image(game.sprites.findRegion("charge_neg"));


        intro();
    }

    private void intro() {
        Label welcome_label = make_label("WELCOME TO CHARGE HOCKEY!");
        welcome_label.setFontScale(1.5f);
        Label sub_title = make_label("A GAME BASED ON THE PHYSICS OF ELECTRIC CHARGES");

        Table goal = get_intro_goal();

        TextButton next_button = make_text_button("NEXT");
        next_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                fade_out(new Runnable() {
                    @Override
                    public void run() {
                        charge_tutorial();
                    }
                });
            }
        });

        table.clear();

        add_back_button();

        table.defaults().pad(15);
        table.add(welcome_label).expandX().fill().row();
        table.add(sub_title).expandX().fill().row();
        table.add().expand().fill().row();
        table.add(goal).expandX().fillX().row();
        table.add().expand().fill().row();
        table.add(next_button).minHeight(MIN_BUTTON_HEIGHT).width(3 * MIN_BUTTON_HEIGHT).expandX().right();

        fade_in();
    }

    private Table get_intro_goal() {
        Table goal_table = new Table();
        goal_table.pad(10);
        goal_table.setBackground(game.skin.getDrawable("button_up"));

        Image goal_img = new Image(game.sprites.findRegion("grid/grid_goal"));
        Image wall_img = new Image(game.sprites.findRegion("grid/grid_wall"));

        Label goal_1 = make_label("YOUR GOAL IS TO GET ALL PUCKS");
        Label goal_2 = make_label("INTO THE GOAL");
        Label goal_3 = make_label("WITHOUT HITTING THE WALLS");
        Label goal_4 = make_label("PUCKS ARE MOVED USING CHARGES");

        goal_table.defaults().minHeight(MIN_BUTTON_HEIGHT).space(15);
        goal_table.add(goal_1).fill().width(get_label_width(Value.percentWidth(0.6f, goal_table)));
        goal_table.add(new Image(puck_region)).colspan(2).center().size(MIN_BUTTON_HEIGHT);
        goal_table.row();
        goal_table.add(goal_2).fill().width(get_label_width(Value.percentWidth(0.6f, goal_table)));
        goal_table.add(goal_img).colspan(2).center().size(MIN_BUTTON_HEIGHT);
        goal_table.row();
        goal_table.add(goal_3).fill().width(get_label_width(Value.percentWidth(0.6f, goal_table)));
        goal_table.add(wall_img).colspan(2).size(MIN_BUTTON_HEIGHT).center();
        goal_table.row();
        goal_table.add(goal_4).fill().width(get_label_width(Value.percentWidth(0.6f, goal_table)));
        goal_table.add(pos_img).size(MIN_BUTTON_HEIGHT);
        goal_table.add(neg_img).size(MIN_BUTTON_HEIGHT);

        return goal_table;
    }

    private void charge_tutorial() {
        TextButton back_button = make_text_button("BACK");
        back_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                fade_out(new Runnable() {
                    @Override
                    public void run() {
                        intro();
                    }
                });
            }
        });

        TextButton next_button = make_text_button("NEXT");
        next_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                interface_tutorial();
            }
        });

        float vector_length = 2 * MIN_BUTTON_HEIGHT;
        float vector_height = 0.5f * MIN_BUTTON_HEIGHT;

        Image pos_vector = new Image(game.sprites.findRegion("blank_vector"));
        pos_vector.setColor(ForcePuckActor.POS_RED);
        Image neg_vector = new Image(game.sprites.findRegion("blank_vector"));
        neg_vector.setColor(ForcePuckActor.NEG_BLUE);
        neg_vector.setSize(vector_length, vector_height);
        neg_vector.setOrigin(Align.center);
        neg_vector.rotateBy(180);

        float move_len = vector_length + MIN_BUTTON_HEIGHT + 30;  // space = 15; * 2
        Image puck_pos = new Image(puck_region);
        RepeatAction pos_move_action = Actions.forever(
                Actions.sequence(
                    Actions.delay(0.1f),
                    Actions.moveBy(move_len, 0, 1, Interpolation.pow2In),
                    Actions.delay(0.5f),
                    Actions.moveBy(-move_len, 0)
                )
        );

        Image puck_neg = new Image(puck_region);
        RepeatAction neg_move_action = Actions.forever(
                Actions.sequence(
                        Actions.delay(0.1f),
                        Actions.moveBy(move_len, 0),
                        Actions.moveBy(-move_len, 0, 1, Interpolation.pow2In),
                        Actions.delay(0.5f)
                )
        );

        Label pos_label = make_label("POSITIVELY CHARGED PUCKS GET REPELLED FROM POSITIVELY CHARGED CHARGES");

        Table pos_charge_tutorial = new Table();
        pos_charge_tutorial.defaults().minHeight(MIN_BUTTON_HEIGHT).space(15);
        pos_charge_tutorial.add(pos_label).fill().expandX().width(Value.percentWidth(0.95f, pos_charge_tutorial)).colspan(5).row();
        pos_charge_tutorial.add().expandX();
        pos_charge_tutorial.add(pos_img).size(MIN_BUTTON_HEIGHT);
        pos_charge_tutorial.add(puck_pos).size(MIN_BUTTON_HEIGHT);
        pos_charge_tutorial.add(pos_vector).left().size(vector_length, vector_height).center();
        pos_charge_tutorial.add().expandX();
        puck_pos.addAction(pos_move_action);

        Label neg_label = make_label("POSITIVELY CHARGED PUCKS GET ATTRACTED TO NEGATIVELY CHARGED CHARGES");

        Table neg_charge_tutorial = new Table();
        neg_charge_tutorial.defaults().minHeight(MIN_BUTTON_HEIGHT).space(15);
        neg_charge_tutorial.add(neg_label).fill().expandX().width(Value.percentWidth(0.95f, neg_charge_tutorial)).colspan(5).row();
        neg_charge_tutorial.add().expandX();
        neg_charge_tutorial.add(neg_img).size(MIN_BUTTON_HEIGHT);
        neg_charge_tutorial.add(puck_neg).size(MIN_BUTTON_HEIGHT);
        neg_charge_tutorial.add(neg_vector).left().size(vector_length, vector_height).center();
        neg_charge_tutorial.add().expandX();
        puck_neg.addAction(neg_move_action);

        Label rule_label = make_label("OPPOSITE CHARGES ATTRACT AND LIKE CHARGES REPEL");
        rule_label.setFontScale(1.25f);

        table.clear();

        table.defaults().colspan(2).pad(15);

        add_back_button();

        table.add().expand().fill().row();

        table.add(rule_label).fillX().row();
        table.add(pos_charge_tutorial).fillX().row();
        table.add(neg_charge_tutorial).fillX().row();

        table.add().expand().fill();
        table.row().minHeight(MIN_BUTTON_HEIGHT).width(3 * MIN_BUTTON_HEIGHT).expandX();
        table.add(back_button).left().colspan(1);
        table.add(next_button).right().colspan(1);

        fade_in();
    }

    private void interface_tutorial() {
        TextButton back_button = make_text_button("BACK");
        back_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                fade_out(new Runnable() {
                    @Override
                    public void run() {
                        charge_tutorial();
                    }
                });
            }
        });

        TextButton next_button = make_text_button("FINISH");
        next_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                fade_out(new Runnable() {
                    @Override
                    public void run() {
                        have_fun();
                    }
                });
            }
        });

        Image play_img = new Image(game.skin.getDrawable("play_up"));
        Image play_img_2 = new Image(game.skin.getDrawable("play_up"));

        Image pos_img_2 = new Image(game.sprites.findRegion("charge_pos"));
        Image neg_img_2 = new Image(game.sprites.findRegion("charge_neg"));

        Table button_table = new Table();
        button_table.setBackground(game.skin.getDrawable("pixels/px_darkbrown"));
        button_table.pad(15);
        button_table.defaults().space(15).size(MIN_BUTTON_HEIGHT);
        button_table.add(play_img);
        button_table.add(pos_img);
        button_table.add(neg_img);

        Label ui_label_1 = make_label("CLICK ON");
        Label ui_label_2 = make_label("TO ADD THE DESIRED CHARGE TO THE CENTER OF THE SCREEN");
        Label ui_label_3 = make_label("DRAG THE CHARGE TO A DESIRED POSITION AND WHEN YOU ARE READY TO RUN THE SIMULATION, PRESS");
        Label ui_label_4 = make_label("TO REMOVE A CHARGE, DRAG IT TO THE BOTTOM OF THE SCREEN");

        Table ui_tutorial_table = new Table();
        ui_tutorial_table.setBackground(game.skin.getDrawable("button_up"));
        ui_tutorial_table.pad(15);
        ui_tutorial_table.defaults().minHeight(MIN_BUTTON_HEIGHT).space(15).pad(10);
        ui_tutorial_table.add(ui_label_1).fillX().width(get_label_width(Value.percentWidth(0.6f, ui_tutorial_table))).colspan(2);
        ui_tutorial_table.add(pos_img_2).size(MIN_BUTTON_HEIGHT);
        ui_tutorial_table.add(neg_img_2).size(MIN_BUTTON_HEIGHT).row();
        ui_tutorial_table.add(ui_label_2).fillX().colspan(4).center().row();
        ui_tutorial_table.add(ui_label_3).fillX().width(get_label_width(Value.percentWidth(0.6f, ui_tutorial_table))).colspan(3).center();
        ui_tutorial_table.add(play_img_2).size(MIN_BUTTON_HEIGHT).row();
        ui_tutorial_table.add(ui_label_4).fillX().colspan(4);

        Label zoom_tip = make_label("TIP: ZOOM BY PINCHING OR BY DOUBLE TAPPING");
        Label settings_tip = make_label("TIP: EXPLORE THE SETTINGS MENU");
        Label editor_tip = make_label("TIP: CREATE YOUR OWN LEVELS USING THE BUILT-IN CUSTOM EDITOR");
        Label symmetry_tip = make_label("TIP: USE THE GRID AND THE SYMMETRY TOOL FOR PRECISE ACTIONS");

        table.clear();

        table.defaults().colspan(2).pad(15);

        add_back_button();

        table.add().expand().fill().row();

        table.add(button_table).fillX().row();
        table.add(ui_tutorial_table).fillX().row();

        table.add(zoom_tip).padTop(MIN_BUTTON_HEIGHT).fillX().row();
        table.add(settings_tip).fillX().row();
        table.add(editor_tip).fillX().row();
        table.add(symmetry_tip).fillX().row();

        table.add().expand().fill();
        table.row().minHeight(MIN_BUTTON_HEIGHT).width(3 * MIN_BUTTON_HEIGHT).expandX();
        table.add(back_button).left().colspan(1);
        table.add(next_button).right().colspan(1);

        fade_in();
    }

    private void have_fun() {
        Label have_fun = make_label("HAVE FUN", false);
        have_fun.setFontScale(2);

        table.clear();

        table.add(have_fun).pad(15).width(Value.percentWidth(0.6f, table));

        fade_in();

        stage.addAction(Actions.sequence(Actions.delay(1), Actions.run(new Runnable() {
            @Override
            public void run() {
                back_key_pressed();
            }
        })));
    }

    /** Packs the label. Use when setting the width of the label in a table. */
    private Value get_label_width(final Value max_width) {
        return new Value() {
            @Override
            public float get(Actor context) {
                return Math.min(context.getWidth(), max_width.get(null));
            }
        };
    }

    @Override
    protected void back_key_pressed() {
        set_screen(new PlayMenuScreen(game));
    }

    @Override
    public void hide() {
        dispose();
    }
}
