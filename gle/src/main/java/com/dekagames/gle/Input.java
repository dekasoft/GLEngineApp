package com.dekagames.gle;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.MotionEvent;
import android.view.View;

import java.util.HashSet;

/**
 * Created with IntelliJ IDEA.
 * User: Deka
 * Date: 22.10.13
 * Time: 20:23
 * To change this template use File | Settings | File Templates.
 */
public class Input implements View.OnTouchListener, SensorEventListener {

    // инициализация переменных правильными кодами клавиш
    static {
        ANY_KEY = -1;
        NUM_0 = 7;
        NUM_1 = 8;
        NUM_2 = 9;
        NUM_3 = 10;
        NUM_4 = 11;
        NUM_5 = 12;
        NUM_6 = 13;
        NUM_7 = 14;
        NUM_8 = 15;
        NUM_9 = 16;
        A = 29;
        ALT_LEFT = 57;
        ALT_RIGHT = 58;
        APOSTROPHE = 75;
        AT = 77;
        B = 30;
        BACK = 4;
        BACKSLASH = 73;
        C = 31;
        CALL = 5;
        CAMERA = 27;
        CLEAR = 28;
        COMMA = 55;
        D = 32;
        DEL = 67;
        BACKSPACE = 67;
        FORWARD_DEL = 112;
        DPAD_CENTER = 23;
        DPAD_DOWN = 20;
        DPAD_LEFT = 21;
        DPAD_RIGHT = 22;
        DPAD_UP = 19;
        CENTER = 23;
        DOWN = 20;
        LEFT = 21;
        RIGHT = 22;
        UP = 19;
        E = 33;
        ENDCALL = 6;
        ENTER = 66;
        ENVELOPE = 65;
        EQUALS = 70;
        EXPLORER = 64;
        F = 34;
        FOCUS = 80;
        G = 35;
        GRAVE = 68;
        H = 36;
        HEADSETHOOK = 79;
        HOME = 3;
        I = 37;
        J = 38;
        K = 39;
        L = 40;
        LEFT_BRACKET = 71;
        M = 41;
        MEDIA_FAST_FORWARD = 90;
        MEDIA_NEXT = 87;
        MEDIA_PLAY_PAUSE = 85;
        MEDIA_PREVIOUS = 88;
        MEDIA_REWIND = 89;
        MEDIA_STOP = 86;
        MENU = 82;
        MINUS = 69;
        MUTE = 91;
        N = 42;
        NOTIFICATION = 83;
        NUM = 78;
        O = 43;
        P = 44;
        PERIOD = 56;
        PLUS = 81;
        POUND = 18;
        POWER = 26;
        Q = 45;
        R = 46;
        RIGHT_BRACKET = 72;
        S = 47;
        SEARCH = 84;
        SEMICOLON = 74;
        SHIFT_LEFT = 59;
        SHIFT_RIGHT = 60;
        SLASH = 76;
        SOFT_LEFT = 1;
        SOFT_RIGHT = 2;
        SPACE = 62;
        STAR = 17;
        SYM = 63;
        T = 48;
        TAB = 61;
        U = 49;
        UNKNOWN = 0;
        V = 50;
        VOLUME_DOWN = 25;
        VOLUME_UP = 24;
        W = 51;
        X = 52;
        Y = 53;
        Z = 54;
        META_ALT_LEFT_ON = 16;
        META_ALT_ON = 2;
        META_ALT_RIGHT_ON = 32;
        META_SHIFT_LEFT_ON = 64;
        META_SHIFT_ON = 1;
        META_SHIFT_RIGHT_ON = 128;
        META_SYM_ON = 4;
        CONTROL_LEFT = 129;
        CONTROL_RIGHT = 130;
        ESCAPE = 131;
        END = 132;
        INSERT = 133;
        PAGE_UP = 92;
        PAGE_DOWN = 93;
        PICTSYMBOLS = 94;
        SWITCH_CHARSET = 95;
        BUTTON_CIRCLE = 255;
        BUTTON_A = 96;
        BUTTON_B = 97;
        BUTTON_C = 98;
        BUTTON_X = 99;
        BUTTON_Y = 100;
        BUTTON_Z = 101;
        BUTTON_L1 = 102;
        BUTTON_R1 = 103;
        BUTTON_L2 = 104;
        BUTTON_R2 = 105;
        BUTTON_THUMBL = 106;
        BUTTON_THUMBR = 107;
        BUTTON_START = 108;
        BUTTON_SELECT = 109;
        BUTTON_MODE = 110;
        COLON = 243;
        F1 = 244;
        F2 = 245;
        F3 = 246;
        F4 = 247;
        F5 = 248;
        F6 = 249;
        F7 = 250;
        F8 = 251;
        F9 = 252;
        F10 = 253;
        F11 = 254;
        F12 = 255;
    }

