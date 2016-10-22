package com.dekagames.gle;

// спрайт состоит из двух треугольников вот с такой нумерацией
//1-----2(4)
//|    /|
//|   / |
//|  /  |
//| /   |
//|/    |
//3(6)--5
//

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * класс описывающий отдельный кадр
 * @author Deka
 *
 */
class Frame {
    // текстурные координаты кадра (0..1)
    float 	s1,t1,      // левый верхний угол
            s2,t2,      // правый верхний угол
            s3,t3,      // левый нижний угол
            s4,t4,      // правый верхний угол
            s5,t5,      // правый нижний угол
            s6,t6;      // левый нижний угол
    int  	pivotx, pivoty;
    int 	w,h;		// ширина и высота кадра
}




/**
 * Основной класс для работы со спрайтами. Весь вывод графики (GUI, шрифты и т.д.) делается именно через класс
 * спрайта. Спрайт представляет собой набор прямоугольных кадров (в самом общем случае они могут быть разного размера),
 * представляющий собой либо кадры анимации, либо изображения разных состояний объекта (например кнопка). У каждого
 * кадра есть свой pivot - точка, относительно которой кадр рисуется, масштабируется и поворачивается. Спрайт можно
 * загружать двумя способами:
 * <br>- из атласа, созданного утилитой AtlasMaker, использующей Slon - формат файла;
 * <br>- из spriteSheet - одной большой картинки, в которой все кадры склеены вместе, без зазоров, слева-направо,
 * сверху-вниз. В этом случае все кадры должны иметь одинаковый размер и единый pivot.
 */
public class Sprite {
    private Texture		tex;
    private int 		tex_width, tex_height;				// Размеры текстуры
    private int			n_cols;								// количество картинок в одном ряду текстуры

    // осмысленные имена индексов в массиве координат
    static final int	X1=0, 	Y1=1, 	R1=2,  G1=3,  B1=4,  A1=5, 	S1=6, 	T1=7,
            X2=8, 	Y2=9, 	R2=10, G2=11, B2=12, A2=13,	S2=14, 	T2=15,
            X3=16, 	Y3=17, 	R3=18, G3=19, B3=20, A3=21,	S3=22, 	T3=23,
            X4=24, 	Y4=25, 	R4=26, G4=27, B4=28, A4=29,	S4=30, 	T4=31,
            X5=32, 	Y5=33, 	R5=34, G5=35, B5=36, A5=37,	S5=38, 	T5=39,
            X6=40, 	Y6=41, 	R6=42, G6=43, B6=44, A6=45,	S6=46, 	T6=47;

    private float[] 	vertices;

    private  Frame[]	frames;

    // the number of the frames in required order
    private int         frameIndexes[];

    private int						n_frames;
    private int						n_fps;
    private int						n_tx, n_ty;		                    		// текстурные координаты и размеры кадра (если все кадры одинаковые)

    // frame indexes from frameIndexes[].
    private int                     curr_index;
    private int                     n_curr_frame, n_start_frame, n_end_frame;

    private boolean					b_flip_h, b_flip_v;							// будет ли спрайт рисоваться отраженным по вертикали или горизонталм
    private boolean					b_loop;
    private boolean					b_playing;

    // переменные для обработки фэйдера
    private boolean                 is_fade_in, is_fade_out, is_fade_inout;
    private float                   current_fade_time, full_fade_time;
    private float                   fade_alpha;
    private int                     fade_repeat_count, current_fade_iteration;
    private boolean                 fade_loop;
    private float                   in_time, stay_time, out_time;               // времена составного фэйдера

    private float					f_fixed_delta;
    private float					f_delta;


