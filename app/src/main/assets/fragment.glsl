#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D u_Texture;
varying vec4 v_Color;
varying vec2 v_TexCoordinate;

uniform float	u_time;		// время с нарастанием
uniform float 	scr_h;		// высота виртуального экрана в скалированных единицах
uniform vec2	sprxy;		// позиция спрайта
uniform vec2	sizexy;		// размер спрайта
uniform float 	yoffset;	// ширина черных полосок сверху и снизу
uniform float 	xoffset;	// ширина черных полосок слева и справа
uniform float 	scale;		// коэффициент масштабирования


void main(void){
	// для упрощения записи
	float sprw = sizexy.x;
	float sprh = sizexy.y;

	float posx = sprxy.x;
	float posy = sprxy.y;

	// получим точку на текстуре, из которой будем брать цвет (координаты 0...1)
	float pointy = ((gl_FragCoord.y - yoffset) - (scr_h-(posy+sprh)*scale))/(sprh*scale);

	// максимальная амплитуда на данной высоте 0..width
	float amp = (sprw*scale/1.5 + sin(pointy*50.0+u_time*8.0) * sprw * scale *(1.0-pointy)/10.0);

	// насколько точка отклоняется от центра -\+
	float	dx = (gl_FragCoord.x-xoffset - posx*scale)-sprw*scale/2.0;

	// во сколько раз уменьшаем
	float k = amp/(sprw*scale);

	// возьмем точку дальше
	float pointx = (sprw*scale/2.0+dx * k) /(sprw * scale);
	if (pointx>1.0 || pointx<-1.0) discard;

	vec4 color = texture2D(u_Texture, vec2(pointx, pointy));

	// добавим прозрачности внизу
	float alpha = color.a * pointy * 0.8;
	gl_FragColor = vec4(color.rgb, alpha);
}