    private int action_;
    private int pointerIndex_;
    private int pointerId_;

//    private View view_;
    public float touchX[] = new float[5];              // координаты указателя (до 5 одновременно)
    public float touchY[] = new float[5];              //
    public boolean touched[] = new boolean[5];          // и флаг нажатости
    public boolean buttoned[] = new boolean[3];         // три мышиные кнопки

    // для акселерометра
    protected float accelX;
    protected float accelY;
    protected float accelZ;

    public boolean catchBackKey;
    public boolean catchMenuKey;

    // сюда классы потомки должны записывать коды нажатых клавиш
    public HashSet<Integer> keys = new HashSet<Integer>();

    // коды последней нажатой и последней отжатой клавиши. при считывании стираются
    public int lastKeyUp, lastKeyDown;

    /**
     * Флаг, показывающий что с момента обнуления его произошло событие касания
     * нулевого указателя (единственного/последнего пальца или левой кнопки мыши). Требует ручного обнуления
     */
    public boolean was_touched;

    /** Флаг, показывающий что с момента обнуления его произошло событие отпускания
     * нулевого указателя (единственного/последнего пальца или левой кнопки мыши). Требует ручного обнуления */
    public boolean was_untouched;

    // кнопки мыши
    public class Buttons {
        public static final int LEFT = 0;
        public static final int RIGHT = 1;
        public static final int MIDDLE = 2;
    }

    public Input(Context context, View view) {
        view.setOnTouchListener(this);
//        view.setOnKeyListener(this);

        SensorManager manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (manager.getSensorList(Sensor.TYPE_ACCELEROMETER).size() != 0) {
            Sensor accelerometer = manager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
            manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        }
    }


    /** @return The value of the accelerometer on its x-axis. ranges between [-10,10]. */
    public float getAccX() {
        return accelX;
    }


    /** @return The value of the accelerometer on its y-axis. ranges between [-10,10]. */
    public float getAccY() {
        return accelY;
    }


    /** @return The value of the accelerometer on its z-axis. ranges between [-10,10]. */
    public float getAccZ() {
        return accelZ;
    }


    /** Returns the x coordinate in screen coordinates of the given pointer. Pointers are indexed from 0 to n. The pointer id
     * identifies the order in which the fingers went down on the screen, e.g. 0 is the first finger, 1 is the second and so on.
     * When two fingers are touched down and the first one is lifted the second one keeps its index. If another finger is placed on
     * the touch screen the first free index will be used.
     *
     * @param pointer the pointer id.
     * @return the x coordinate */
    public float getX(int pointer) {
        if (pointer>=5) return 0;
        return touchX[pointer];
    }

    /** Returns the y coordinate in screen coordinates of the given pointer. Pointers are indexed from 0 to n. The pointer id
     * identifies the order in which the fingers went down on the screen, e.g. 0 is the first finger, 1 is the second and so on.
     * When two fingers are touched down and the first one is lifted the second one keeps its index. If another finger is placed on
     * the touch screen the first free index will be used.
     *
     * @param pointer the pointer id.
     * @return the y coordinate */
    public float getY(int pointer) {
        if (pointer>=5) return 0;
        return touchY[pointer];
    }


