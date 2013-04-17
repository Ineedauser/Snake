package com.szofttech.snake;

import java.util.LinkedList;

import android.graphics.Color;

import com.szofttech.snake.Snake.Direction;

/**
 * Dummy network emulator for single player mode.
 * 
 */
public class DummyNetworkManager implements NetworkManager {
	private Direction lastDirection;
	private LinkedList<NewObjectPlacement> objects;
	private User [] users;
	private boolean [] socketError;
	private long frameStartTime;
	
	private int gameTime=0;
	
	public DummyNetworkManager(){
		objects = new LinkedList<NewObjectPlacement>();
		users = new User[1];
		users[0]= new User();
		
		users[0].name="Local";
		users[0].color=Color.RED;
		
		lastDirection=Direction.UNCHANGED;
		
		socketError=new boolean[1];
		socketError[0]=false;
	}
	
	
	@Override
	public void putLocalDirection(Direction direction) {
		lastDirection=direction;

	}

	@Override
	public void getSnakeDirections(Direction[] destionation) {
		if (destionation.length!=1)
			throw new IllegalArgumentException("The length of the destination buffer must be equal to the number of users!");
		
		destionation[0]=lastDirection;
		lastDirection=Direction.UNCHANGED;
	}

	@Override
	public void putNewObjects(NewObjectPlacement... object) {
		for (int a=0; a<object.length; a++)
			objects.add(object[a]);		
	}

	@Override
	public LinkedList<NewObjectPlacement> getNewObjects() {
		return objects;
	}


	@Override
	public User[] getUserList() {
		return users;
	}


	@Override
	public boolean[] getErrorList() {
		return socketError;
	}


	@Override
	public int getUsetCount() {
		return 1;
	}


	@Override
	public void setLocalUserData(String name, int color) {
		synchronized (users){
			users[0].name=name;
			users[0].color=color;
		}
		
	}


	@Override
	public boolean waitForGameStart(long timeoutInMills) {
		return true;
	}


	@Override
	public void startLocalGame() {
		frameStartTime=System.currentTimeMillis();
	}


	@Override
	public boolean waitForFrameEnd(long timeoutInMills) {
		long endTime=frameStartTime+Game.getInstance().settings.stepTime;
		long now=System.currentTimeMillis();
		boolean result;
		
		if (endTime<now+timeoutInMills){
			result=true;
		} else {
			endTime=now+timeoutInMills;
			result=false;
		}
		
		
		while (true){
			now=System.currentTimeMillis();
			if (now >= endTime)
				break;
			else {
				try {
					Thread.sleep(endTime-now);
				} catch (InterruptedException e) {
				}
			}
		}
		
		if (result){
			frameStartTime=now+Game.getInstance().settings.stepTime;
			gameTime++;
		}
		return result;
		
	}


	@Override
	public int getGameTime() {
		return gameTime;
	}
}
