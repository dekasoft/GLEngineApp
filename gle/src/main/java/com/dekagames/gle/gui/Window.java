package com.dekagames.gle.gui;

import com.dekagames.gle.Graphics;
import com.dekagames.gle.Sprite;
import com.dekagames.gle.GLE;

import java.util.ArrayList;

/**
 * Created by deka on 23.07.14.
 */
public class Window {
//    protected   Screen                      screen;
    protected   GLE                     gle;
    protected   ArrayList<Control>      controls;
    protected   boolean                 skinned;
    protected   Sprite                  spr_skin;
    protected	int						n_top, n_left, n_width, n_height;		// положение в пикселях базового экрана
    protected	int						n_width_cells, n_height_cells;			// размеры окна в спрайтах
    protected	Control				    prev_control, now_control;//, done_control;


    /**
     * Create window from skin sprite - tiled window image.
     *
     * @param skin спрайт, представляющий собой 9 кадров слева направо-сверху вниз (части окна). Все кадры одинакового
     *             размера.
     * @param x координата x левого верхнего угла окна в пикселях
     * @param y координата y левого верхнего угла окна в пикселях
     * @param width_cells количество ячеек окна по горизонтали (без учета крайних)
     * @param height_cells количество ячеек окна по вертикали (без учета крайних)
     */
    public Window(GLE gle,Sprite skin, int x, int y, int width_cells, int height_cells) {
        this.gle = gle;
        skinned = true;
        spr_skin = skin;
        n_left = x;
        n_top = y;
        n_width_cells = width_cells+2;
        n_height_cells = height_cells+2;
        n_width = spr_skin.getFrameWidth(0)*n_width_cells;
        n_height = spr_skin.getFrameHeight(0)*n_height_cells;
        spr_skin.setHotSpot(0, 0);
        controls = new ArrayList<Control>();
    }


    /**
     * Create window from full window image. Size of the window = size of the image. Not scaled.
     *
     * @param bkImage sprite with full window image (window background and decoration without controls)
     * @param x x position  of window (top left corner)
     * @param y y position of window (top left corner)
     */
    public Window(GLE gle, Sprite bkImage, int x, int y){
        this.gle = gle;
        skinned = false;
        spr_skin = bkImage;
        n_left = x;
        n_top = y;
        n_width = spr_skin.getFrameWidth(0);
        n_height = spr_skin.getFrameHeight(0);
        spr_skin.setHotSpot(0, 0);
        controls = new ArrayList<Control>();
    }


    /**
     * Нерисуемое окно. Требуется для контролов, которые находятся вне окна - просто на экране.
     * Так как контрол не может не иметь родительского окна, создается такое, прозрачное окно.
     */
    public Window(GLE gle, int width, int height) {
        this.gle = gle;
        spr_skin = null;

        n_left = 0;
        n_top = 0;
        n_width_cells = 0;
        n_height_cells = 0;
        n_width =width;      // full screen size
        n_height = height;
        controls = new ArrayList<Control>();
    }


    public void initControls(){
        controls.clear();
    }

//    public void setScreen(Screen screen){
//        this.screen = screen;
//    }


//    public Screen getScreen(){
//        return screen;
//    }


    public GLE getGLE(){
        return gle;
    }

    public boolean isPointIn(float x, float y) {
        return (x>=n_left && x<=n_left+n_width && y>n_top && y<n_top+n_height);
    }


    public void moveTo(int x, int y) {
        n_left = x;
        n_top = y;
    }


    public int getLeft()	{ return n_left;}
    public int getTop()		{ return n_top;}
    public int getWidth()	{ return n_width;}
    public int getHeight()	{ return n_height;}

    // эти методы нужны для задания размеров невидимого окна больше, чем размер экрана
    // полезно для экранов выбора уровня, карт и т.д.
    public void setWidth(int w)		{ n_width = w;}
    public void setHeight(int h)	{ n_height = h;}

    public void addCtrl(Control c) {
        if (c == null) return;
        c.setParentWindow(this);
        controls.add(c);
    }


    public ArrayList<Control> getControls(){
        return controls;
    }


    /**
     * вызывается при таче или антаче в окне.
     */
    public void windowTouched(boolean down, float x, float y) {
   //     boolean is_any_control_pressed =false;

        for (Control ctrl:controls) {
            if (ctrl.isPointIn(x,y)){
                // if click was in control's area - clear touched flags
                gle.input.was_untouched = false;
                gle.input.was_touched = false;
                ctrl.controlTouched(down);
            }
        }
    }

    /**
     * вызывается при движении тачнутого указателя в окне
     */
    public void touchMove(float x, float y) {
        prev_control = now_control;
        now_control = null;

        for (Control ctrl:controls) {
            if (ctrl.isPointIn(x,y)) {
                now_control = ctrl;
                ctrl.touchMove(x-ctrl.fx, y-ctrl.fy);
            }
        }

        if (prev_control != now_control) {
            if (prev_control!=null) prev_control.touchOut();
            if (now_control!=null)	now_control.touchIn();
        }

    }


    public void touchIn() {
    }

    public void touchOut() {
        if (prev_control!=null) {
            prev_control.touchOut();
            prev_control = null;
        }
        if (now_control!=null)	{
            now_control.touchOut();
            now_control = null;
        }
    }


    public void draw(Graphics graphics) {
        if (spr_skin != null) draw_window(graphics);
        for (Control c:controls)
            c.draw(graphics);
    }

    public Control update(float delta) {
        Control doneCtrl = null;
        for (Control ctrl:controls)
            if (ctrl.update(delta))
                doneCtrl = ctrl;

        return doneCtrl;
    }

    protected void draw_window(Graphics gr) {
        float tmpy;
        float sprw = spr_skin.getFrameWidth(0);
        float sprh = spr_skin.getFrameHeight(0);

        if (skinned) {      // draw tiled background
            //	draw top line
            tmpy = n_top;

            spr_skin.setFrame(0);
            spr_skin.draw(gr, n_left, tmpy);

            spr_skin.setFrame(1);
            for (int i = 1; i < n_width_cells - 1; i++)
                spr_skin.draw(gr, n_left + i * sprw, tmpy);

            spr_skin.setFrame(2);
            spr_skin.draw(gr, n_left + (n_width_cells - 1) * sprw, tmpy);

            // draw middle part
            for (int j = 1; j < n_height_cells - 1; j++) {
                tmpy = n_top + j * sprh;
                spr_skin.setFrame(3);
                spr_skin.draw(gr, n_left, tmpy);

                spr_skin.setFrame(4);
                for (int i = 1; i < n_width_cells - 1; i++)
                    spr_skin.draw(gr, n_left + i * sprw, tmpy);

                spr_skin.setFrame(5);
                spr_skin.draw(gr, n_left + (n_width_cells - 1) * sprw, tmpy);
            }

            // draw bottom line
            tmpy = n_top + (n_height_cells - 1) * sprh;

            spr_skin.setFrame(6);
            spr_skin.draw(gr, n_left, tmpy);

            spr_skin.setFrame(7);
            for (int i = 1; i < n_width_cells - 1; i++)
                spr_skin.draw(gr, n_left + i * sprw, tmpy);

            spr_skin.setFrame(8);
            spr_skin.draw(gr, n_left + (n_width_cells - 1) * sprw, tmpy);
        }
        else {      // draw simple background image
            spr_skin.draw(gr, n_left, n_top);
        }

    }

}