    /** Whether the screen is currently touched by the pointer with the given index. Pointers are indexed from 0 to n. The pointer
     * id identifies the order in which the fingers went down on the screen, e.g. 0 is the first finger, 1 is the second and so on.
     * When two fingers are touched down and the first one is lifted the second one keeps its index. If another finger is placed on
     * the touch screen the first free index will be used.
     *
     * @param pointer the pointer
     * @return whether the screen is touched by the pointer */
    public boolean isTouched(int pointer) {
        if (pointer>=5)
            return false;
        else
            return touched[pointer];
    }


    /** Whether a given button is pressed or not. Button constants can be found in {@link Buttons}. On Android only the Button#LEFT
     * constant is meaningful.
     * @param button the button to check.
     * @return whether the button is down or not. */
    public boolean isButtonPressed(int button) {
        if (button == Buttons.LEFT)
            return isTouched(0);
        else
            return false;
    }

//
//    @Override
//    public boolean onKey(View v, int keyCode, KeyEvent e) {
//        synchronized (this) {
////            char character = (char)e.getUnicodeChar();
////            // Android doesn't report a unicode char for back space. hrm...
////            if (keyCode == 67) character = '\b';
//
//            KeyEvent event = null;
//            switch (e.getAction()) {
//                case android.view.KeyEvent.ACTION_DOWN:
////                    event = usedKeyEvents.obtain();
////                    event.keyChar = 0;
////                    event.keyCode = e.getKeyCode();
////                    event.type = KeyEvent.KEY_DOWN;
////
//                    // Xperia hack for circle key. gah...
//                    if (keyCode == android.view.KeyEvent.KEYCODE_BACK && e.isAltPressed()) {
//                        keyCode = Keys.BUTTON_CIRCLE;
////                        event.keyCode = keyCode;
//                    }
////
////                    keyEvents.add(event);
//                    keys.add(keyCode);
//                    break;
//                case android.view.KeyEvent.ACTION_UP:
////                    event = usedKeyEvents.obtain();
////                    event.keyChar = 0;
////                    event.keyCode = e.getKeyCode();
////                    event.type = KeyEvent.KEY_UP;
//
//                    // Xperia hack for circle key. gah...
//                    if (keyCode == android.view.KeyEvent.KEYCODE_BACK && e.isAltPressed()) {
//                        keyCode = Keys.BUTTON_CIRCLE;
////                        event.keyCode = keyCode;
//                    }
//
////                    keyEvents.add(event);
////
////                    event = usedKeyEvents.obtain();
////                    event.keyChar = character;
////                    event.keyCode = 0;
////                    event.type = KeyEvent.KEY_TYPED;
////                    keyEvents.add(event);
//
////                    if (keyCode == Keys.BUTTON_CIRCLE)
////                        keys.remove(Keys.BUTTON_CIRCLE);
////                    else
//                        keys.remove(keyCode);
//            }
////            app.getGraphics().requestRendering();
//        }
//
//        // circle button on Xperia Play shouldn't need catchBack == true
//        if (keyCode == Keys.BUTTON_CIRCLE) return true;
//        if (catchBack && keyCode == android.view.KeyEvent.KEYCODE_BACK) return true;
//        if (catchMenu && keyCode == android.view.KeyEvent.KEYCODE_MENU) return true;
//
//        return false;
//    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        synchronized (this){
            action_ = event.getAction() & MotionEvent.ACTION_MASK;
            pointerIndex_ = (event.getAction() & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT;
            pointerId_ = event.getPointerId(pointerIndex_);

            float x = (event.getX(pointerIndex_) - Graphics.XOFFSET)/Graphics.SCALE;
            float y = (event.getY(pointerIndex_) - Graphics.YOFFSET)/Graphics.SCALE;

//            System.out.println("x:"+event.getX(pointerIndex_)+", y:"+event.getY(pointerIndex_)+", XOFF:" + Graphics.XOFFSET+", YOFF:"+Graphics.YOFFSET);

            switch (action_){
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                    touchX[pointerId_] = x;
                    touchY[pointerId_] = y;
                    touched[pointerId_] = true;
                    if (pointerId_ == 0)
                        was_touched = true;
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                case MotionEvent.ACTION_CANCEL:
                    touchX[pointerId_] = x;
                    touchY[pointerId_] = y;
                    touched[pointerId_] = false;
                    if (pointerId_ == 0)
                        was_untouched = true;
                    break;
                case MotionEvent.ACTION_MOVE:
                    // из-за глюков переполучим pointerId
                    int nPointer = event.getPointerCount();
                    if (nPointer > 20)
                        nPointer = 20;

                    for (int i=0; i<nPointer; i++ ){
                        pointerIndex_ = i;
                        pointerId_ = event.getPointerId(pointerIndex_);

                        touchX[pointerId_] = (event.getX(pointerIndex_) - Graphics.XOFFSET)/Graphics.SCALE;
                        touchY[pointerId_] = (event.getY(pointerIndex_) - Graphics.YOFFSET)/Graphics.SCALE;
                    }
                    break;
            }
            return true;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        accelX = event.values[0];
        accelY = event.values[1];
        accelZ = event.values[2];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // nothing to do here
    }

//    /** @return whether a new touch down event just occured. */
//    public boolean justTouched ();