    /**
     * Метод с видимостью package. Вызывается из public метода Atlas.getSprite().
     * Загрузка спрайта из атласа, созданного AtlasMaker-ом. Используется Slon формат файла.
     * @param texture текстура, на которой находятся кадры спрайта. На одной текстуре может содержаться
     *                большое количество разнотипных элементов - кадры разных спрайтов, символы шрифтов и т.д.
     *                AtlasMaker упаковывает их всех плотно, для экономии места на одну текстуру.
     * @param name имя спрайта в slon-файле. Указывается при добавлении спрайта в AtlasMaker.
     * @param fps частота смены кадров для анимации. Если кадр всего один - можно указать 0;
     */
    Sprite(Texture texture, JSONObject jsonFile, String name, int fps) {
        int tx1,ty1,w,h,pivotx, pivoty;	// расчетные текстурные координаты
        JSONObject sprSection, sprite, frame;
        JSONArray frmSection;                   // секция с кадрами

        if (fps<=0) fps=1;
        n_fps = fps;

        tex = texture;
        tex_width = tex.width;
        tex_height = tex.height;

        try {
            sprSection = jsonFile.getJSONObject("sprites");
            sprite = sprSection.getJSONObject(name);
            frmSection = sprite.getJSONArray("frames");

            // количество кадров
            n_frames = frmSection.length();
            frames = new Frame[n_frames];
            vertices = new float[Graphics.QUAD_SIZE];

            // запишем массив кадров
            for (int i = 0; i<n_frames; i++) {
                frame = (JSONObject) frmSection.get(i);
                // прочитаем все координаты
                tx1 = frame.getInt("x");
                ty1 = frame.getInt("y");
                w = frame.getInt("w");
                h = frame.getInt("h");
                pivotx = frame.getInt("pivotX");
                pivoty = frame.getInt("pivotY");
                set_frame_coords(i,tx1,ty1,w,h,pivotx,pivoty);
            }
        } catch (JSONException e) {
            Log.error("Can not create sprite \"" + name + "\" from atlas. "+e.getMessage());
            return;
        }

        default_init();
    }


//    /**
//     * Загрузка спрайта из SlonNode.
//     * @param texture
//     * @param sprNode
//     * @param fps
//     */
//    public Sprite(Texture texture, SlonNode sprNode, int fps) {
//        int tx1,ty1,w,h,pivotx,pivoty;	// расчетные текстурные координаты
//        SlonNode nodeFrame;
//
//        nodeSprite = sprNode;
//        if (fps<=0) fps=1;
//        n_fps = fps;
//
//        tex = texture;
//        tex_width = tex.width;
//        tex_height = tex.height;
//
//        // количество кадров
//        n_frames = nodeSprite.getChildCount();
//        frames = new Frame[n_frames];
//        vertices = new float[Graphics.QUAD_SIZE];
//        // запишем массив кадров
//        for (int i = 0; i<n_frames; i++) {
//            nodeFrame = nodeSprite.getChildAt(i);
//            // прочитаем все координаты
//            tx1 = nodeFrame.getKeyAsInt("x");
//            ty1 = nodeFrame.getKeyAsInt("y");
//            w = nodeFrame.getKeyAsInt("w");
//            h = nodeFrame.getKeyAsInt("h");
//            pivotx = nodeFrame.getKeyAsInt("pivotX");
//            pivoty = nodeFrame.getKeyAsInt("pivotY");
//            set_frame_coords(i,tx1,ty1,w,h,pivotx,pivoty);
//        }
//        default_init();
//    }



    /**
     * Создание спрайта напрямую по координатам текстуры. Требуется, например, при загрузке модели
     * скелетной анимации. В этом случае все кадры в файле должны располагаться друг за другом
     * слева-направо, сверху-вниз начиная с левого верхнего угла изображения, и иметь одинаковые размеры.
     * Pivot кадра (точка относительно которой он рисуется, масштабируется и т.д.) по умолчанию
     * находится в точке (0, 0), то есть в левом верхнем углу кадра. Его можно изменить методом
     * {@link Sprite#setHotSpot(int, int)}.
     * @param texture текстура, в которой находятся кадры, слева-направо сверху-вниз.
     *                Первый кадр может находится где угодно, но все последующие кадры должны быть
     *                расположены по-порядку (при переходе на следующий ряд кадры должны  начинаться
     *                от левого края текстуры).
     * @param nframes количество кадров, подлежащих загрузке.
     * @param fps частота воспроизведения кадров. Если кадр один, можно указать 0.
     * @param tx координата X левого верхнего угла первого кадра.
     * @param ty координата Y левого верхнего угла первого кадра.
     * @param w ширина кадра в пикселях.
     * @param h высота кадра в пикселях.
     */
    public Sprite(Texture texture,  int nframes,  int fps,  int tx,  int ty,  int w,  int h){
        int tx1,ty1;	// расчетные текстурные координаты
        int	n;					// номер кадра

        frames = new Frame[nframes];
        vertices = new float[Graphics.QUAD_SIZE];

        if (fps<=0) fps=1;
        tex = texture;
        tex_width = tex.width;
        tex_height = tex.height;
        n_frames	= nframes;
        n_fps 		= fps;
        n_tx = tx;
        n_ty = ty;

        // сформируем массив кадров
        n_cols = tex_width / w;//n_width; // количество кадров в одном ряду
        for (int i=0; i<n_frames; i++) {
            n = i;

            // вычислим текстурные координаты для кадра n
            ty1 = n_ty;
            tx1 = n_tx + n * w;//n_width;

            if(tx1 > tex.width - w)	{
                n -= (tex.width - n_tx) / w;//n_width; 		// уменьшим n на количество кадров в ряду
                tx1 = w * (n % n_cols);
                ty1 += h * (1 + n/n_cols);
            }
            set_frame_coords(i,tx1,ty1,w, h,0,0);
        }
        default_init();
    }

