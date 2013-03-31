package com.szofttech.snake;

import java.util.Random;

import android.graphics.Color;
import android.graphics.Point;

public class GameController extends Thread{
	private final Game game;
	
	private Snake [] snakes;
	private User [] users;
	private Snake.Direction [] snakeDirections;
	private CollectableList collectables;
	private ObjectPlacementList newObjects;
	private int [] deadSnakeDelays;
	private int [] skipSteps;
	private int fruitsNeeded;
	private boolean [] growSnakes;
	
	private static final int SNAKE_DEAD_DELAY_MS=2000;
	private static final int SNAKE_START_DELAY_MS=500;
	
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
		deadSnakeDelays=new int[users.length];
		skipSteps=new int[users.length];
		growSnakes=new boolean[users.length];
		fruitsNeeded=users.length;
		
		for (int a=0; a<snakes.length; a++){
			snakes[a]=new Snake(game.context);
			snakes[a].setColor(users[a].color);
			snakes[a].setDead(true);
			game.renderer.addRenderable(snakes[a]);
			
			deadSnakeDelays[a]=0;
			skipSteps[a]=0;
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
	
	private Point getWallNormalVectorNearPoint(Point pos){
		int x=0;
		int y=0;
		
		if (pos.x==0)
			x=1;
		else if (pos.y==0)
			y=1;
		else {
			Point mapSize=CoordinateManager.getInstance().getMapDimensions();
			
			if (pos.x==(mapSize.x-1))
				x=-1;
			else if (pos.y==(mapSize.y-1))
				y=-1;
			
			ObjectPool.getInstance().putPoint(mapSize);
		}
		
		if (x!=0 || y!=0){
			Point result=ObjectPool.getInstance().getPoint();
			result.x=x;
			result.y=y;
			return result;
		}
		
		return null;		
	}
	
	private void placeDeadSnakes(){
		for (int a=0; a<snakes.length; a++){
			if (snakes[a].isDead()){
				if (deadSnakeDelays[a]!=0)
					continue;
				
				Point p=getRandomPlacement(3*3);
				
				int dx=0;
				int dy=0;
				boolean swapPoints=false;
				
			
				if (random.nextBoolean()==false){
					dx=random.nextInt(2)*2-1;
				} else {
					dy=random.nextInt(2)*2-1;
				}
			
				Point wallNormal=getWallNormalVectorNearPoint(p);
				if (wallNormal!=null){
					if (dx!=0 && wallNormal.x!=0){
						dx=wallNormal.x;
						swapPoints=true;
					} else if (dy!=0 && wallNormal.y!=0){
						dy=wallNormal.y;
						swapPoints=true;
					}
										
					ObjectPool.getInstance().putPoint(wallNormal);					
				}
				
				NewObjectPlacement p1=ObjectPool.getInstance().getNewObjectPlacement();
				NewObjectPlacement p2=ObjectPool.getInstance().getNewObjectPlacement();
				
				
				p1.position.set(p.x, p.y);
				p1.user=a;
				p1.type=NewObjectPlacement.Type.SNAKE;
				
				p2.position.set(p.x+dx, p.y+dy);
				p2.user=a;
				p2.type=NewObjectPlacement.Type.SNAKE;
				

				if (swapPoints)
					game.networkManager.putNewObjects(p2,p1);
				else
					game.networkManager.putNewObjects(p1,p2);
				
				ObjectPool.getInstance().putPoint(p);
			}
		}
	}
	
	private void placeFruits(){
		for (; fruitsNeeded>0; fruitsNeeded--){
				Point p=getRandomPlacement(2*2);
				
				NewObjectPlacement fruit=ObjectPool.getInstance().getNewObjectPlacement();
				fruit.type=NewObjectPlacement.Type.FRUIT;
				fruit.user=0;
				fruit.position.set(p.x, p.y);
				
				game.networkManager.putNewObjects(fruit);
				ObjectPool.getInstance().putPoint(p);
		}
	}
	
	private void generatePlacements(){
		if (game.isServer==false)
			return;
		
		placeDeadSnakes();
		placeFruits();
	}	
	
	
	private void mergeNewObjects(){
		game.networkManager.getNewObjects(newObjects);
		
		while (!newObjects.isEmpty()){
			NewObjectPlacement o=newObjects.pop();
			
			switch (o.type){
				case SNAKE:
					snakes[o.user].addPoint(o.position);
					snakes[o.user].setDead(false);
					skipSteps[o.user]=SNAKE_START_DELAY_MS/game.settings.stepTime;
					break;
				case FRUIT:
					Fruit f=ObjectPool.getInstance().getFruit(game.context);
					f.setPosition(o.position);
					game.renderer.addRenderable(f);
					collectables.add(f);
					break;
				default:
					break;
			}
			
			ObjectPool.getInstance().putNewObjectPlacement(o);
		}
	}
	
	private void dieSnake(int index){
		snakes[index].setDead(true);
		deadSnakeDelays[index]=SNAKE_DEAD_DELAY_MS/game.settings.stepTime;
	}
	
	private void moveSnakes(){
		for (int a=0; a<snakes.length; a++){
			if (!snakes[a].isSnakeValid())
				continue;
			
			if (skipSteps[a]>0){
				skipSteps[a]--;
				continue;
			}
			
			snakes[a].move(snakeDirections[a], growSnakes[a]);
		}
	}
	
	private void wallDetect(){
		for (int a=0; a<snakes.length; a++){
			if (!snakes[a].isSnakeValid())
				continue;
			
			Point pos=snakes[a].getFuturePosition(snakeDirections[a]);
			if (!CoordinateManager.getInstance().isValidPosition(pos)){
				dieSnake(a);
			}
		}
	}
	
	private void collectableCollected(Collectable c){
		game.renderer.removeRenderable(c);
		collectables.remove(c);
		
		if (c instanceof Fruit)
			ObjectPool.getInstance().putFruit((Fruit)c);
	}
	
	
	private void collectableDetect(){
		for (int a=0; a<snakes.length; a++){
			growSnakes[a]=false;
			
			Point nextPos=snakes[a].getFuturePosition(snakeDirections[a]);
			Collectable collected=collectables.findByPos(nextPos);
			
			ObjectPool.getInstance().putPoint(nextPos);
			
			if (collected!=null){
				if (collected instanceof Fruit){
					growSnakes[a]=true;
					fruitsNeeded++;
				}
				
				collectableCollected(collected);
			}
			
		}
	}
	
	private void collisionDetect(){
		wallDetect();
		collectableDetect();
	}
	
	void updateDeadSnakeDelays(){
		for (int a=0; a<snakes.length; a++){
			if (deadSnakeDelays[a]==0)
				continue;
						
			deadSnakeDelays[a]=deadSnakeDelays[a]-1;
			if (deadSnakeDelays[a]==0){
				snakes[a].clearPoints();
			}
		}
	}
	
	private void waitForNewTimeframe(){
		long endTime=game.networkManager.getFrameStartTimeInMills()+game.settings.stepTime;
		
		while (true){
			long time=System.currentTimeMillis();
			
			if (time>=endTime){
				break;
			}
			
			try {
				Thread.sleep(endTime-time);
			} catch (InterruptedException e) {
			}
		}
	}
	
	@Override
    public void run(){
		generatePlacements();
		mergeNewObjects();
		
		while (running){
			waitForNewTimeframe();
		
			game.networkManager.getSnakeDirections(snakeDirections);
			updateDeadSnakeDelays();
			generatePlacements();
			mergeNewObjects();
			collisionDetect();
			moveSnakes();
		}
    }
}
