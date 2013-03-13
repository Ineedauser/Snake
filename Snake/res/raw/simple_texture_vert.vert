uniform mat4 u_MVPMatrix;
uniform sampler2D u_Texture;

attribute vec4 a_Position;
varying vec4 v_Color;

void main(){
	v_Color=texture2D(u_Texture, a_Position + vec4(0.5,0.5,0,0));
	gl_Position = u_MVPMatrix * a_Position;
}