    // создает кадр в массиве кадров с индексом и размерами
    private final void set_frame_coords(int frame, int x, int y, int w, int h, int piv_x, int piv_y) {
        float tx1 = x;// + 0.5f;
        float tx2 = x + w;// - 0.5f;
        float ty1 = y;// + 0.5f;
        float ty2 = y + h;// - 0.5f;

        // запишем координаты
        frames[frame] = new Frame();

        frames[frame].s1 = tx1/tex_width;			frames[frame].t1 = ty1/tex_height;
        frames[frame].s2 = tx2/tex_width;			frames[frame].t2 = ty1/tex_height;
        frames[frame].s3 = tx1/tex_width;			frames[frame].t3 = ty2/tex_height;
        frames[frame].s4 = frames[frame].s2;		frames[frame].t4 = frames[frame].t2;
        frames[frame].s5 = tx2/tex_width;			frames[frame].t5 = ty2/tex_height;
        frames[frame].s6 = frames[frame].s3;		frames[frame].t6 = frames[frame].t3;

        // размеры кадра
        frames[frame].w = w;
        frames[frame].h = h;
        // горячая точка
        frames[frame].pivotx = piv_x;
        frames[frame].pivoty = piv_y;
    }

    // начальная инициализация спрайта
    private final void default_init() {
        setFrame(0);
        setColor(1, 1, 1);
        setAlpha(1);
        f_fixed_delta = 1.0f/n_fps;
        b_loop = true;
        b_playing =false;
    }

    /**
     * Return number of frames.
     * @return number of frames.
     */
    public int getFramesCount(){
        return frames.length;
    }

    /**
     * Возвращает ширину кадра. Для спрайтов загруженных из SpriteSheet ширина всех кадров одинакова.
     * @param nFrame номер кадра, ширину которого нужно узнать. При выходе n за пределы диапазона номеров
     *               кадра, n "прокручивается" через количество кадров в анимации для возвращения
     *               в диапазон (0..n_frames-1).
     * @return ширина n-го кадра в пикселях.
     */
    public int getFrameWidth(int nFrame) {
        nFrame = nFrame % n_frames;	// если n больше чем количество кадров то вернем его в диапазон
        if(nFrame < 0) nFrame = n_frames + nFrame;
        return frames[nFrame].w;
    }

    /**
     * Возвращает высоту кадра. Для спрайтов загруженных из SpriteSheet высота всех кадров одинакова.
     * @param nFrame номер кадра, высоту которого нужно узнать. При выходе n за пределы диапазона номеров
     *               кадра, n "прокручивается" через количество кадров в анимации для возвращения
     *               в диапазон (0..n_frames-1).
     * @return высота n-го кадра в пикселях.
     */
    public int getFrameHeight(int nFrame) {
        nFrame = nFrame % n_frames;	// если n больше чем количество кадров то вернем его в диапазон
        if(nFrame < 0) nFrame = n_frames + nFrame;
        return frames[nFrame].h;
    }


//    /**
//     * Set array of indexes of the frames in order they need to appear in animation.
//     * For example: we have sprite with 3 frames (0,1,2). We can use this
//     * method to set certain sequence of frames (e.g [0,0,0,1,1,2,1,0]) and this sequence will be used
//     * for playing sprite animation. By default, this array is initialized with the natural sequence [0,1,2].
//     * Methods like {@link com.dekagames.dongle.Sprite#play(int, int, boolean)} retrieve parameters which are
//     * actually indexes of the frames in this array.
//     * @param indexes
//     */
//    public void setFrameIndexes(int[] indexes){
//        frameIndexes = indexes;
//    }

