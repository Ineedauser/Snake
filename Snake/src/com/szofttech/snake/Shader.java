package com.szofttech.snake;

import java.io.IOException;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

public class Shader {
	
	private int shaderHandle=0;
	
	public Shader(int type, final String source){
		buildShader(type, source);
	}
	
	private void buildShader(int type, final String source){
		shaderHandle = GLES20.glCreateShader(type);

		if (shaderHandle != 0){
			GLES20.glShaderSource(shaderHandle, source);
			GLES20.glCompileShader(shaderHandle);
			// Get the compilation status.
			final int[] compileStatus = new int[1];
			GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
	
			// If the compilation failed, delete the shader.
			if (compileStatus[0] == 0){		
				String compilationLog= GLES20.glGetShaderInfoLog(shaderHandle);
				switch (type){
					case (GLES20.GL_FRAGMENT_SHADER):
						Log.e("Snake: Failed to compile FRAGMENT Shader: ",compilationLog);
						break;
					case (GLES20.GL_VERTEX_SHADER):
						Log.e("Snake: Failed to compile VERTEX Shader: ",compilationLog);
						break;
					default:
						Log.e("Snake: Failed to compile UNKNOWN Shader: ",compilationLog);
				}
				GLES20.glDeleteShader(shaderHandle);
				shaderHandle = 0;
			}
		}
	
		if (shaderHandle == 0){
			throw new RuntimeException("Error creating shader.");
		}
	}
	
	Shader(int type, final Context appContext, final int resourceId){
		String source;
		try {
			source = Helpers.readTextFileFromRawResource(appContext, resourceId);
		} catch (IOException e) {
			throw new RuntimeException("Error loading shader from resource.");			
		}
		
		buildShader(type, source);
	}
	
	
	int getHandle(){
		return shaderHandle;
	}
}

