package com.dekagames.gle;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.view.Window;
import android.view.WindowManager;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.util.ArrayList;

public class GLE implements GLSurfaceView.Renderer{

    GLEActivity     activity;
    GLSurfaceView   glView;
    Object          stateMonitor = new Object();
    long            startTime = System.nanoTime();
    boolean         isPaused;

    public      FileIO          fileIO;
    public      Graphics        graphics;
    public      Audio           audio;
    public      Input           input;

    boolean needToReload;

    private int     virtualWidth, virtualHeight;
    private long    nFramesRendered;        // для расчета FPS
    private float   fTimeFromStart;
    private float   fTimeFPS;
    private float   fFPS, nframes;

    ArrayList<Texture> managedTextures;
    ArrayList<Shader>  managedShaders;


    Screen          screen;


    public GLE(int  virtual_width, int virtual_height){
        virtualWidth = virtual_width;
        virtualHeight = virtual_height;

        managedTextures = new ArrayList<>();
        managedShaders = new ArrayList<>();
    }


    public int getVirtualWidth(){
        return virtualWidth;
    }


    public int getVirtualHeight(){
        return virtualHeight;
    }


    // этот метод вызывается классом Application (или его наследником) из контекста OpenGL
    public void initialize(){
        Log.info("GLE initialize called");
        if (screen != null)
            // необходимо инициализировать экран - возможно ему требуется OpenGL для инициализации
            if (!screen.isInitialized) screen.initialize();
    }


    /**
     * Метод, вызываемый приложением каждую итерацию игрового цикла.
     * В нем вызывается соответствующий метод класса {@link com.dekagames.gle.Screen}, и производится
     * расчет FPS.
     * @param deltaTime время в секундах с момента прошлого вызова
     */
    public final void update(float deltaTime){
        if (screen != null) {
            if (!screen.isInitialized) screen.initialize();
            screen.update(deltaTime);
            fTimeFromStart += deltaTime;

            // расчет мгновенного FPS (каждую секунду)
            nframes++;
            fTimeFPS += deltaTime;
            if (fTimeFPS>=1.0f){
                fFPS = nframes;
                nframes = 0;
                fTimeFPS = 0;
            }
        }
    }


    /**
     * Метод, вызываемый приложением каждую итерацию игрового цикла.
     * В нем вызывается соответствующий метод класса {@link com.dekagames.gle.Screen}.
     */
    public final void draw(){
        if (screen != null) {
            if (!screen.isInitialized) screen.initialize();
            screen.draw(graphics);
            // запишем данные для расчета FPS
            nFramesRendered++;
        }
    }


    public void pause(){
        Log.info("GLE pause method called");
        isPaused = true;
        needToReload = true;
        glView.onPause();
        if (screen != null) screen.pause();
        if (graphics != null) graphics.pause();
        if (audio != null)  audio.pause();
    }


    public void resume(){//int width, int height) {
        Log.info("GLE resume method called");
        isPaused = false;
        glView.onResume();

        if (screen != null) screen.resume();
        if (audio != null)  audio.resume();
    }


    public void setScreen(Screen scr){
        if (scr != null) {
            if (screen != null) {
                screen.pause();
                screen.dispose();
            }
            scr.resume();
            screen = scr;
        }
    }


    public Screen getScreen(){
        return screen;
    }


    /**
     * Устанавливает флаг необходимости перезагрузки текстур и шейдеров.
     * Так как работа перезагрузка текстур и шейдеров должна вестись в потоке OpenGL, то не всегда
     * возможно вызвать метод перезагрузки {@link GLE#reloadTextures() reloadTextures} сразу. Вместо
     * этого можно воспользоваться этим методом, установив соответствующий флаг, и движок, сам из потока
     * OpenGL вызовет перезагрузку текстур.
     *
     * @param needReload флаг необходимости перезагрузки.
     */
    public void setNeedToReload(boolean needReload){
        needToReload = needReload;
    }


    /**
     * Перезагружает все ранее загруженные текстуры и шейдеры.
     * Метод используется самим движком для перезагрузки текстур и шейдеров в случае потери контекста.
     * Как правило, нет необходимости самостоятельно вызывать этот метод.
     */
    public void reloadTextures(){
        Log.info("Reload texture called. " + managedTextures.size()+" textures and " + managedShaders.size()+" shaders.");
        if (graphics == null){
            Log.error("Graphics was not created. Reloading of the textures is impossible!");
            return;
        }

        for (int i=0; i<managedTextures.size(); i++) {

            Texture tex = managedTextures.get(i);
            if (tex != null) {
                Log.info("Reloading texture: " + tex.textureId);
                graphics.reload_texture_from_file(tex);//.reload_texture_from_file();
            }
        }

        for (int i=0; i<managedShaders.size(); i++) {

            Shader sh = managedShaders.get(i);
            if (sh != null) {
                Log.info("Reloading shader: " + sh.getProgramId());
                sh.rebuild();
            }
        }

    }


    public float getFPS(){
        return fFPS;
    }


    public ArrayList<Texture> getTextures(){
        return managedTextures;
    }


    public ArrayList<Shader> getShaders(){
        return managedShaders;
    }


    public void start(GLEActivity activity, String title, boolean fullscreen) {
        if (activity == null) return;

        this.activity = activity;
        this.activity.gle = this;

        if (fullscreen) {
            activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        glView = new GLSurfaceView(activity);


        // Проверяем поддерживается ли OpenGL ES 2.0.
        final ActivityManager activityManager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;

        if (supportsEs2)   {
            // Запрос OpenGL ES 2.0 для установки контекста.
            glView.setEGLContextClientVersion(2);
            glView.setRenderer(this);
            activity.setContentView(glView);

            // модуль работы с файлами
            fileIO = new FileIO(activity.getAssets());

            // модуль работы со вводом (тачскрин и акселерометр)
            input = new Input(activity, glView);

            // модуль для работы со звуком
            audio = new Audio(activity);
//
//            PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
//            wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK,"Game");
            Log.info("GLE started successfully");
        }
        else  {
            Log.error("Your device does not support OpenGL ES 2.0!");
            throw(new RuntimeException("Unsupported device!"));
        }
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.info("GLE renderer onSurfaceCreated called");
        // запишем модуль для работы с графикой
        if (graphics == null)
            graphics = new Graphics(this);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.info("GLE renderer onSurfaceChanged called. W=" + width + "; H=" + height + ";");
        synchronized (stateMonitor) {
            initialize();
            if (graphics != null)
                graphics.init(width, height);
            startTime = System.nanoTime();
        }
    }


    @Override
    public void onDrawFrame(GL10 gl) {
        if (!isPaused){
            long now = System.nanoTime();
            float deltaTime = (now - startTime) / 1000000000.0f;
            startTime = now;

            update(deltaTime);
            draw();
        }
    }
}