    /**
     * Запуск воспроизведения с указанием начального и конечного кадров. Метод только
     * устанавливает начальные параметры и необходимость воспроизведения анимации.
     * Чтобы анимация воспроизводилась
     * необходимо каждую итерацию игрового цикла для спрайта вызывать метод
     * {@link com.dekagames.gle.Sprite#update(float)} именно в нем происходит смена кадров.
     *
     *  @param nstart номер кадра, с которого необходимо играть анимацию. Кадры нумеруются с 0.
     *  @param nend номер последнего кадра, на котором анимация заканчивается.
     *  @param loop требуется ли воспроизводить анимацию по кругу или нет.
     */
    public void play(int nstart, int nend, boolean loop){
        frameIndexes = null;
        if (b_playing && (nstart== n_start_frame) && (nend== n_end_frame)) return;
        b_playing = true;
        b_loop = loop;
        n_start_frame = nstart;
        n_curr_frame = nstart;
        n_end_frame = nend;
        f_delta = 0;
    }


    /**
     * Запуск воспроизведения всех кадров анимации. Метод только
     * устанавливает начальные параметры и необходимость воспроизведения анимации.
     * Чтобы анимация воспроизводилась
     * необходимо каждую итерацию игрового цикла для спрайта вызывать метод
     * {@link com.dekagames.gle.Sprite#update(float)} именно в нем происходит смена кадров.
     *
     *  @param loop требуется ли воспроизводить анимацию по кругу или нет.
     */
    public void play(boolean loop){
        frameIndexes = null;
        if (b_playing) return;
        b_playing = true;
        b_loop = loop;
        n_start_frame = 0;
        n_curr_frame = 0;
        n_end_frame = n_frames-1;
        f_delta = 0;
    }

    /**
     * Start play animation. Frames appear in order from indexes parameter.
     * @param indexes
     * @param loop
     */
    public void play(int[] indexes, boolean loop){
        if (b_playing) return;
        frameIndexes = indexes;
        b_playing = true;
        b_loop = loop;

        curr_index = 0;
//        n_start_frame = 0;
//        n_curr_frame = 0;
//        n_end_frame = n_frames-1;
        f_delta = 0;

    }


    /**
     * Останавливает анимацию.
     */
    public void stop(){
        b_playing = false;
    }


    /**
     * Проверяет активность любого из процессов FadeOut, FadeIn или FadeInOut.
     * @return true, если хотя бы один из процессов активен.
     */
    public boolean isFading(){
        return (is_fade_in || is_fade_out || is_fade_inout);
    }


    /**
     * Проверяет активность процесса Fade In.
     * @return true, если процесс активен.
     */
    public boolean isFadingIn(){
        return is_fade_in;
    }


    /**
     * Проверяет активность процесса Fade Out.
     * @return true, если процесс активен.
     */
    public boolean isFadingOut(){
        return is_fade_out;
    }


    /**
     * Запускает плавное проявление спрайта. Проверить окончание этого процесса можно с помощью метода
     * {@link Sprite#isFadingIn()} или {@link Sprite#isFading()}.
     * @param time время, за которое должен проявиться спрайт в секундах. Должно быть больше 0.01f.
     */
    public void fadeIn(float time){
        if (is_fade_in || time < 0.01f) return;
        full_fade_time = time;
        current_fade_time = 0;
        fade_alpha = 0;
        is_fade_in = true;
        is_fade_out = false;        // fool's protect
    }


    /**
     * Запускает плавное исчезание спрайта. Проверить окончание этого процесса можно с помощью метода
     * {@link Sprite#isFadingOut()} или {@link Sprite#isFading()}.
     * @param time время, за которое должен исчезнуть спрайт в секундах. Должно быть больше 0.01f.
     */
    public void fadeOut(float time){
        if (is_fade_out || time < 0.01f) return;
        full_fade_time = time;
        current_fade_time = 0;
        fade_alpha = 1;
        is_fade_in = false;
        is_fade_out = true;        // fool's protect
    }


