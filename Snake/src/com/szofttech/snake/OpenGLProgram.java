package com.szofttech.snake;

import android.content.Context;
import android.opengl.GLES20;

public class OpenGLProgram {
	private int programHandle;
	
	private Shader fragmentShader;
	private Shader vertexShader;
	
	OpenGLProgram(final Context appContext, int vertexShaderResource, int fragmentShaderResource){
		fragmentShader=new Shader(GLES20.GL_FRAGMENT_SHADER, appContext, fragmentShaderResource);
		vertexShader=new Shader(GLES20.GL_VERTEX_SHADER, appContext, vertexShaderResource);
			
		
		// Create a program object and store the handle to it.
		programHandle = GLES20.glCreateProgram();	 
		if (programHandle != 0){
		    GLES20.glAttachShader(programHandle, vertexShader.getHandle());
		    GLES20.glAttachShader(programHandle, fragmentShader.getHandle());
		 
		 
		    GLES20.glLinkProgram(programHandle);
		 
		    final int[] linkStatus = new int[1];
		    GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);
		 
		    if (linkStatus[0] == 0){
		        GLES20.glDeleteProgram(programHandle);
		        programHandle = 0;
		    }    
		}
		
		if (programHandle==0){
			throw new RuntimeException("Failed to link shader program");
		}
	}
	
	int getAttributeLocation(final String name){
		return GLES20.glGetAttribLocation(programHandle, name);
	}
	
	int getUniformLocation(final String name){
		return GLES20.glGetUniformLocation(programHandle, name);
	}
	
	void load(){
		GLES20.glUseProgram(programHandle);
	}
}
