package com.dekagames.glengineapp;

import com.dekagames.gle.*;
import com.dekagames.gle.gui.controls.Button;

import java.util.Random;

public class MainScreen extends Screen {
    // текстура с одиночной картинкой
    private Texture texLogo;

    // текстура с полоской кадров - 1 способ
    private Texture texAngelStrip;

    //атлас спрайтов
    public Atlas atlas;

//    // текстура с атласом - 2 способ
//    public Texture textureAtlas;

    // Текстура для модели сделанной в скелетном редакторе / skeletal model
    private Texture textureModel;

//    // Скелетная модель
//    private SkeletonModel model;

    // Текстура для примера шейдера
    private Texture texReed;
    private Shader shader;

    // разные спрайты для разных способов загрузки
    private Sprite  sprAngelStrip, spriteFromAtlas, spriteLogo, sprReed;




    // GUI window - needed for gui elements
    private MainWindow mainWindow;
    // GUI element - simple button
    private Button button;



    private Font font;
    public  Sound sound;
    private Music music;

    private Random rand;

    private float fangle;
//    private boolean nowTouched;

    private int lastKeyUp;

    private float time;


    public MainScreen(GLE gle){
        super(gle);
        rand = new Random(System.currentTimeMillis());

        // create window for controls and add it to screen
        mainWindow = new MainWindow(gle, this, gle.getVirtualWidth(), gle.getVirtualHeight());
        addWindow(mainWindow);
    }

    @Override
    public boolean initialize(){
        // вся работа с input должна делаться в потоке GL
//        gle.input.catchBackKey = true;

        // текстура с одиночной картинкой
        texLogo = gle.graphics.loadTexture("logo.png");
        spriteLogo = new Sprite(texLogo, 1, 1, 0, 0, 256, 128);
        spriteLogo.fadeInOut(0.5f, 2.5f, 0.5f, 0);

        // ----------  1 способ - загрузка текстуры с полоской кадров -----------------------
        texAngelStrip = gle.graphics.loadTexture("angel.png");
        // создание спрайта из полоски кадров
        sprAngelStrip = new Sprite(texAngelStrip,16,10,0,0,64,64);
        sprAngelStrip.play(true);       // запуск анимации


        // ----------  2 способ - загрузка текстуры с атласом кадров -----------------------
        atlas = gle.graphics.loadAtlas("atlas.png", "atlas.png.json");
        spriteFromAtlas = atlas.getSprite("sprMan", 12);
        spriteFromAtlas.play(true);



//        // ----------  2 способ - загрузка не спрайта, а скелетной модели -----------------------
//        textureModel = dgl.loadTexture("hero.model.png");
//        // скелетная модель содержит мини атлас всех своих костей, который, обычно, склеивается с  общим
//        // атласом в качестве кадра спрайта, поэтому для загрузки модель использует не текстуру
//        // а только область текстуры. В нашем тестовом примере мини-атлас модели хранится сам по себе, а не
//        // в общем атласе, поэтому просто создадим TextureRegion из всей текстуры
//        TextureRegion region = new TextureRegion(textureModel,0,0,textureModel.width, textureModel.height);
//        model = new SkeletonModel(dgl, region, "hero.model", 10);
//        model.setFlip(true, false);     // отражение модели - просто для примера
//        model.play(true);               // запуск анимации
//

        // проверка шейдера
        texReed = gle.graphics.loadTexture("reed.png");
        sprReed = new Sprite(texReed, 1,1,0,0,128,128);
        shader = gle.graphics.loadShader("vertex.glsl", "fragment.glsl");

        // ------------------------  загрузка шрифта ----------------------------------------------------
        font = atlas.getFont("fntComic");

        // --------------------------- create controls by call initControls method from GL thread --------
        mainWindow.initControls();

        // ------------------------ загрузка звуков и музыки ------------------------------------
        sound = gle.audio.newSound("bird.wav", true);
        music = gle.audio.newMusic("polka.ogg", true);

        music.play();

//
//        rand = new Random(System.currentTimeMillis());
        return super.initialize();
    }

