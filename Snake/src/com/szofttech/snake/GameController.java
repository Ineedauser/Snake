package com.szofttech.snake;

import java.util.Random;

import android.graphics.Color;
import android.graphics.Point;

public class GameController extends Thread{
	private final Game game;
	
	private Snake [] snakes;
	private User [] users;
	private CollectableList collectables;
	
	
	private Random random;
	
	private volatile boolean running;
	
	public void stopMe(){
		running=false;
	}
	
	public GameController(final Game game){
		this.game=game;
		running=true;
		
		random=new Random(System.currentTimeMillis());
		collectables=new CollectableList();
		
		Grid g=new Grid(game.context);
		g.setColor(Color.GRAY);
		game.renderer.addRenderable(g);
		users=game.networkManager.getUserList();
		snakes=new Snake[users.length];
		for (int a=0; a<snakes.length; a++){
			snakes[a]=new Snake(game.context);
			snakes[a].setColor(users[a].color);
			game.renderer.addRenderable(snakes[a]);
		}
	}
	
	private int getSnakeSquaredDistances(final Point p){
		int minDistance=Integer.MAX_VALUE;
		
		for (int a=0; a<snakes.length; a++){
			minDistance=Math.min(minDistance, snakes[a].getDistanceSquared(p));
		}
		
		return minDistance;
	}
	
	private int getObjectDistances(final Point p){
		return Math.min(getSnakeSquaredDistances(p), collectables.getDistanceSquared(p));
	}
	
	private Point getRandomPlacement(int minSquredDistance){
		Point p=ObjectPool.getInstance().getPoint();
		Point mapSize=CoordinateManager.getInstance().getMapDimensions();
		
		do{
			p.x=random.nextInt(mapSize.x);
			p.y=random.nextInt(mapSize.y);
		} while (getObjectDistances(p)>=minSquredDistance);
		
		ObjectPool.getInstance().putPoint(mapSize);
		
		return p;
	}
	
	private void generatePlacements(){
		if (game.isServer==false)
			return;
			
	}	
	
	@Override
    public void run(){
		while (running){
			generatePlacements();
			Thread.yield();
		}
    }
}
