package com.szofttech.snake;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.os.Bundle;


public class GameScreen extends Activity {	
	private GLSurfaceView openglSurface;
	private GameController gc;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		
		Game game=new Game();
		game.networkManager=new DummyNetworkManager();
		game.isServer=true;
		game.renderer=new SnakeRenderer(getBaseContext());
		game.settings=new GameSettings();
		game.settings.height=24;
		game.settings.width=40;
		game.settings.stepTime=1000;
		game.context=this;
		
		
		
		

		//CoordinateManager.getInstance().resizeMap(36,60);
		//CoordinateManager.getInstance().resizeMap(48,80);
		//CoordinateManager.getInstance().resizeMap(96,160);
		//CoordinateManager.getInstance().resizeMap(10,16);
		
		/*SnakeRenderer renderer=new SnakeRenderer(getBaseContext());
	
		Grid g=new Grid(getBaseContext());
		g.setColor(Color.GRAY);
		
		Fruit c=new Fruit(getBaseContext());
		c.setPosition(10, 5);
		
		Skull c2=new Skull(getBaseContext());
		c2.setPosition(3, 5);
		
		Star c3=new Star(getBaseContext());
		c3.setPosition(7, 3);
		
		
		
		Snake s=new Snake(getBaseContext());
		s.addPoint(new Point(1,1));
		s.addPoint(new Point(2,1));
		s.addPoint(new Point(3,1));
		s.setColor(Color.RED);
		
		Snake s2=new Snake(getBaseContext());
		s2.addPoint(new Point(15,5));
		s2.addPoint(new Point(15,6));
		s2.addPoint(new Point(15,7));
		s2.addPoint(new Point(15,8));
		s2.addPoint(new Point(16,8));
		s2.setColor(Color.BLUE);
		
		renderer.addRenderable(g);
		renderer.addRenderable(c);
		renderer.addRenderable(c2);
		renderer.addRenderable(c3);
		renderer.addRenderable(s);
		renderer.addRenderable(s2);*/

		openglSurface = new GLSurfaceView(this);
		openglSurface.setEGLContextClientVersion(2);
		//openglSurface.setEGLConfigChooser(new MultisampleConfigChooser());
		openglSurface.setRenderer(game.renderer);

		setContentView(openglSurface);
		
		gc=new GameController(game);
		gc.start();
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

	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    
	    gc.stopMe();
	    gc=null;
	    openglSurface=null;
	}
	

	
}
