package com.dekagames.gle;

import java.io.InputStream;
import java.util.Scanner;

import static android.opengl.GLES20.*;

public class Shader {

    // there are two groups of shaders: vertex/fragment shader for drawing primitives (without texturing,
    // color only) and vertex/fragment shader for drawing sprites (with textures)
    final static String PRIMITIVE_VERTEX_SHADER =
            "uniform mat4 u_MVPMatrix;                          \n"     // Константа отвечающая за комбинацию матриц МОДЕЛЬ/ВИД/ПРОЕКЦИЯ.
                    + "attribute vec4 a_Position;               \n"     // Информация о положении вершин.
                    + "attribute vec4 a_Color;                  \n"     // Информация о цвете вершин.
                    + "varying vec4 v_Color;                    \n"     // Это будет передано в фрагментный шейдер.

                    + "void main()                              \n"     // Начало программы вершинного шейдера.
                    + "{                                        \n"
                    + "   v_Color = a_Color;                    \n"     // Передаем цвет для фрагментного шейдера.
                    // Он будет интерполирован для всего треугольника.
                    + "   gl_Position = u_MVPMatrix             \n"     // gl_Position специальные переменные используемые для хранения конечного положения.
                    + "               * a_Position;             \n"     // Умножаем вершины на матрицу для получения конечного положения
                    + "}                                        \n";    // в нормированных координатах экрана.

    final static String PRIMITIVE_FRAGMENT_SHADER =
            // Устанавливаем по умолчанию среднюю точность для переменных. Максимальная точность
            // в фрагментном шейдере не нужна.
            // Примечание: устанавливаем точность если у нас андроидовский OpenGLES, так как директивы
            // precision не было в настольном OpenGL до версии 4.1 (или типа того)
            "#ifdef GL_ES\n"
                    +"precision mediump float;\n"
                    +"#endif\n"

                    + "varying vec4 v_Color;                    \n"     // Цвет вершинного шейдера преобразованного
                    // для фрагмента треугольников.

                    + "void main()                              \n"     // Точка входа для фрагментного шейдера.
                    + "{                                        \n"
                    + " gl_FragColor = v_Color;                 \n"     // Передаем значения цветов.
                    + "}                                        \n";


    final static String DEFAULT_VERTEX_SHADER =
            "uniform mat4 u_MVPMatrix;                          \n"     // Константа отвечающая за комбинацию матриц МОДЕЛЬ/ВИД/ПРОЕКЦИЯ.
                    + "attribute vec4 a_Position;               \n"     // Информация о положении вершин.
                    + "attribute vec4 a_Color;                  \n"     // Информация о цвете вершин.
                    + "attribute vec2 a_TexCoordinate;          \n"     // текстурные координаты
                    + "varying vec4 v_Color;                    \n"     // Это будет передано в фрагментный шейдер.
                    + "varying vec2 v_TexCoordinate;            \n"

                    + "void main()                              \n"     // Начало программы вершинного шейдера.
                    + "{                                        \n"
                    + "   v_Color = a_Color;                    \n"     // Передаем цвет для фрагментного шейдера.
                    // Он будет интерполирован для всего треугольника.
                    + "   v_TexCoordinate = a_TexCoordinate;    \n"     // передадим текстурную координату во фрагментный шейдер
                    + "   gl_Position = u_MVPMatrix             \n"     // gl_Position специальные переменные используемые для хранения конечного положения.
                    + "               * a_Position;             \n"     // Умножаем вершины на матрицу для получения конечного положения
                    + "}                                        \n";    // в нормированных координатах экрана.

    final static String DEFAULT_FRAGMENT_SHADER =
            // Устанавливаем по умолчанию среднюю точность для переменных. Максимальная точность
            // в фрагментном шейдере не нужна.
            // Примечание: устанавливаем точность если у нас андроидовский OpenGLES, так как директивы
            // precision не было в настольном OpenGL до версии 4.1 (или типа того)
            "#ifdef GL_ES\n"
                    +"precision mediump float;\n"
                    +"#endif\n"

                    + "uniform sampler2D u_Texture;             \n"     // Наша Текстура.
                    + "varying vec4 v_Color;                    \n"     // Цвет вершинного шейдера преобразованного
                    // для фрагмента треугольников.
                    + "varying vec2 v_TexCoordinate;            \n"

                    + "void main()                              \n"     // Точка входа для фрагментного шейдера.
                    + "{                                        \n"
                    + " vec4 texColor = texture2D(u_Texture, v_TexCoordinate);  \n"     // берем цвет из текстуры
                    + " gl_FragColor = v_Color*texColor;                        \n"     // Передаем значения цветов.
                    + "}                                                        \n";


    //    private boolean     is_default_shader;
//    private int         kindOfShader;
    private int         vertexShaderHandle;
    private int         fragmentShaderHandle;
    private int         shaderProgramHandle;

    // адреса обязательных атрибутов
    public int mpvMatrixUniformLocation;       // расположение сборной матрицы MPV в шейдере
    public int textureUniformLocation;         // расположение текстуры в шейдере
    public int positionAttrLocation;           // расоложение атрибута координат в шейдере
    public int colorAttrLocation;              // то же для цвета
    public int texCoordAttrLocation;           // то же для текстурных координат


    // ВОЗМОЖНА ПОТЕРЯ ШЕЙДЕРА ПРИ ПОТЕРЕ КОНТЕКСТА GL
//    private Graphics graphics;

