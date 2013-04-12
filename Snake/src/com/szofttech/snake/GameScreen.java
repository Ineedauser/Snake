package com.szofttech.snake;

import android.app.Activity;
import android.os.Bundle;


public class GameScreen extends Activity {	
	private SnakeView snakeView;
	private GameController gc;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		Game.getInstance().renderer=new SnakeRenderer(getBaseContext());

		
		snakeView=new SnakeView(this);
		setContentView(snakeView);
		
		gc=new GameController();
		gc.start();
	}
	
	
	@Override
	protected void onResume() 
	{
		// The activity must call the GL surface view's onResume() on activity onResume().
		super.onResume();
		snakeView.onResume();
	}

	@Override
	protected void onPause() 
	{
		// The activity must call the GL surface view's onPause() on activity onPause().
		super.onPause();
		snakeView.onPause();
	}	

	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    
	    gc.stopMe();
	    gc=null;
	    snakeView=null;
	}
	

	
}
