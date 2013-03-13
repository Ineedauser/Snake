package com.szofttech.snake;

import javax.microedition.khronos.egl.EGL10;

import android.app.Activity;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.os.Bundle;


public class GameScreen extends Activity {	
	private GLSurfaceView openglSurface;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		CoordinateManager.getInstance().resizeMap(24,40);
		
		SnakeRenderer renderer=new SnakeRenderer();
		Grid g=new Grid(getBaseContext());
		g.setColor(Color.RED);
		renderer.addRenderable(g);
		

		openglSurface = new GLSurfaceView(this);
		openglSurface.setEGLContextClientVersion(2);
		//openglSurface.setEGLConfigChooser(new MultisampleConfigChooser());
		openglSurface.setRenderer(renderer);
		
		
		int[] configSpec = {
                EGL10.EGL_RED_SIZE, 5,
                EGL10.EGL_GREEN_SIZE, 6,
                EGL10.EGL_BLUE_SIZE, 5,
                EGL10.EGL_DEPTH_SIZE, 16,
                // Requires that setEGLContextClientVersion(2) is called on the view.
                EGL10.EGL_RENDERABLE_TYPE, 4 /* EGL_OPENGL_ES2_BIT */,
                EGL10.EGL_SAMPLE_BUFFERS, 1 /* true */,
                EGL10.EGL_SAMPLES, 2,
                EGL10.EGL_NONE
        };


		setContentView(openglSurface);
	}
	
	
	@Override
	protected void onResume() 
	{
		// The activity must call the GL surface view's onResume() on activity onResume().
		super.onResume();
		openglSurface.onResume();
	}

	@Override
	protected void onPause() 
	{
		// The activity must call the GL surface view's onPause() on activity onPause().
		super.onPause();
		openglSurface.onPause();
	}	


	
}
