precision mediump float;
uniform sampler2D u_Texture;
uniform vec4 color;

varying vec2 v_TexCoordinate;

void main(){
	gl_FragColor = color*texture2D(u_Texture, v_TexCoordinate);	
}
