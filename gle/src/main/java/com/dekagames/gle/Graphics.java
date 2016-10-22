package com.dekagames.gle;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import org.json.JSONException;
import org.json.JSONObject;

import javax.microedition.khronos.opengles.GL10;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.*;

public class Graphics {
    public static int MAX_SPRITES_DEFAULT = 2000;
    protected static final int VERTEX_SIZE = 2 + 1+1+1+1 + 2;			        // x,y,r,g,b,a,s,t
    protected static final int VERTICES_PER_QUAD = 6;                          // сколькими вершинами задается прямоугольная область
    protected static final int QUAD_SIZE = VERTICES_PER_QUAD * VERTEX_SIZE;    // сколько значений типа float требуется для задания одного QUAD

    protected static final int BYTES_PER_FLOAT = 4;

    // расположение данных в буфере вершин
    protected static final int POSITION_OFFSET = 0;   // смещение координат вершин в буфере вершин
    protected static final int POSITION_SIZE = 2;     // размер данных по координатам (2 - X и Y)

    protected static final int COLOR_OFFSET = 2;      // то же для данных о цвете
    protected static final int COLOR_SIZE = 4;        // (R,G,B,A)

    protected static final int TEX_COORD_OFFSET = 6;  // то же для текстурных координат
    protected static final int TEX_COORD_SIZE = 2;    //

    protected static final int STRIDE = VERTEX_SIZE * BYTES_PER_FLOAT;  // количество байт на вершину


    protected GLE gle;

    private     Shader shaderForSprites;          // стандартный шейдер для вывода текстурных объектов
    private     Shader shaderForPrimitives;       // standart shader for primitives

    protected   FloatBuffer vertexBuffer;       // буффер с прорисовываемыми вершинами

    protected   int spritesCount;               // сколько спрайтов будем рисовать
    protected   int vertexCount;                // how many vertexes to draw
    protected   int maxSpritesAmount;           // сколько максимум спрайтов рисуется за один проход
    public      int physicalWidth, physicalHeight;

    public static float SCALE;                  // коэффициент трансформации виртуального разрешения в реальное
    public static int XOFFSET, YOFFSET;         // смещение картинки относительно левого верхнего угла из-за разного соотношения сторон
    public static float BOTTOM_OFFSET;          // смещение низа картинки

    private int viewportWidth, viewportHeight;    //реальная ширина и высота viewport-a

//    protected ArrayList<Texture> managedTextures;
//    protected ArrayList<Shader>  managedShaders;


    private Texture currentTexture;
    private Shader currentShader;
    private boolean isFirstDrawCall;

    // матрицы
    protected float[] projectionMatrix = new float[16];     // матрица проекции
    protected float[] viewModelMatrix = new float[16];      // произведение матриц вида и модели
    protected float[] mpvMatrix = new float[16];            // произведение матриц вида, модели и проекции


    public Graphics(GLE gle){
        this.gle = gle;
        isFirstDrawCall = true;

        // подготовим массив вершин
        vertexBuffer = ByteBuffer.
                allocateDirect(QUAD_SIZE * BYTES_PER_FLOAT * MAX_SPRITES_DEFAULT).
                order(ByteOrder.nativeOrder()).
                asFloatBuffer();
    }


    public int getViewportWidth(){return viewportWidth;}


    public int getViewportHeight() {return viewportHeight;}


    public final void clearScreen(float r, float g, float b){
        glClearColor(r, g, b, 1);
        glClear(GL_COLOR_BUFFER_BIT);
    }


    public final void clearViewport(float r, float g, float b){
        glEnable(GL_SCISSOR_TEST);
        glScissor(XOFFSET, YOFFSET, viewportWidth, viewportHeight);
        glClearColor(r,g,b,1);
        glClear(GL_COLOR_BUFFER_BIT);
        glDisable(GL_SCISSOR_TEST);
    }


    public void init(int physical_width, int physical_height){
        Log.info("Graphics.init("+physical_width+", "+physical_height+") called");
        physicalWidth = physical_width;
        physicalHeight = physical_height;

        // определим как вытянут экран и коэффициент растяжения
        float virtRatio = (float) gle.getVirtualWidth()/(float) gle.getVirtualHeight();
        float realRatio = (float)physical_width/(float)physical_height;
        if (realRatio>virtRatio){ // экран вытянут по ширине - будут полоски слева и справа
            SCALE = (float)physical_height/(float) gle.getVirtualHeight();
            YOFFSET = 0;
            BOTTOM_OFFSET = 0;
            XOFFSET = Math.round(physical_width - SCALE* gle.getVirtualWidth())/2;
        }
        else {  // экран вытянут по высоте - будут полоски сверху и снизу
            SCALE = (float)physical_width/(float) gle.getVirtualWidth();
            XOFFSET = 0;
            YOFFSET = Math.round(physical_height - SCALE* gle.getVirtualHeight())/2;
            BOTTOM_OFFSET = YOFFSET;
        }

        viewportWidth = Math.round(SCALE* gle.getVirtualWidth());
        viewportHeight= Math.round(SCALE* gle.getVirtualHeight());

        glDisable(GL_DEPTH_TEST);
        glDisable(GL10.GL_LIGHTING);
        glDisable(GL_SCISSOR_TEST);

        // для прозрачности разные установки для платформ хз почему
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA); // !!!!!! // for desktop

