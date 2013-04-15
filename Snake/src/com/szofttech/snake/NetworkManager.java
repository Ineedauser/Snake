package com.szofttech.snake;

public interface NetworkManager {
	public abstract void getSnakeDirections(Snake.Direction [] destionation);
	public abstract void putLocalDirection(Snake.Direction direction);
	
	public abstract long getFrameStartTimeInMills();
	
	public abstract void putNewObjects(NewObjectPlacement... object);
	public abstract ObjectPlacementList getNewObjects();
	
	public abstract User [] getUserList();
	public abstract int getUsetCount();
	
	public abstract boolean [] getErrorList();
	
	public abstract void setLocalUserData(String name, int color);
	public abstract boolean waitForGameStart(long timeoutInMills);
	public abstract void startLocalGame();
}
