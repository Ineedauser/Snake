package com.szofttech.snake;

import java.nio.FloatBuffer;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

public class Grid extends Renderable{
	
	OpenGLProgram program;

	private FloatBuffer vertexData;
	
	int mvpMatrixHandle;
	int positionHandle;
	int colorHandle;
	
	private final int POS_OFFSET = 0;
	private final int COLOR_OFFSET = 3;
	private final int POS_SIZE = 3;
	private final int COLOR_SIZE = 4;
	private final int STRIDE = 7 * BYTES_PER_FLOAT;
	
	

	public Grid(Context appContext) {
		super(appContext);
		
		final float[] triangle1VerticesData = {
				// X, Y, Z, 
				// R, G, B, A
	            10.0f, 0.0f, 0.0f, 
	            1.0f, 0.0f, 0.0f, 1.0f,
	            
	            -10.0f, 0.0f, 0.0f,
	            0.0f, 0.0f, 1.0f, 1.0f,
	            
	            0.0f, 5.0f, 0.0f, 
	            0.0f, 1.0f, 0.0f, 1.0f};
		
		vertexData=createFloatBufferFromData(triangle1VerticesData);
	}
	
	
	@Override
	public void init() {
		program=new OpenGLProgram(appContext, R.raw.passthrough_vert, R.raw.passthrough_frag);
		
		mvpMatrixHandle = program.getUniformLocation("u_MVPMatrix");        
	    positionHandle = program.getAttributeLocation("a_Position");
	    colorHandle = program.getAttributeLocation("a_Color");
	}
	
	@Override
	public void renderPrepare(long time) {
		//float angleInDegrees = (360.0f / 10000.0f) * ((int) time);
        
        // Draw the triangle facing straight on.
        //Matrix.setIdentityM(modelMatrix, 0);
       // Matrix.rotateM(modelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);
	}

	@Override
	public void render() {
		// Pass in the position information
		vertexData.position(POS_OFFSET);
        GLES20.glVertexAttribPointer(positionHandle, POS_SIZE, GLES20.GL_FLOAT, false,
        		STRIDE, vertexData);        
                
        GLES20.glEnableVertexAttribArray(positionHandle);        
        
        // Pass in the color information
        vertexData.position(COLOR_OFFSET);
        GLES20.glVertexAttribPointer(colorHandle, COLOR_SIZE, GLES20.GL_FLOAT, false,
        		STRIDE, vertexData);        
        
        GLES20.glEnableVertexAttribArray(colorHandle);
        
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3); 
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resize(int h, int w) {
		// TODO Auto-generated method stub
	}
	
	
	@Override
	public int getMVPMatrixHandle() {
		return mvpMatrixHandle;
	}

	@Override
	public void useProgram() {
		program.load();
	}
}
