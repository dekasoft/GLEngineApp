package com.dekagames.gle.gui.controls;

import com.dekagames.gle.Graphics;
import com.dekagames.gle.Sprite;
import com.dekagames.gle.gui.Control;

/**
 * Created by deka on 02.02.15.
 */
public class Image extends Control {
    private Sprite sprite;

    public Image(Sprite sprite){
        super();
        if (sprite != null) {
            this.sprite = sprite;
            fwidth = sprite.getFrameWidth(0);
            fheight =sprite.getFrameHeight(0);
        }
    }

    public Sprite getSprite(){
        return sprite;
    }

    public void setPosition(float x, float y){
        fx = x;
        fy = y;
    }

    @Override
    public void draw(Graphics graphics) {
        if (sprite != null)
            sprite.draw(graphics, fx+parent.getLeft(), fy+parent.getTop());
    }

    @Override
    public boolean update(float delta){
        if (sprite != null)
            sprite.update(delta);
        return false;       // image does not process events
    }

    @Override
    public void controlTouched(boolean down) {

    }
}
