package com.szofttech.snake;

import java.util.ArrayList;
import java.util.Iterator;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

public class SnakeRenderer implements GLSurfaceView.Renderer {
	/**
	 * Store the view matrix. This can be thought of as our camera. This matrix transforms world space to eye space;
	 * it positions things relative to our eye.
	 */
	private float[] mViewMatrix = new float[16];

	/** Store the projection matrix. This is used to project the scene onto a 2D viewport. */
	private float[] mProjectionMatrix = new float[16];
	
	/** Allocate storage for the final combined matrix. This will be passed into the shader program. */
	private float[] mMVPMatrix = new float[16];
		
	private ArrayList<Renderable> objectList;
	
	private final Context appContext;
	
	private long glSyncTime=0;
	
	
	
	
	public void addRenderable(Renderable object){
		synchronized(objectList){
			objectList.add(object);
		}
	}
	
	public void removeRenderable(Renderable object){
		synchronized(objectList){
			objectList.remove(object);
		}
	}
		
	public SnakeRenderer(final Context appContext){
		objectList=new ArrayList<Renderable>();
		this.appContext=appContext;
	}
	
	@Override
	public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
		// Set the background clear color to gray.
		GLES20.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);
	
		// Position the eye behind the origin.
		final float eyeX = 0.0f;
		final float eyeY = 0.0f;
		final float eyeZ = 1.0001f;

		// We are looking toward the distance
		final float lookX = 0.0f;
		final float lookY = 0.0f;
		final float lookZ = -5.0f;

		// Set our up vector. This is where our head would be pointing were we holding the camera.
		final float upX = 1.0f;
		final float upY = 0.0f;
		final float upZ = 0.0f;

		// Set the view matrix. This matrix can be said to represent the camera position.
		// NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
		// view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
		Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

		TextureTable.init(appContext);
		glSyncTime++;
		synchronized(objectList){
			Iterator<Renderable> itr = objectList.iterator();
			while(itr.hasNext()){
				Renderable obj=itr.next();
				obj.init();
				obj.glSyncTime=glSyncTime;
			}
		}
        
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND);
	}	
	
	@Override
	public void onSurfaceChanged(GL10 glUnused, int width, int height) 
	{
		// Set the OpenGL viewport to the same size as the surface.
		GLES20.glViewport(0, 0, width, height);
		
		CoordinateManager.getInstance().resizeScreen(height, width);

		// Create a new perspective projection matrix. The height will stay the same
		// while the width will vary as per aspect ratio.
		//final float ratio = (float) width / height;
	/*	final float left = -ratio;
		final float right = ratio;
		final float bottom = -100.0f;
		final float top = 100.0f;
		final float near = 1.0f;
		final float far = 10.0f;*/
		
		RectF screenBoundaries=CoordinateManager.getInstance().getOpenGLBoundaries();
		
		//left
		final float bottom = screenBoundaries.left;
		//right
		final float top = screenBoundaries.right;
		//bottom
		final float left = screenBoundaries.bottom;
		//top
		final float right = screenBoundaries.top;
		
		
		final float near = 1.0f;
		final float far = 10.0f;
		
		Log.wtf("SNAKE", "Resizing: W="+width+" H="+height);
		
		Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
		
		
		
		synchronized(objectList){
			Iterator<Renderable> itr = objectList.iterator();
			while(itr.hasNext()){
				Renderable obj=itr.next();
				obj.resize(height, width);
			}
		}
	}	

	@Override
	public void onDrawFrame(GL10 glUnused) 
	{
		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT );
		
		// GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
         //        GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
                
        // Do a complete rotation every 10 seconds.
        long time = SystemClock.uptimeMillis();
        
        synchronized(objectList){
	        Iterator<Renderable> itr = objectList.iterator();
	        while(itr.hasNext()){
	        	Renderable obj=itr.next();
	        	
	        	if (obj.glSyncTime!=glSyncTime){
					obj.init();
					obj.glSyncTime=glSyncTime;
				}
	        	
	        	obj.useProgram();
	        	
	        	int mvpHandle=obj.getMVPMatrixHandle();
	        	
	        	obj.renderPrepare(time);
	        	
	    		Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, obj.getModelMatrix(), 0);
	    		Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
	    		GLES20.glUniformMatrix4fv(mvpHandle, 1, false, mMVPMatrix, 0);
	    		
	        	obj.render(); 
	        }
        }
	}
}