        // Устанавливаем viewport  в соответствии с виртуальным экраном.
        glViewport(XOFFSET, YOFFSET, viewportWidth, viewportHeight);

        // получим произведение матриц вида и модели. они используются совместно всегда
        init_view_model_matrix();

        // Создаем новую матрицу проекции. Ортогональную.
        final float left = 0;
        final float right = gle.getVirtualWidth();
        final float bottom = gle.getVirtualHeight();   // так как стоим ровно, то верх и низ просто меняем местами - чтобы
        final float top = 0;                            // координата Y на экране была направлена вниз
        final float near = 2.0f;
        final float far = -2.0f;
        Matrix.orthoM(projectionMatrix, 0, left, right, bottom, top, near, far);

        // Перемножаем матрицу модели*вида на матрицу проекции, сохраняем в MVP матрицу.
        // (которая теперь содержит модель*вид*проекцию).
        Matrix.multiplyMM(mpvMatrix, 0, projectionMatrix, 0, viewModelMatrix, 0);

        // создаем два дефолтных шейдера - один для спрайтов, другой для простых примитивов
        // они не хранятся в managedShaders, поэтому создаем их напрямую, а не через createShader
        shaderForSprites = new Shader(Shader.DEFAULT_VERTEX_SHADER, Shader.DEFAULT_FRAGMENT_SHADER);
        shaderForPrimitives = new Shader(Shader.PRIMITIVE_VERTEX_SHADER, Shader.PRIMITIVE_FRAGMENT_SHADER);

        // send MPV matrix to both shaders
        // before we send uniform to shader we MUST enable shader by glUseProgram command!!!
        glUseProgram(shaderForSprites.getProgramId());
        glUniformMatrix4fv(shaderForSprites.mpvMatrixUniformLocation, 1, false, mpvMatrix, 0);
        glUseProgram(shaderForPrimitives.getProgramId());
        glUniformMatrix4fv(shaderForPrimitives.mpvMatrixUniformLocation, 1, false, mpvMatrix, 0);

