package com.szofttech.snake;

import java.nio.FloatBuffer;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

public class Grid extends Renderable{
	
	Shader fragmentShader;
	Shader vertexShader;
	int programHandle;
	
	private FloatBuffer vertexData;
	
	int mvpMatrixHandle;
	int positionHandle;
	int colorHandle;
	
	private final int POS_OFFSET = 0;
	private final int COLOR_OFFSET = 3;
	private final int POS_SIZE = 3;
	private final int COLOR_SIZE = 4;
	private final int STRIDE = 7 * BYTES_PER_FLOAT;
	
	private final float[] triangle1VerticesData = {
			// X, Y, Z, 
			// R, G, B, A
            -0.5f, -0.25f, 0.0f, 
            1.0f, 0.0f, 0.0f, 1.0f,
            
            0.5f, -0.25f, 0.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            
            0.0f, 0.559016994f, 0.0f, 
            0.0f, 1.0f, 0.0f, 1.0f};

	public Grid(Context appContext) {
		super(appContext);
	}

	@Override
	public void render(long time) {
		float angleInDegrees = (360.0f / 10000.0f) * ((int) time);
        
        // Draw the triangle facing straight on.
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.rotateM(modelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);   
		
		
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
	
	protected void finalize() throws Throwable {
		if (programHandle!=0){
			GLES20.glDeleteProgram(programHandle);
		}
	}

	@Override
	public int getMVPMatrixHandle() {
		return mvpMatrixHandle;
	}

	@Override
	public void useProgram() {
		GLES20.glUseProgram(programHandle); 
	}

	@Override
	public void init() {
		fragmentShader=new Shader(GLES20.GL_FRAGMENT_SHADER, appContext, R.raw.passthrough_frag);
		vertexShader=new Shader(GLES20.GL_VERTEX_SHADER, appContext, R.raw.passthrough_vert);
		
		vertexData=createFloatBufferFromData(triangle1VerticesData);
		
		// Create a program object and store the handle to it.
		programHandle = GLES20.glCreateProgram();	 
		if (programHandle != 0){
		    GLES20.glAttachShader(programHandle, vertexShader.getHandle());
		    GLES20.glAttachShader(programHandle, fragmentShader.getHandle());
		 
		    GLES20.glBindAttribLocation(programHandle, 0, "a_Position");
		    GLES20.glBindAttribLocation(programHandle, 1, "a_Color");
		 
		    GLES20.glLinkProgram(programHandle);
		 
		    final int[] linkStatus = new int[1];
		    GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);
		 
		    if (linkStatus[0] == 0){
		        GLES20.glDeleteProgram(programHandle);
		        programHandle = 0;
		    }
		    
		    mvpMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVPMatrix");        
	        positionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position");
	        colorHandle = GLES20.glGetAttribLocation(programHandle, "a_Color");
		}
		
		if (programHandle==0){
			throw new RuntimeException("Failed to link shader program");
		}
	}
	


}
