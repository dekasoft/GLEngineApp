package com.dekagames.gle.gui.controls;


import com.dekagames.gle.Atlas;
import com.dekagames.gle.Graphics;
import com.dekagames.gle.Sprite;
import com.dekagames.gle.gui.Control;

/** Basic button class. Any button may have up to three stages: unpressed, pressed, unabled.
 */
public class Button extends Control {
    protected       Sprite          sprite;
    protected 		boolean 		is_pressed;
    protected		int 			n_stages;
    public 			boolean			enabled;
    /** Place to store any of user data */
    public			Object			tag;

    /**
     * @deprecated
     * Create the button from the sprite.
     * @param stages how many stages has the button
     * @param spr sprite for button. Must consist of up to three frames (amount of frames = amount of stages).
     * @param x X position of the button, relatively of the upper left corner of the parent window
     * @param y Y position of the button, relatively of the upper left corner of the parent window
     */
    public Button(int stages, Sprite spr, float x, float y) {
        super();
        if (stages<=0) 	stages = 1;
        if (stages>3) 	stages = 3;
        n_stages = stages;
        fx = x;
        fy = y;
        fwidth = spr.getFrameWidth(0);
        fheight = spr.getFrameHeight(0);
        sprite = spr;
        enabled = true;
    }

    /**
     * Create the button from the sprite, which placed in the SlonNode structure. This method is using for loading
     * button sprite from the texture atlas, made with TexturePacker tool.
     * @param stages how many stages has the button
     * @param tex texture atlas create by TexturePacker.
     * @param node sprite node from *.atlas file which represent button sprite
     * @param x X position of the button, relatively of the upper left corner of the parent window
     * @param y Y position of the button, relatively of the upper left corner of the parent window
     */
//    public Button(int stages, Texture tex, SlonNode node, float x, float y) {
//        super();
//        if (stages<=0) 	stages = 1;
//        if (stages>3) 	stages = 3;
//        n_stages = stages;
//        fx = x;
//        fy = y;
//        sprite = new Sprite(tex,node,1);
//        fwidth = sprite.getFrameWidth(0);
//        fheight = sprite.getFrameHeight(0);
//        enabled = true;
//    }


    /**
     * Create the button from the atlas node. This method is using for loading
     * button sprite from the texture atlas, made with TexturePacker tool. Button has that number of stages
     * that frames in sprite node.
     * @param atlas atlas in which button sprite contains in "sprites" section
     * @param name button sprite name in atlas.
     * @param x X position of the button, relatively of the upper left corner of the parent window
     * @param y Y position of the button, relatively of the upper left corner of the parent window
     */
    public Button(Atlas atlas, String name, float x, float y){
        super();
        sprite = atlas.getSprite(name, 1);
        n_stages = sprite.getFramesCount();
        if (n_stages <= 0) n_stages = 1;
        if (n_stages > 3) n_stages = 3;
        fx = x;
        fy = y;
        fwidth = sprite.getFrameWidth(0);
        fheight = sprite.getFrameHeight(0);
        enabled = true;
    }


    /**
     * Draw the button on the window. This method is invoked by window, and usually no need to call it manually.
     * @param graphics {@link com.dekagames.gle.Graphics} to draw sprites.
     */
    @Override
    public void draw(Graphics graphics) {
        if (!bVisible)	return;
        if (n_stages == 1) 			// 1-stage button
            sprite.setFrame(0);
        else if (n_stages == 2) {	// 2-stage button
            if (is_pressed) sprite.setFrame(1);
            else 			sprite.setFrame(0);
        }
        else if (n_stages == 3) {	// 3-stage button
            if (!enabled) 		sprite.setFrame(2);
            else {
                if (is_pressed) sprite.setFrame(1);
                else 			sprite.setFrame(0);
            }
        }
        sprite.draw(graphics, fx+parent.getLeft(), fy+parent.getTop());
    }

    @Override
    public boolean isPointIn(float x, float y) {
        return (x>=fx && x<=fx+fwidth && y>=fy && y<=fy+fheight);
    }

    @Override
    public void controlTouched(boolean down) {
        if (!enabled) {
            is_pressed = false;
            return;// false;
        }

        is_pressed = down;
        if (!is_pressed && bVisible) {
            bDone = true;            // window will process this button
        }
        return;// is_pressed;
    }

    @Override
    public void touchIn() {
//        System.out.println("touch in");
        if (!enabled)
            is_pressed = false;
        else
            is_pressed = true;
    }

    @Override
    public void touchOut() {
//        System.out.println("touch out");
        is_pressed = false;
    }

//    @Override
//    public void touchMove(float x, float y) {
//        System.out.println("touch move");
//    }

}
