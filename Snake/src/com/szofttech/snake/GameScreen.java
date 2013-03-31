package com.szofttech.snake;

import android.app.Activity;
import android.os.Bundle;


public class GameScreen extends Activity {	
	private SnakeView snakeView;
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
		//game.settings.height=10;
		//game.settings.width=15;
		game.settings.stepTime=200;
		game.settings.starProbability=0.005f;
		game.settings.skullProbability=0.01f;
		game.context=this;

		
		snakeView=new SnakeView(game);
		setContentView(snakeView);
		
		gc=new GameController(game);
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