    /**
     * Плавное проявление спрайта, потом плавное его исчезание. Отлично подходит для экрана с логотипом.
     * @param inTime время проявления в секундах
     * @param stayTime время нахождения в состоянии полного показа в секундах
     * @param outTime время исчезания в секундах
     * @param repeat количество повторений. Если отрицательное или ноль - повторять непрерывно.
     */
    public void fadeInOut(float inTime, float stayTime, float outTime, int repeat){
        if (is_fade_inout) return;
        in_time = inTime;
        stay_time = stayTime;
        out_time = outTime;
        current_fade_iteration = 0;

        is_fade_inout = true;
        if (repeat>0){
            fade_loop = false;
            fade_repeat_count = repeat;
        }
        else {
            fade_loop = true;
        }

        fadeIn(inTime);     // запускаем начальный фэйдер
    }


    /**
     * Метод для обновления фэйдера. Вызывается автоматически из основного метода
     * {@link com.dekagames.gle.Sprite#update(float)} при наличии флагов фэйдера до обновления анимации.
     * @param delta время в секундах с прошлой итерации
     */
    private void update_fading(float delta){
        current_fade_time += delta;

        if (current_fade_time >= full_fade_time){       // fade time is out - current fade finished
            if (is_fade_inout){                 // последовательный fade in - fade out

                if (is_fade_in){                // закончилась первая стадия составного фэйдера
                    is_fade_in = false;
                    is_fade_out = false;
                    full_fade_time = stay_time;
                    current_fade_time = 0;
                    fade_alpha = 1;
                }

                else if (is_fade_out) {          // закончилась последняя стадия составного фэйдера
                    current_fade_iteration++;

                    if (fade_loop || (current_fade_iteration < fade_repeat_count))
                        fadeIn(in_time);

                    else {                      // прекратим составной фэйдер
                        is_fade_inout = false;
                        is_fade_in = false;
                        is_fade_out = false;
                        fade_alpha = 0;
                    }
                }



                else {                          // закончилась вторая стадия составного фэйдера
                    fadeOut(out_time);
                }

            }

            else {                  //одиночный фэйдер
                if (is_fade_in) {
                    is_fade_in = false;
                    fade_alpha = 1;
                } else if (is_fade_out) {
                    is_fade_out = false;
                    fade_alpha = 0;
                }
            }
        }

        else {                                          // continue fading
            if (is_fade_in)
                fade_alpha = current_fade_time/full_fade_time;
            else if (is_fade_out)
                fade_alpha = 1-current_fade_time/full_fade_time;
        }
        setAlpha(fade_alpha);
    }


    /**
     * Обновляет анимацию спрайта. Этот метод необходимо вызывать каждую итерацию игрового цикла
     * и передавать ему время, прошедшее с момента прошлого вызова. В этом методе происходит выбор
     * текущего кадра на основе fps и прошедшего времении.
     * @param delta время, прошедшее с момента прошлого вызова в секундах.
     */
    public void update(float delta){
        if (is_fade_in || is_fade_out || is_fade_inout)
            update_fading(delta);

        if(!b_playing)
            return;

        f_delta += delta; 			// время с предыдущего кадра
        // найдем текущий кадр
        while(f_delta >= f_fixed_delta) {
            f_delta -= f_fixed_delta;
            if (frameIndexes == null){                      // regular playing actual frames
                if(n_curr_frame + 1 > n_end_frame)	{
                    if (b_loop)
                        setFrame(n_start_frame);
                    else
                        b_playing = false;
                }
                else
                    setFrame(n_curr_frame +1);
            } else {                                        // playing indexed frames
                if (curr_index<frameIndexes.length-1){
                    curr_index++;
                    setFrame(frameIndexes[curr_index]);
                } else {
                    if (b_loop){
                        curr_index = 0;
                        setFrame(frameIndexes[curr_index]);
                    } else
                        b_playing= false;
                }

            }
        }
    }


    /**
     * Рисует спрайт. За прорисовку всего и вся отвечает класс {@link com.dekagames.gle.Graphics},
     * экземпляр которого передается в этот метод. Сам экземпляр {@link com.dekagames.gle.Graphics} создается
     * классами, запускающими игру на разных системах и сохраняется в классе {@link com.dekagames.gle}.
     * @param graphics экземпляр {@link com.dekagames.gle.Graphics}, обычно что-то вроде game.graphics.
     * @param x координата x спрайта в пикселях от верхнего левого угла экрана.
     * @param y координата y спрайта в пикселях от верхнего левого угла экрана.
     */
    public void draw(Graphics graphics, float x, float y){
        drawEx(graphics,x,y,0,1.0f,0);
    }


