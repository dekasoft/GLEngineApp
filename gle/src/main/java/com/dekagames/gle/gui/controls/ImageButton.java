package com.dekagames.gle.gui.controls;

import com.dekagames.gle.Atlas;
import com.dekagames.gle.Graphics;
import com.dekagames.gle.Sprite;

/**
 *  This class represents a Button with image on it. ImageButton consists of two separate
 *  images: image of button itself, and image drawn onto the button. Both of this images
 *  may consists of some frames (for each stage) as like as general Button.
 */
public class ImageButton extends Button {
    protected       int             n_img_stages;
    protected       Sprite          sprImage ;

    /**
     * @deprecated
     * Create the button from the sprite.
     * @param stages how many stages has the button
     * @param sprBtn sprite for button. Must consists of up to three frames (amount of frames = amount of stages).
     * @param sprImg sprite for image. MAY consists some frames <= frames of sprButton.
     * @param x X position of the button, relatively of the upper left corner of the parent window
     * @param y Y position of the button, relatively of the upper left corner of the parent window
     */
    public ImageButton(int stages, Sprite sprBtn, Sprite sprImg, float x, float y) {
        super(stages, sprBtn, x, y);
        sprImage = sprImg;
    }

    /**
     * Create the button from the sprite, which placed in the SlonNode structure. This method is using for loading
     * button sprite from the texture atlas, made with TexturePacker tool.
     * @param atlas atlas in which to sprites.
     * @param btnSprName button sprite node from *.atlas file which represent button "base"
     * @param imgSprName image sprite node from *.atlas file which represent button image
     * @param x X position of the button, relatively of the upper left corner of the parent window
     * @param y Y position of the button, relatively of the upper left corner of the parent window
     */
    public ImageButton(Atlas atlas, String btnSprName, String imgSprName, float x, float y) {
        super(atlas,  btnSprName, x, y);
        sprImage = atlas.getSprite(imgSprName, 1);
    }

    /**
     * Draw the button on the window. This method is invoked by window, and usually no need to call it manually.
     * @param graphics {@link com.dekagames.gle.Graphics} to draw sprites.
     */
    @Override
    public void draw(Graphics graphics) {
        super.draw(graphics);
        if (!bVisible)	return;
        int n_images = sprImage.getFramesCount();
        if (n_images == 1) 			// 1-stage button
            sprImage.setFrame(0);
        else if (n_images == 2) {	// 2-stage button
            if (is_pressed) sprImage.setFrame(1);
            else 			sprImage.setFrame(0);
        }
        else if (n_images == 3) {	// 3-stage button
            if (!enabled) 		sprImage.setFrame(2);
            else {
                if (is_pressed) sprImage.setFrame(1);
                else 			sprImage.setFrame(0);
            }
        }
        // draw image at the center of the button
        sprImage.draw(graphics, fx+parent.getLeft()+fwidth/2, fy+parent.getTop()+fheight/2);
    }
}
