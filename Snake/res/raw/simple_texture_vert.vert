uniform mat4 u_MVPMatrix;

attribute vec4 a_Position;
varying vec2 v_TexCoordinate;

void main(){
	v_TexCoordinate=vec2(a_Position.x+0.5, a_Position.y+0.5);
	
	//a_Position + vec4(0.5,0.5,0,1);
	//texture2D(u_Texture, a_Position + vec4(0.5,0.5,0,0));
	gl_Position = u_MVPMatrix * a_Position;
}
