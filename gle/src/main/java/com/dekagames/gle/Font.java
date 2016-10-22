package com.dekagames.gle;

//import com.dekagames.slon.Slon;
//import com.dekagames.slon.SlonNode;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

// глиф буквы, как и спрайт состоит из двух треугольников вот с такой нумерацией
//1-----2(4)
//|    /|
//|   / |
//|  /  |
//| /   |
//|/    |
//3(6)--5
//

/**
 */
class Glyph {
    // текстурные координаты глифа (0..1)
    float 	s1,t1,      // левый верхний угол
            s2,t2,      // правый верхний угол
            s3,t3,      // левый нижний угол
            s4,t4,      // правый верхний угол
            s5,t5,      // правый нижний угол
            s6,t6;      // левый нижний угол
    // базовая точка
    float lsb, origy;
    // расстояние до горячей точки следующего глифа
    float   advance;
    int 	w,h;		// ширина и высота глифа
}

public class Font {
    /** Space width. It is calculated as average glyph width during loading. */
    public int spaceWidth;                              // ширина пробела. вычисляется при загрузке.


    public HashMap <Character, Glyph> glyphMap;         // карта символов
    private Texture texture_;
//    private Slon slon_;

    private String fontName_;
    private float scale_;

    // осмысленные имена индексов в массиве координат
    static final int	X1=0, 	Y1=1, 	R1=2,  G1=3,  B1=4,  A1=5, 	S1=6, 	T1=7,
                        X2=8, 	Y2=9, 	R2=10, G2=11, B2=12, A2=13,	S2=14, 	T2=15,
                        X3=16, 	Y3=17, 	R3=18, G3=19, B3=20, A3=21,	S3=22, 	T3=23,
                        X4=24, 	Y4=25, 	R4=26, G4=27, B4=28, A4=29,	S4=30, 	T4=31,
                        X5=32, 	Y5=33, 	R5=34, G5=35, B5=36, A5=37,	S5=38, 	T5=39,
                        X6=40, 	Y6=41, 	R6=42, G6=43, B6=44, A6=45,	S6=46, 	T6=47;

    private float[] 	vertices;                // массив для передачи данных глифа


    Font(Texture tex, JSONObject jsonFile, String name){
        vertices = new float[Graphics.QUAD_SIZE];
        texture_ = tex;
        fontName_ = name;
        glyphMap = new HashMap<Character, Glyph>();

        JSONObject fontNode;
        try {
            fontNode = jsonFile.getJSONObject("fonts").getJSONObject(name);
        } catch (JSONException e) {
            Log.error("Can not create font \"" + name + "\" from atlas. "+e.getMessage());
            return;
        }

        if (fontNode == null) {
            Log.error("Could not find font "+ fontName_ +"!");
            return;
        }

        glyphMap.clear();

        int texWidth = texture_.width;
        int texHeight = texture_.height;

        Iterator<String> charKeys = fontNode.keys();

//        int glyphsAmount = fontNode..getChildCount();
        spaceWidth = 0;                         // initial value

        while (charKeys.hasNext()){
            Glyph glyph = new Glyph();
            String key = charKeys.next();
            try {
                JSONObject glyphNode = fontNode.getJSONObject(key);

                // прочитаем характеристики символа
                Character character = key.charAt(0);
                // прочитаем координаты глифа на текстуре и другие характеристики
                int x = glyphNode.getInt("x");
                int y = glyphNode.getInt("y");
                int w = glyphNode.getInt("w");
                int h = glyphNode.getInt("h");
                float lsb = (float)glyphNode.getDouble("lsb");
                float adv = (float)glyphNode.getDouble("advance");
                float origY = (float)glyphNode.getDouble("originY");

                // скорректируем на половину пикселя, чтобы правильнее считались координаты
                float tx1 = (float)x;// + 0.5f;
                float tx2 = (float)x + (float)w -1;//+ 0.5f;
                float ty1 = (float)y;// + 0.5f;
                float ty2 = (float)y + (float)h -1;//+ 0.5f;

                // рассчитаем текстурные координаты
                glyph.s1 = tx1/(float)texWidth;			glyph.t1 = ty1/(float)texHeight;
                glyph.s2 = tx2/(float)texWidth;			glyph.t2 = ty1/(float)texHeight;
                glyph.s3 = tx1/(float)texWidth;			glyph.t3 = ty2/(float)texHeight;
                glyph.s4 = glyph.s2;			        glyph.t4 = glyph.t2;
                glyph.s5 = tx2/(float)texWidth;			glyph.t5 = ty2/(float)texHeight;
                glyph.s6 = glyph.s3;			        glyph.t6 = glyph.t3;

                // размеры кадра
                glyph.w = w;
                spaceWidth+=w;
                glyph.h = h;
                // горячая точка
                glyph.lsb = lsb;
                glyph.origy = origY;
                glyph.advance = adv;

                // добавим в карту глифов
                glyphMap.put(character,glyph);

            } catch (JSONException e) {
                Log.error("Can not find symbol \"" + key + "\" in font \""+ name+"\". "+ e.getMessage());
                continue;
            }
        }

//        for (int i=0; i<glyphsAmount; i++){
//            Glyph glyph = new Glyph();
//            SlonNode glyphNode = fontNode.getChildAt(i);
//            // прочитаем характеристики символа
//            Character character = glyphNode.getKeyValue("glyph").charAt(0);
//            // прочитаем координаты глифа на текстуре и другие характеристики
//            int x = glyphNode.getKeyAsInt("x");
//            int y = glyphNode.getKeyAsInt("y");
//            int w = glyphNode.getKeyAsInt("w");
//            int h = glyphNode.getKeyAsInt("h");
//            float lsb = glyphNode.getKeyAsFloat("lsb");
//            float adv = glyphNode.getKeyAsFloat("advance");
//            float origY = glyphNode.getKeyAsFloat("originY");
//
//            // скорректируем на половину пикселя, чтобы правильнее считались координаты
//            float tx1 = (float)x;// + 0.5f;
//            float tx2 = (float)x + (float)w -1;//+ 0.5f;
//            float ty1 = (float)y;// + 0.5f;
//            float ty2 = (float)y + (float)h -1;//+ 0.5f;
//
//            // рассчитаем текстурные координаты
//            glyph.s1 = tx1/(float)texWidth;			glyph.t1 = ty1/(float)texHeight;
//            glyph.s2 = tx2/(float)texWidth;			glyph.t2 = ty1/(float)texHeight;
//            glyph.s3 = tx1/(float)texWidth;			glyph.t3 = ty2/(float)texHeight;
//            glyph.s4 = glyph.s2;			        glyph.t4 = glyph.t2;
//            glyph.s5 = tx2/(float)texWidth;			glyph.t5 = ty2/(float)texHeight;
//            glyph.s6 = glyph.s3;			        glyph.t6 = glyph.t3;
//
//            // размеры кадра
//            glyph.w = w;
//            spaceWidth+=w;
//            glyph.h = h;
//            // горячая точка
//            glyph.lsb = lsb;
//            glyph.origy = origY;
//            glyph.advance = adv;
//
//            // добавим в карту глифов
//            glyphMap.put(character,glyph);
//        }
        spaceWidth = Math.round(0.8f*spaceWidth/fontNode.length());


        setColor(1,1,1,1);
        setScale(1.0f);

    }