        // перезагрузим текстуры
        // восстановим текстуры при создании поверхности - это происходит в самом начале, при запуске игрыы
        // и при паузе-возобновлении
        if (gle != null) {
            if (gle.needToReload) {
                gle.reloadTextures();
                gle.needToReload = false;
                System.out.println("Dgl reload textures and set flag to false");

            }
        }
    }


    public final void begin(){
        maxSpritesAmount = MAX_SPRITES_DEFAULT;
        vertexBuffer.position(0);
    }


    public final void end(){
        // we always send ALL data to ANY shader. e.g. we send texture data to primitives shader too.
        // texture's uniforms in primitive's shader are NOT exist, thus its location variables is -1.
        if (currentShader == null){
            if (currentTexture == null)
                currentShader = shaderForPrimitives;
            else
                currentShader = shaderForSprites;
        }

        glUseProgram(currentShader.getProgramId());

        vertexBuffer.rewind();
        // значения позиции и цвета передаются в любой шейдер

        // Передаем значения о расположении.
        vertexBuffer.position(POSITION_OFFSET);
        glVertexAttribPointer(currentShader.positionAttrLocation, POSITION_SIZE, GL_FLOAT, false, STRIDE, vertexBuffer);
        glEnableVertexAttribArray(currentShader.positionAttrLocation);

        // Передаем значения о цвете.
        vertexBuffer.position(COLOR_OFFSET);
        glVertexAttribPointer(currentShader.colorAttrLocation, COLOR_SIZE, GL_FLOAT, false, STRIDE, vertexBuffer);
        glEnableVertexAttribArray(currentShader.colorAttrLocation);

        if (currentTexture != null) { //shader.getKind() != Shader.DEFAULT_FOR_PRIMITIVES) {
            // Передаем значения о текстурных координатах.
            vertexBuffer.position(TEX_COORD_OFFSET);
            glVertexAttribPointer(currentShader.texCoordAttrLocation, TEX_COORD_SIZE, GL_FLOAT, false, STRIDE, vertexBuffer);
            glEnableVertexAttribArray(currentShader.texCoordAttrLocation);
        }

        // непосредственно рисуем все что накопилось в буфере
        glDrawArrays(GL_TRIANGLES, 0, vertexCount);//VERTICES_PER_QUAD * spritesCount);
        glFlush();   // нехило ускоряется процесс из-за этой команды
        // обнулим счетчик спрайтов - мы их уже нарисовали
        spritesCount = 0;
        vertexCount = 0;
        if (currentTexture != null){
            glBindTexture(GL_TEXTURE_2D, 0);
        }
        currentTexture = null;
        isFirstDrawCall = true;
    }


    /**
     * Устанавливаем текущий шейдер для отрисовки. Если shader равен null, значит используется
     * шейдер по умолчанию.
     * @param shader - шейдер, который необходимо использовать при последующей прорисовке или null
     *               если требуется использовать встроенный в движок шейдер по умолчанию.
     */
    public void useShader(Shader shader){
        if (shader == currentShader)
            return;

        if (!isFirstDrawCall) {
            end();
            begin();
        }

        // если шейдер свой - сразу загрузим ему mpv матрицу - она неизменна
        // в шейдеры по умолчанию она уже загружена при инициализации графики
        if (shader != null){
            glUseProgram(shader.getProgramId());
            glUniformMatrix4fv(shader.mpvMatrixUniformLocation, 1, false, mpvMatrix, 0);
        }
        currentShader = shader;
    }


    // устанавливает произведение матриц вида и модели
    private void init_view_model_matrix(){
        float[] viewMatrix = new float[16];
        float[] modelMatrix = new float[16];

        // Устанавливаем матрицу ВИДА. Она описывает положение камеры.
        // Примечание: В OpenGL 1, матрица ModelView используется как комбинация матрицы МОДЕЛИ
        // и матрицы ВИДА. В OpenGL 2, мы можем работать с этими матрицами отдельно по выбору.
        // Положение глаза, точки наблюдения в пространстве.
        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = 1.0f;

        // направление взгляда - смотрим против оси Z (ось Z направлена к нам)
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = -1.0f;

        // голова смотрит по оси Y (стоим вертикально - ось Y направлена вверх).
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

        Matrix.setLookAtM(viewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);
        //устанавливаем матрицу модели - мы ее не используем, поэтому просто единичная
        Matrix.setIdentityM(modelMatrix, 0);
        viewModelMatrix = new float[16];
        Matrix.multiplyMM(viewModelMatrix, 0, viewMatrix, 0, modelMatrix, 0);
    }


    void draw(Texture texture, float[] vertices) {
        // maximum vertices[] size is 48 - 6 vertices of 8 floats!!!! If we draw primitive, it is not
        // allowed to send more then 6 vertices by method call.

        // if texture changing detected - we'll finish previous draw and send new texture to shader
        if (currentTexture != texture) {
            if (!isFirstDrawCall) {
                end();
                begin();
            }
            currentTexture = texture;
            // Передаем текстуру в шейдер
            if (currentTexture != null) {
                glActiveTexture(GL_TEXTURE0);                           // установим активный текстурный блок
                glBindTexture(GL_TEXTURE_2D, texture.textureId);        // установим активную текстуру
                if (currentShader == null)                                          // шейдер по умолчанию
                    glUniform1i(shaderForSprites.textureUniformLocation, 0);        // свяжем адрес униформы текстуры с номером акивного блока
                else                                                                // пользовательский шейдер
                    glUniform1i(currentShader.textureUniformLocation, 0);
            }
        }
        isFirstDrawCall = false;


        // добавим вершины в буфер
        vertexBuffer.put(vertices);
        vertexCount += vertices.length/VERTEX_SIZE;

        spritesCount++;

        if (spritesCount >= MAX_SPRITES_DEFAULT) {
            end();
            begin();
        }
    }


    // перечитывает текстуру из файла, записывает ее в готовый уже Id
    // используется при повторной перезагрузке ранее загруженных текстур после потери контекста
    protected void reload_texture_from_file(Texture texture){
        System.out.println("Reloading texture "+texture.filename);
        InputStream is = gle.fileIO.readAsset(texture.filename);

        Bitmap bitmap = BitmapFactory.decodeStream(is);
        int w = bitmap.getWidth();
        int h =bitmap.getHeight();

        texture.width = w;
        texture.height = h;

        ByteBuffer tempBuffer;
        tempBuffer = ByteBuffer.allocateDirect(h * w * 4);
        tempBuffer.limit(h * w * 4);

        int[] pixels = new int[w*h];
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h);

        for(int color:pixels){
            tempBuffer.put((byte)((color>>16) & 0xFF)); // red
            tempBuffer.put((byte)((color>>8)&0xFF));    // green
            tempBuffer.put((byte)((color)&0xFF));       // blue
            tempBuffer.put((byte)((color>>24)&0xFF));   // alpha
        }

        tempBuffer.rewind();

        // загрузим текстуру
        glBindTexture(GL_TEXTURE_2D, texture.textureId);
        // установим картинку
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, texture.width, texture.height,
                0, GL_RGBA, GL_UNSIGNED_BYTE, tempBuffer);

        // GLUtils хреново загружает файлы - производит пре-мультиплирование альфа канала
