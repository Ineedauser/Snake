package com.szofttech.snake;

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
		
		//CoordinateManager.getInstance().resizeMap(10,16);
		
		SnakeRenderer renderer=new SnakeRenderer(getBaseContext());
	
		Grid g=new Grid(getBaseContext());
		g.setColor(Color.GRAY);
		
		Collectable c=new Collectable(getBaseContext());
		c.setPosition(10, 5);
		
		renderer.addRenderable(g);
		renderer.addRenderable(c);

		openglSurface = new GLSurfaceView(this);
		openglSurface.setEGLContextClientVersion(2);
		//openglSurface.setEGLConfigChooser(new MultisampleConfigChooser());
		openglSurface.setRenderer(renderer);

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