    /** Returns whether the key is pressed.
     *
     * @param key The key code as found in {@link Input}.
     * @return true or false. */
    public boolean isKeyPressed(int key) {
        synchronized (this) {
            if (key == Input.ANY_KEY)
                return keys.size() > 0;
            else
                return keys.contains(key);
        }
    }

    /**
     * Возвращает код последней нажатой клавиши и стирает его из буфера, то есть повторный вызов метода
     * вернет уже 0 (если не было других нажатий)
     * @return код последней нажатой клавиши
     */
    public int getLastKeyUp(){
        int k = lastKeyUp;
        lastKeyUp = 0;
        return k;
    }



    /**
     * Возвращает код последней отжатой клавиши и стирает его из буфера, то есть повторный вызов метода
     * вернет уже 0 (если не было других отжатий)
     * @return код последней отжатой клавиши
     */
    public int getLastKeyDown(){
        int k = lastKeyDown;
        lastKeyDown = 0;
        return k;
    }


    // методы, при вызове которых происходит сброс флагов о произошедших тачах/антачах
    public boolean wasTouched(){
        boolean b = was_touched;
        was_touched = false;
        return b;
    }


    public boolean wasUnTouched(){
        boolean b = was_untouched;
        was_untouched = false;
        return b;
    }


    public static int ANY_KEY;
    public static int NUM_0;
    public static int NUM_1;
    public static int NUM_2;
    public static int NUM_3;
    public static int NUM_4;
    public static int NUM_5;
    public static int NUM_6;
    public static int NUM_7;
    public static int NUM_8;
    public static int NUM_9;
    public static int A;
    public static int ALT_LEFT;
    public static int ALT_RIGHT;
    public static int APOSTROPHE;
    public static int AT;
    public static int B;
    public static int BACK;
    public static int BACKSLASH;
    public static int C;
    public static int CALL;
    public static int CAMERA;
    public static int CLEAR;
    public static int COMMA;
    public static int D;
    public static int DEL;
    public static int BACKSPACE;
    public static int FORWARD_DEL;
    public static int DPAD_CENTER;
    public static int DPAD_DOWN;
    public static int DPAD_LEFT;
    public static int DPAD_RIGHT;
    public static int DPAD_UP;
    public static int CENTER;
    public static int DOWN;
    public static int LEFT;
    public static int RIGHT;
    public static int UP;
    public static int E;
    public static int ENDCALL;
    public static int ENTER;
    public static int ENVELOPE;
    public static int EQUALS;
    public static int EXPLORER;
    public static int F;
    public static int FOCUS;
    public static int G;
    public static int GRAVE;
    public static int H;
    public static int HEADSETHOOK;
    public static int HOME;
    public static int I;
    public static int J;
    public static int K;
    public static int L;
    public static int LEFT_BRACKET;
    public static int M;
    public static int MEDIA_FAST_FORWARD;
    public static int MEDIA_NEXT;
    public static int MEDIA_PLAY_PAUSE;
    public static int MEDIA_PREVIOUS;
    public static int MEDIA_REWIND;
    public static int MEDIA_STOP;
    public static int MENU;
    public static int MINUS;
    public static int MUTE;
    public static int N;
    public static int NOTIFICATION;
    public static int NUM;
    public static int O;
    public static int P;
    public static int PERIOD;
    public static int PLUS;
    public static int POUND;
    public static int POWER;
    public static int Q;
    public static int R;
    public static int RIGHT_BRACKET;
    public static int S;
    public static int SEARCH;
    public static int SEMICOLON;
    public static int SHIFT_LEFT;
    public static int SHIFT_RIGHT;
    public static int SLASH;
    public static int SOFT_LEFT;
    public static int SOFT_RIGHT;
    public static int SPACE;
    public static int STAR;
    public static int SYM;
    public static int T;
    public static int TAB;
    public static int U;
    public static int UNKNOWN;
    public static int V;
    public static int VOLUME_DOWN;
    public static int VOLUME_UP;
    public static int W;
    public static int X;
    public static int Y;
    public static int Z;
    public static int META_ALT_LEFT_ON;
    public static int META_ALT_ON;
    public static int META_ALT_RIGHT_ON;
    public static int META_SHIFT_LEFT_ON;
    public static int META_SHIFT_ON;
    public static int META_SHIFT_RIGHT_ON;
    public static int META_SYM_ON;
    public static int CONTROL_LEFT;
    public static int CONTROL_RIGHT;
    public static int ESCAPE;
    public static int END;
    public static int INSERT;
    public static int PAGE_UP;
    public static int PAGE_DOWN;
    public static int PICTSYMBOLS;
    public static int SWITCH_CHARSET;
    public static int BUTTON_CIRCLE;
    public static int BUTTON_A;
    public static int BUTTON_B;
    public static int BUTTON_C;
    public static int BUTTON_X;
    public static int BUTTON_Y;
    public static int BUTTON_Z;
    public static int BUTTON_L1;
    public static int BUTTON_R1;
    public static int BUTTON_L2;
    public static int BUTTON_R2;
    public static int BUTTON_THUMBL;
    public static int BUTTON_THUMBR;
    public static int BUTTON_START;
    public static int BUTTON_SELECT;
    public static int BUTTON_MODE;

// public static final int BACKTICK = 0;
// public static final int TILDE = 0;
// public static final int UNDERSCORE = 0;
// public static final int DOT = 0;
// public static final int BREAK = 0;
// public static final int PIPE = 0;
// public static final int EXCLAMATION = 0;
// public static final int QUESTIONMARK = 0;

    // ` | VK_BACKTICK
// ~ | VK_TILDE
// : | VK_COLON
// _ | VK_UNDERSCORE
// . | VK_DOT
// (break) | VK_BREAK
// | | VK_PIPE
// ! | VK_EXCLAMATION
// ? | VK_QUESTION
    public static int COLON;
    public static int F1;
    public static int F2;
    public static int F3;
    public static int F4;
    public static int F5;
    public static int F6;
    public static int F7;
    public static int F8;
    public static int F9;
    public static int F10;
    public static int F11;
    public static int F12;
}