    @Override
    public void update(float deltaTime) {
        super.updateGUI(deltaTime);

        lastKeyUp = gle.input.getLastKeyUp();
        if (lastKeyUp == Input.BACK)
            System.out.println("BAck button up!");

        spriteLogo.update(deltaTime);
        sprAngelStrip.update(deltaTime);
        spriteFromAtlas.update(deltaTime);
//
//        model.update(deltaTime);
//        fangle += deltaTime*10;         // чтобы модель крутилась
//
        time += deltaTime;
        shader.setUniform("u_time", time);
    }

    @Override
    public void draw(Graphics gr) {
        gr.clearScreen(0,0,0);               // очищает весь экран (полосы по бокам)
        gr.clearViewport(0.1f, 0.7f, 0.1f);  // очищаем саму область вывода

        // весь вывод графики должен предворяться begin
        gr.begin();

        // появляющееся и исчезающее лого
        spriteLogo.draw(gr, 300, 300);

        //выводим спрайт созданный из полоски
        sprAngelStrip.setARGBColor(0xFEFDFCFB);
        sprAngelStrip.draw(gr, 100, 100);

        // выводим спрайт загруженный из атласа
        spriteFromAtlas.draw(gr, 200,100);

//        spriteFromAtlas.draw(gr, 300,100);
//
////        for (int i = 0; i < 5; i++) {
////            spriteFromAtlas.draw(gr,rand.nextInt(game.getVirtualWidth()), rand.nextInt(game.getVirtualHeight()));
////        }
//
//
//        // рисуем скелетную модель
//        model.draw(gr, 400, 200, fangle);

        //проверка шейдера
        shader.setUniform("scr_h", (float) gr.getViewportHeight());
        shader.setUniform("sprxy", new Point(600, 200));
        shader.setUniform("sizexy", new Point(128,128));

        shader.setUniform("yoffset", (float)Graphics.YOFFSET);
        shader.setUniform("xoffset", (float)Graphics.XOFFSET);
        shader.setUniform("scale", Graphics.SCALE);
        gr.useShader(shader);
        sprReed.draw(gr,600,200);
        gr.useShader(null);
        sprReed.draw(gr,600,100);

        // выведем FPS загруженным шрифтом
        font.setColor(1,0,0,1);
        font.drawString(gr,"Hello world! FPS: " + gle.getFPS(), 200,100);
//
//        // draw primitives
//        Primitives.drawPoint(gr, 1, 1);
//        for (int i=0; i<90; i+=10){
//            Primitives.drawLine(gr, 10,10, 50*(float)Math.cos(Math.toRadians(i)), 50*(float)Math.sin(Math.toRadians(i)));
//        }
//        Primitives.drawCircle(gr, 50,50, 30);
//
//        float[] vert = new float[10];
//        vert[0] = 10; vert[1] = 200;
//        vert[2] = 50; vert[3] = 200;
//        vert[4] = 70; vert[5] = 280;
//        vert[6] = 40; vert[7] = 360;
//        vert[8] = 0; vert[9] = 250;
//        Primitives.drawPolygon(gr, vert);
//
        // обработка ввода
        if (gle.input.was_touched){
            sound.play();
            gle.input.was_touched = false;
        }


//        if (game.input.touched[0] && !nowTouched){ // произошла касание
//            nowTouched = true;
//            sound.play();
//        }
//
//        if (!game.input.touched[0]){
//            nowTouched = false;
//        }


        for (int i=0; i<3; i++){
            if (gle.input.touched[i]) {
                float x = gle.input.touchX[i];
                float y = gle.input.touchY[i];
                font.drawString(gle.graphics,"Pointer "+i, x, y );
            }
        }

        super.drawGUI(gr);

        // вывод графики должен заканчиваться end
        gr.end();
    }


}
