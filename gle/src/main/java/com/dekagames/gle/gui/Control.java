package com.dekagames.gle.gui;


import com.dekagames.gle.Graphics;

/**
 * Base class for all controls.
 * If at least one of the methods (touchDown, touchUp, touchOver) return true - this control is returning
 * in Gui.update() method for processing. If we do not want process this events this methods should return false.
 * All gui controls must have parent window.
 */
public abstract class Control {
    protected   Window      parent;
//    protected	float		scale;
    public 		float 		fx,fy, fwidth, fheight;
    /** Flag, if its true, than parent window must process this control  */
    public		boolean		bDone;
    public 		boolean		bVisible;

    public Control() {
        bVisible = true;
    }

    public void setParentWindow(Window window){
        parent = window;
    }

    public abstract void draw(Graphics graphics);


    /**
     * Возвращает координату X положения элемента управления контрола в экранных координатах.
     * @return координата X элемента управления.
     */
    public float getScreenPosX(){
        return (fx+parent.getLeft());
    }


    /**
     * Возвращает координату Y положения элемента управления контрола в экранных координатах.
     * @return координата Y элемента управления.
     */
    public float getScreenPosY(){
        return (fy+parent.getTop());
    }


    public boolean isPointIn(float x, float y) {
        return false;
    }

    public boolean 	update(float delta) {
        if (bVisible && bDone) {
            bDone = false;
            return true;
        }
        else
            return false;
    }

    /** Called by parent window when touch or detouch event is occured.
     *  @return true if control is currently touched
     * */
    public abstract void controlTouched(boolean down);

    /** Called by the window when touched pointer is moving above the control  */
    public void touchMove(float x, float y){};
    public void touchIn(){};
    public void touchOut(){};
}
