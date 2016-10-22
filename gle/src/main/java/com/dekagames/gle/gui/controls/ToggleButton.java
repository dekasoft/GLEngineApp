package com.dekagames.gle.gui.controls;

import com.dekagames.gle.Atlas;
import com.dekagames.gle.Graphics;

/**
 * Created by deka on 07.01.15.
 */
public class ToggleButton extends Button {
    private boolean wait_touch_up;   // wait touch up after touch down to skip any action


    public ToggleButton(Atlas atlas, String name, float x, float y) {
        super(atlas, name, x, y);
    }

    public boolean isPressed(){
        return is_pressed;
    }

    /**
     * Draw the button on the window. This method is invoked by window, and usually no need to call it manually.
     * @param graphics {@link com.dekagames.gle.Graphics} to draw sprites.
     */
    @Override
    public void draw(Graphics graphics) {
        if (!bVisible)	return;

        if (is_pressed) sprite.setFrame(1);
        else 			sprite.setFrame(0);

        sprite.draw(graphics, fx+parent.getLeft(), fy+parent.getTop());
    }


    @Override
    public void controlTouched(boolean down) {
        if (bVisible) {
            if (is_pressed) {
                if (wait_touch_up)              // skip touch up immediately after touch down
                    wait_touch_up = false;
                else {
                    if (!down) {
                        is_pressed = false;      // button up with touch up
                        bDone = true;
                    }
                }
            } else {
                if (down) {
                    is_pressed = true;
                    bDone = true;
                    wait_touch_up = true;
                }
            }
        }
        return;// is_pressed;
    }

    @Override
    public void touchIn() {
        // NOTHING TO DO HERE!!!!
    }

    @Override
    public void touchOut() {
        wait_touch_up = false;
    }
}
