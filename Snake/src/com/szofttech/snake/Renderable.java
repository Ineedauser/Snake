package com.szofttech.snake;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.content.Context;
import android.opengl.Matrix;

public abstract class Renderable{
	public long glSyncTime;
	
	protected final int BYTES_PER_FLOAT = 4;
	
	protected final Context appContext;
	
	protected float[] modelMatrix = new float[16];
	
	public Renderable(final Context appContext){
		this.appContext=appContext;
		glSyncTime=0;
		Matrix.setIdentityM(modelMatrix, 0);
	}	
	
	FloatBuffer createFloatBufferFromData(final float[] data){
		FloatBuffer result=ByteBuffer.allocateDirect(data.length * BYTES_PER_FLOAT)
		        .order(ByteOrder.nativeOrder()).asFloatBuffer();
		result.put(data).position(0);
		
		return result;
	}
	
	public abstract void init();
	public abstract void useProgram();	
	public abstract void renderPrepare(long time);
	public abstract void render();
	
	public abstract void resize(int h, int w);
	
	public float [] getModelMatrix(){
		return modelMatrix;
	}
	
	public abstract int getMVPMatrixHandle();

}
