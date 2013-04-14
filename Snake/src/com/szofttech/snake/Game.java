package com.szofttech.snake;

import android.content.Context;

public class Game {
	private static Game instance=null;
	
	private Game(){
		settings=new GameSettings();
	}
	
	public static Game getInstance(){
		if (instance==null)
			instance=new Game();
		return instance;
	}
	
	public SnakeRenderer renderer;
	public NetworkManager networkManager;
	public GameSettings settings;
	public Context context;
	boolean isServer;
}
