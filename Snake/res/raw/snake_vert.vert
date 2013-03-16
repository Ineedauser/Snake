uniform mat4 u_MVPMatrix;
uniform vec2 coordinateShift;
uniform vec4 rotateVector;

attribute vec4 a_Position;
varying vec2 v_TexCoordinate;


void main(){
	v_TexCoordinate=vec2(rotateVector.x*a_Position.x+rotateVector.y*a_Position.y+0.5, rotateVector.z*a_Position.x+rotateVector.w*a_Position.y+0.5);
	gl_Position = u_MVPMatrix * (a_Position + vec4(coordinateShift,0,0)) ;
}