    /**
     * Рисует спрайт с масштабированием. За прорисовку всего и вся отвечает класс {@link com.dekagames.gle.Graphics},
     * экземпляр которого передается в этот метод. Сам экземпляр {@link com.dekagames.gle.Graphics} создается
     * классами, запускающими игру на разных системах и сохраняется в классе {@link com.dekagames.gle}.
     * @param graphics экземпляр {@link com.dekagames.gle.Graphics}, обычно что-то вроде game.graphics.
     * @param x координата x спрайта в пикселях от верхнего левого угла экрана.
     * @param y координата y спрайта в пикселях от верхнего левого угла экрана.
     * @param scale коэффициент масштабирования спрайта.
     */
    public void draw(Graphics graphics, float x, float y, float scale){
        drawEx(graphics,x,y,0,scale,0);
    }


    /**
     * Рисует спрайт с поворотом и масштабированием. За прорисовку всего и вся отвечает класс
     * {@link com.dekagames.gle.Graphics},
     * экземпляр которого передается в этот метод. Сам экземпляр {@link com.dekagames.gle.Graphics} создается
     * классами, запускающими игру на разных системах и сохраняется в классе {@link com.dekagames.gle}.
     * @param graphics экземпляр {@link com.dekagames.gle.Graphics}, обычно что-то вроде game.graphics.
     * @param x координата x спрайта в пикселях от верхнего левого угла экрана.
     * @param y координата y спрайта в пикселях от верхнего левого угла экрана.
     * @param angle угол поворота спрайта в градусах по часовой стрелке.
     * @param hscale коэффициент масштабирования по горизонтали
     * @param vscale коэффициент масштабирования по вертикали,
     *               если vscale = 0, то он не учитывается и скалируется одинаково по параметру hscale
     */
    public void drawEx(Graphics graphics, float x, float y, float angle, float hscale, float vscale) {
        float tx1, ty1, tx2, ty2;  	// временные координаты
        float sint, cost;
        int thsx, thsy;			// временный хотспот
        int w = frames[n_curr_frame].w;				// ширина и высота кадра
        int h = frames[n_curr_frame].h;

        if(vscale<=0) vscale=hscale;

        // горячая точка переносится при флипе!
        if (b_flip_h)	thsx = w- frames[n_curr_frame].pivotx;//n_hot_x;
        else			thsx = frames[n_curr_frame].pivotx;//n_hot_x;

        if (b_flip_v)	thsy = h - frames[n_curr_frame].pivoty;//n_hot_y;
        else			thsy = frames[n_curr_frame].pivoty;//n_hot_y;


        tx1 = -thsx * hscale;
        ty1 = -thsy * vscale;
        tx2 = (w - thsx)*hscale;
        ty2 = (h - thsy)*vscale;

        if (angle != 0)	{
            cost = MathUtils.cos(angle);
            sint = MathUtils.sin(angle);

            vertices[X1] = tx1*cost - ty1*sint + x;
            vertices[Y1] = tx1*sint + ty1*cost + y;

            vertices[X2] = tx2*cost - ty1*sint + x;
            vertices[Y2] = tx2*sint + ty1*cost + y;

            vertices[X3] = tx1*cost - ty2*sint + x;
            vertices[Y3] = tx1*sint + ty2*cost + y;

            vertices[X4] = vertices[X2];//tx2*cost - ty1*sint + x;
            vertices[Y4] = vertices[Y2];//tx2*sint + ty1*cost + y;

            vertices[X5] = tx2*cost - ty2*sint + x;
            vertices[Y5] = tx2*sint + ty2*cost + y;

            vertices[X6] = vertices[X3];//tx1*cost - ty2*sint + x;
            vertices[Y6] = vertices[Y3];//tx1*sint + ty2*cost + y;
        }
        else {
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
        }

        graphics.draw(tex, vertices);
    }


    /**
     * Возвращает true, если проигрывается анимация спрайта и false если нет.
     * @return true, если анимация в настоящий момент проигрывается, иначе false
     */
    public boolean isPlaying() {
        return b_playing;
    }


