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
	private ObjectPlacementList objects;
	private User [] users;
	private boolean [] socketError;
	
	public DummyNetworkManager(){
		objects = new ObjectPlacementList();
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
	public long getFrameStartTimeInMills() {
		return System.currentTimeMillis();
	}

	@Override
	public void putNewObjects(NewObjectPlacement... object) {
		for (int a=0; a<object.length; a++)
			objects.add(object[a]);		
	}

	@Override
	public ObjectPlacementList getNewObjects() {
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

}