//        GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);
        // установим режим фильтрации
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        // удалим уже ненужный битмэп
        bitmap.recycle();
        // закроем входной поток
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     *  Создает текстуру из файла.
     *  Загруженные однажды текстуры движок хранит в собственном хранилище
     *  и при потере контекста OpenGL самостоятельно восстанавливает.
     * @param filepath - относительный путь с именем файла текстуры относительно каталога assets
     * @return текстура
     */
    public Texture loadTexture(String filepath){
        Texture texture = new Texture(filepath);

        int textureIds[] = new int[1];
        glGenTextures(1, textureIds, 0);
        texture.textureId = textureIds[0];

        reload_texture_from_file(texture);
        // добавим в список текстур, чтобы можно было перезагрузить все текстуры при потере контекста
        gle.managedTextures.add(texture);

        return texture;
    }


    public Atlas loadAtlas(String texturePath, String atlasPath){
        Log.info("Loading atlas: texture="+texturePath+", atlas="+atlasPath);
        Texture tex = loadTexture(texturePath);
        if (tex == null)
            return null;

        String text = null;
        InputStream istream = gle.fileIO.readAsset(atlasPath);
        try {
            byte buffer[] = new byte[istream.available()];
            istream.read(buffer);
            text = new String(buffer);
        } catch (IOException e) {
            Log.error("Error while reading atlas file.\n"+e.getMessage());
            return null;
        }

        JSONObject atlasJSON = null;
        if (text!=null){
            try {
                atlasJSON = new JSONObject(text);
            } catch (JSONException e) {
                Log.error("Error while parsing JSON.\n"+e.getMessage());
                return null;
            }
        } else {
            return null;
        }

        return new Atlas(tex, atlasJSON);
    }


    /**
     * Создает и компилирует шейдер из двух потоков: потока вершинного шейдера и потока фрагментного шейдеров.
     * В дальнейшем созданный таким образом шейдер можно использовать в методах
     * {@link com.dekagames.gle.Screen#draw(Graphics) draw}, подключив его методом
     * {@link com.dekagames.gle.Graphics#useShader(Shader) useShader} для создания различных эффектов.
     * @param path_vertex путь к вершинному шейдеру
     * @param path_fragment путь к фрагментному шейдеру
     * @return скомпилированный шейдер
     */
    public Shader   loadShader(String path_vertex, String path_fragment){
        InputStream is_vertex = gle.fileIO.readAsset(path_vertex);
        InputStream is_fragment = gle.fileIO.readAsset(path_fragment);

        Shader sh = new Shader(is_vertex, is_fragment);
        gle.managedShaders.add(sh);
        return sh;
    }


    /**
     * Создает и компилирует шейдер из двух строк: строки вершинного шейдера и строки фрагментного шейдеров.
     * В дальнейшем созданный таким образом шейдер можно использовать в методах
     * {@link com.dekagames.gle.Screen#draw(Graphics) draw}, подключив его методом
     * {@link com.dekagames.gle.Graphics#useShader(Shader) useShader} для создания различных эффектов.
     * @param str_vertex строка, содержащая вершинный шейдер
     * @param str_fragment строка, содержащая фрагментный шейдер
     * @return скомпилированный шейдер
     */
    public Shader createShader(String str_vertex, String str_fragment){
        Shader sh = new Shader(str_vertex, str_fragment);
        gle.managedShaders.add(sh);
        return sh;
    }


    public void deleteShader(Shader sh){
        glDeleteProgram(sh.getProgramId());
        gle.managedShaders.remove(sh);
    }


    public void deleteTexture(Texture texture){
        glBindTexture(GL_TEXTURE_2D, texture.textureId);
        int[] texureIds = {texture.textureId};
        glDeleteTextures(1, texureIds, 0);
        gle.managedTextures.remove(texture);
    }



    public void pause(){
        Log.info("Graphics pause() method called");
        glDeleteProgram(shaderForPrimitives.getProgramId());
        glDeleteProgram(shaderForSprites.getProgramId());
    }


}