    /**
     * Устанавливает в качестве текущего кадра кадр с номером n.
     * @param n номер кадра. Если номер выходит из диапазона допустимых кадром, то
     *          n "прокручивается" через количество кадров в анимации для возвращения
     *          в диапазон (0..n_frames-1).
     */
    public void setFrame(int n) {
        float temp;

        n = n % n_frames;	// если n больше чем количество кадров то вернем его в диапазон
        if(n < 0) n = n_frames + n;

        n_curr_frame = n;

        vertices[S1] = frames[n].s1;			vertices[T1] = frames[n].t1;
        vertices[S2] = frames[n].s2;			vertices[T2] = frames[n].t2;
        vertices[S3] = frames[n].s3;			vertices[T3] = frames[n].t3;

        vertices[S4] = frames[n].s4;			vertices[T4] = frames[n].t4;
        vertices[S5] = frames[n].s5;			vertices[T5] = frames[n].t5;
        vertices[S6] = frames[n].s6;			vertices[T6] = frames[n].t6;

        // если надо отразить по горизонтали
        if (b_flip_h){
            temp = vertices[S1]; vertices[S1] = vertices[S2]; vertices[S2]=temp; vertices[S4]=temp;
            temp = vertices[S5]; vertices[S5] = vertices[S3]; vertices[S3]=temp; vertices[S6]=temp;


        }

        // если надо отразить по вертикали N0-N3  N2-N1
        if (b_flip_v){
            temp = vertices[T1]; vertices[T1] = vertices[T3]; vertices[T3]=temp; vertices[T6]=temp;
            temp = vertices[T5]; vertices[T5] = vertices[T2]; vertices[T2]=temp; vertices[T4]=temp;
        }
    }


    /**
     * Устанавливает режим отражения спрайта.
     * @param horizontal если этот параметр равен true, то спрайт при прорисовке будет отражен по горизонтали
     * @param vertical если этот параметр равен true, то спрайт при прорисовке будет отражен по вертикали
     */
    public void setFlip(boolean horizontal, boolean vertical){
        b_flip_h = horizontal;
        b_flip_v = vertical;
        setFrame(n_curr_frame);
    }


    /**
     * Возвращает режим отражения спрайта по горизонтали.
     * @return true, если спрайт рисуется отраженным по горизонтали, иначе false.
     */
    public boolean getHFlip(){
        return b_flip_h;
    }


    /**
     * Возвращает режим отражения спрайта по вертикали.
     * @return true, если спрайт рисуется отраженным по вертикали, иначе false.
     */
    public boolean getVFlip(){
        return b_flip_v;
    }


    /**
     * Устанавливает цвет спрайта не трогая прозрачность.
     * @param r красная составляющая цвета (0..1)
     * @param g зеленая составляющая цвета (0..1)
     * @param b синяя составляющая цвета (0..1)
     */
    public void setColor (float r, float g, float b) {
        vertices[R1] = vertices[R2] = vertices[R3] = vertices[R4] = vertices[R5] = vertices[R6] = r;
        vertices[G1] = vertices[G2] = vertices[G3] = vertices[G4] = vertices[G5] = vertices[G6] = g;
        vertices[B1] = vertices[B2] = vertices[B3] = vertices[B4] = vertices[B5] = vertices[B6] = b;
    }


    /**
     * Set full sprite color in the ARGB format.
     * @param argbColor color in ARGB format (i.e. 0xAARRGGBB).
     */
    public void setARGBColor(int argbColor){
        int alpha = (argbColor>>>24) & 0xFF;
        int red = (argbColor>>>16) & 0xFF;
        int green = (argbColor>>>8) & 0xFF;
        int blue = (argbColor & 0xFF);
        setColor(red/255.0f, green/255.0f, blue/255.0f);
        setAlpha(alpha/255.0f);
    }


    /**
     * Устанавливает прозрачность спрайта.
     * @param a альфа составляющая цвета (0..1)
     */
    public void setAlpha (float a) {
        vertices[A1] = vertices[A2] = vertices[A3] = vertices[A4] = vertices[A5] = vertices[A6] = a;
    }


    /**
     * Перезаписывает горячую точку для всех кадров одновременно. При загрузке из атласа,
     * как правило, не используется, так как в атласе задаются горячие точки
     * для каждого кадра в отдельности, поэтому для атласа может испортить прорисовку!
     * Координаты точки отсчитываются от нижнего!!!! левого угла спрайта.
     * @param x координата x
     * @param y координата y
     */
    public void setHotSpot(int x, int y){
        for (Frame fr:frames) {
            fr.pivotx = x;
            fr.pivoty = y;
        }
    }


    /**
     * Возвращает номер текущего кадра (кадра, рисуемого в данный момент).
     * @return номер текущего кадра.
     */
    public int getFrame(){
        return n_curr_frame;
    }
}
