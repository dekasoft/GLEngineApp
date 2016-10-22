package com.dekagames.gle;

/**
 * Created with IntelliJ IDEA.
 * User: Deka
 * Date: 29.09.13
 * Time: 23:59
 * To change this template use File | Settings | File Templates.
 */
public class MathUtils {
    public static final float PI = 3.1415927f;
    public static final float degreesToRadians = PI / 180;
    public static final float radiansToDefrees = 180 / PI;

    private static final float[] cosTable = new float[360];
    private static final float[] sinTable = new float[360];

    // init SinCos tables
    static {
        for (int i = 0; i<360; i++){
            cosTable[i] = (float)Math.cos(i * degreesToRadians);
            sinTable[i] = (float)Math.sin(i * degreesToRadians);
        }
    }



    public  static float cos(float degAngle){
        int a = Math.round(degAngle);
        if (a > 359) a=a%360;
        while(a<0) a+=360;
        return cosTable[a];
    }

    public  static float sin(float degAngle){
        int a = Math.round(degAngle);
        if (a > 359) a=a%360;
        while(a<0) a+=360;
        return sinTable[a];
    }

    // поворот на угол относительно точки x0, y0 угол в градусах быстрая библиотека - проблемы с
    // с округлением при малых углах!!!!!
    public static Point rotatePointDeg(float x0, float y0, float x1, float y1, float angle){
        float sina = sin(angle);
        float cosa = cos(angle);

        float x2 = (x1-x0)*cosa - (y1-y0)*sina + x0;
        float y2 = (y1-y0)*cosa + (x1-x0)*sina + y0;
        return new Point(x2, y2);
    }
}


