package com.szofttech.snake;

import java.util.Random;

import android.graphics.Color;
import android.graphics.Point;
import android.util.Log;

public class GameController extends Thread{
	private final Game game;
	
	private Snake [] snakes;
	private User [] users;
	private Snake.Direction [] snakeDirections;
	private CollectableList collectables;
	private ObjectPlacementList newObjects;
	
	
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
		newObjects=new ObjectPlacementList();
		
		
		CoordinateManager.getInstance().resizeMap(game.settings.height,game.settings.width);
		
		Grid g=new Grid(game.context);
		g.setColor(Color.GRAY);
		game.renderer.addRenderable(g);
		users=game.networkManager.getUserList();
		
		snakes=new Snake[users.length];
		snakeDirections=new Snake.Direction[users.length];
		for (int a=0; a<snakes.length; a++){
			snakes[a]=new Snake(game.context);
			snakes[a].setColor(users[a].color);
			snakes[a].setDead(true);
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
		} while (getObjectDistances(p)<=minSquredDistance);
		
		ObjectPool.getInstance().putPoint(mapSize);
		
		return p;
	}
	
	private void generatePlacements(){
		if (game.isServer==false)
			return;
		
		for (int a=0; a<snakes.length; a++){
			if (snakes[a].isDead()){
				Point p=getRandomPlacement(3*3);
				
				int dx=0;
				int dy=0;
				
				if (random.nextInt(2)==0){
					dx=random.nextInt(2)*2-1;
				} else
					dy=random.nextInt(2)*2-1;
				
				NewObjectPlacement p1=ObjectPool.getInstance().getNewObjectPlacement();
				NewObjectPlacement p2=ObjectPool.getInstance().getNewObjectPlacement();
				
				
				p1.position.set(p.x, p.y);
				p1.user=a;
				p1.type=NewObjectPlacement.Type.SNAKE;
				
				p2.position.set(p.x+dx, p.y+dy);
				p2.user=a;
				p2.type=NewObjectPlacement.Type.SNAKE;
				
				game.networkManager.putNewObjects(p1,p2);
				
				ObjectPool.getInstance().putPoint(p);
				
				snakes[a].setDead(false);
			}
		}
	}	
	
	
	void mergeNewObjects(){
		game.networkManager.getNewObjects(newObjects);
		
		while (!newObjects.isEmpty()){
			NewObjectPlacement o=newObjects.pop();
			
			switch (o.type){
				case SNAKE:
					snakes[o.user].addPoint(o.position);
			}
			
			ObjectPool.getInstance().putNewObjectPlacement(o);
		}
	}
	
	void moveSnakes(){
		game.networkManager.getSnakeDirections(snakeDirections);
		for (int a=0; a<snakes.length; a++){
			snakes[a].move(snakeDirections[a], false);
		}
	}
	
	@Override
    public void run(){
		generatePlacements();
		mergeNewObjects();
		
		while (running){
			
			long endTime=game.networkManager.getFrameStartTimeInMills()+game.settings.stepTime;
			
			while (true){
				long time=System.currentTimeMillis();
				
				if (time>=endTime){
					break;
				}
				
				try {
					Thread.sleep(endTime-time);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			generatePlacements();
			mergeNewObjects();
			moveSnakes();
		}
    }
}