    public void setScale (float scale){
        scale_ = scale;
    }

    // рисуем Glyph с координатами origin x,y
    private void draw_glyph(Graphics graphics, Glyph glyph, float x, float y){
        // экранные координаты
        float tx1 = glyph.lsb * scale_;
        float ty1 = -glyph.origy * scale_;
        float tx2 = (glyph.w + glyph.lsb)*scale_;
        float ty2 = (glyph.h - glyph.origy)*scale_;

        vertices[X1] = tx1 + x;
        vertices[Y1] = ty1 + y;
        vertices[X2] = tx2 + x;
        vertices[Y2] = ty1 + y;
        vertices[X3] = tx1 + x;
        vertices[Y3] = ty2 + y;

        vertices[X4] = vertices[X2];
        vertices[Y4] = vertices[Y2];
        vertices[X5] = tx2 + x;
        vertices[Y5] = ty2 + y;
        vertices[X6] = vertices[X3];
        vertices[Y6] = vertices[Y3];

        // текстурные координаты
        vertices[S1] = glyph.s1;			vertices[T1] = glyph.t1;
        vertices[S2] = glyph.s2;			vertices[T2] = glyph.t2;
        vertices[S3] = glyph.s3;			vertices[T3] = glyph.t3;

        vertices[S4] = glyph.s4;			vertices[T4] = glyph.t4;
        vertices[S5] = glyph.s5;			vertices[T5] = glyph.t5;
        vertices[S6] = glyph.s6;			vertices[T6] = glyph.t6;

        graphics.draw(texture_, vertices);
    }

    // рисуем строку с координатами x.y
    public void drawString(Graphics graphics, String text, float x, float y){
        float tempX=x;  // текущая координата origin

        for (int i=0; i<text.length(); i++){
            Character c = text.charAt(i);

            if (c.equals(' ')){
                tempX += spaceWidth * scale_;
                continue;
            } else {
                Glyph g = glyphMap.get(c);
                if (g != null) {
                    draw_glyph(graphics, g, tempX, y);//+g.origy);
                    tempX += scale_ * g.advance;
                } else {
                    tempX += spaceWidth * scale_;
                }

            }
        }
    }


    // рисуем строку, отцентрированную по горизонтали около точки с координатами x.y
    public void drawCaption(Graphics graphics, String text, float x, float y){
        float tempX=x;  // текущая координата origin
        float strWidth=0;


        // сначала вычислим длину строки
        for (int i=0; i<text.length(); i++){
            Character c = text.charAt(i);

            if (c.equals(' ')){
                strWidth += spaceWidth * scale_;
                continue;
            } else {
                Glyph g = glyphMap.get(c);
                if (g != null)
                    strWidth += scale_ * g.advance;
                else
                    strWidth += spaceWidth * scale_;
            }
        }

        // нарисуем строку
        drawString(graphics,text, x-strWidth/2, y);
    }


    public void setColor (float r, float g, float b, float a) {
        vertices[R1] = vertices[R2] = vertices[R3] = vertices[R4] = vertices[R5] = vertices[R6] = r;
        vertices[G1] = vertices[G2] = vertices[G3] = vertices[G4] = vertices[G5] = vertices[G6] = g;
        vertices[B1] = vertices[B2] = vertices[B3] = vertices[B4] = vertices[B5] = vertices[B6] = b;
        vertices[A1] = vertices[A2] = vertices[A3] = vertices[A4] = vertices[A5] = vertices[A6] = a;
    }

}