    private String vertexShader, fragmentShader;


    Shader(final String vertex_shader, final String fragment_shader){
        vertexShader = vertex_shader;
        fragmentShader = fragment_shader;

        build(vertex_shader, fragment_shader);
    }


    Shader(InputStream is_vertex, InputStream is_fragment){
        vertexShader = streamToString(is_vertex);
        fragmentShader = streamToString(is_fragment);

        build(vertexShader, fragmentShader);
    }


    public void rebuild(){
        build(vertexShader, fragmentShader);
    }

    private void build(final String vertex_shader, final String fragment_shader){
        // Загрузка вершинного шейдера.
        vertexShaderHandle = glCreateShader(GL_VERTEX_SHADER);
        if (vertexShaderHandle != 0)   {
            // Передаем в наш шейдер программу.
            glShaderSource(vertexShaderHandle, vertex_shader);
            // Компиляция шейдера
            glCompileShader(vertexShaderHandle);
            // Получаем результат процесса компиляции
            final int[] compileStatus = new int[1];
            glGetShaderiv(vertexShaderHandle, GL_COMPILE_STATUS, compileStatus, 0);

            // Если компиляция не удалась, удаляем шейдер.
            if (compileStatus[0] == 0) {
                // проверим ошибку
                String error = glGetShaderInfoLog(vertexShaderHandle);
                Log.error("Unable to compile vertex shader! Error - "+error);
                glDeleteShader(vertexShaderHandle);
                vertexShaderHandle = 0;
            }
        }
        if (vertexShaderHandle == 0)  {
            throw new RuntimeException("Error creating vertex shader.");
        }

        // Загрузка фрагментного шейдера.
        fragmentShaderHandle = glCreateShader(GL_FRAGMENT_SHADER);
        if (fragmentShaderHandle != 0)   {
            // Передаем в наш шейдер программу.
            glShaderSource(fragmentShaderHandle, fragment_shader);
            // Компиляция шейдера
            glCompileShader(fragmentShaderHandle);
            // Получаем результат процесса компиляции
            final int[] compileStatus = new int[1];
            glGetShaderiv(fragmentShaderHandle, GL_COMPILE_STATUS, compileStatus, 0);
            // Если компиляция не удалась, удаляем шейдер.
            if (compileStatus[0] == 0) {
                String error = glGetShaderInfoLog(fragmentShaderHandle);
                Log.error("Unable to compile fragment shader! Error - "+error);
                glDeleteShader(fragmentShaderHandle);
                fragmentShaderHandle = 0;
            }
        }
        if (fragmentShaderHandle == 0)  {
            throw new RuntimeException("Error creating fragment shader.");
        }

        // Создаем объект программы вместе со ссылкой на нее.
        shaderProgramHandle = glCreateProgram();

        if (shaderProgramHandle != 0)  {
            // Подключаем вершинный шейдер к программе.
            glAttachShader(shaderProgramHandle, vertexShaderHandle);
            // Подключаем фрагментный шейдер к программе.
            glAttachShader(shaderProgramHandle, fragmentShaderHandle);
            // Объединяем оба шейдера в программе.
            glLinkProgram(shaderProgramHandle);
            // Получаем статус линковки
            final int[] linkStatus = new int[1];
            glGetProgramiv(shaderProgramHandle, GL_LINK_STATUS, linkStatus, 0);

            // Если линковка не удалась - удаляем программу
            if (linkStatus[0] == 0)  {
                String error = glGetProgramInfoLog(shaderProgramHandle);
                Log.error("Unable to link shader program! Error - "+error);
                glDeleteProgram(shaderProgramHandle);
                shaderProgramHandle = 0;
                throw new RuntimeException("Error linking shader!");
            }

            // получим адреса обязательных атрибутов, которые должны присутствовать в шейдере
            mpvMatrixUniformLocation = glGetUniformLocation(shaderProgramHandle, "u_MVPMatrix");  // адрес униформы матрицы
            positionAttrLocation = glGetAttribLocation(shaderProgramHandle, "a_Position");             // адреса атрибуов передаваемых в шейдер
            colorAttrLocation = glGetAttribLocation(shaderProgramHandle, "a_Color");

            textureUniformLocation = glGetUniformLocation(shaderProgramHandle, "u_Texture");   // адрес униформы текстуры
            texCoordAttrLocation = glGetAttribLocation(shaderProgramHandle, "a_TexCoordinate");
        }
    }


    public void setUniform(String uniformName, float value){
        glUseProgram(shaderProgramHandle);
        int location = glGetUniformLocation(shaderProgramHandle,uniformName);
        glUniform1f(location, value);
    }


    public void setUniform(String uniformName, int value){
        glUseProgram(shaderProgramHandle);
        int location = glGetUniformLocation(shaderProgramHandle,uniformName);
        glUniform1i(location, value);
    }


    public void setUniform(String uniformName, Point point){
        glUseProgram(shaderProgramHandle);
        int location = glGetUniformLocation(shaderProgramHandle,uniformName);
        glUniform2f(location, point.x, point.y);
    }


    public int getProgramId(){
        return shaderProgramHandle;
    }

    // конвертируем поток в строку для
    private String streamToString(InputStream is){
        Scanner s = new Scanner(is);//.useDelimiter("\\A");
        StringBuffer buf = new StringBuffer();
        while (s.hasNext()){
            buf.append(s.nextLine());
            buf.append("\n");
        }
        return buf.toString();
    }
}
