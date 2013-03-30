package com.szofttech.snake;

import android.graphics.Color;

public class GameController extends Thread{
	private final Game game;
	
	GameController(final Game game){
		this.game=game;
		
		Grid g=new Grid(game.context);
		g.setColor(Color.GRAY);
		game.renderer.addRenderable(g);
	}
	
	
	@Override
    public void run(){
		
    }
}
