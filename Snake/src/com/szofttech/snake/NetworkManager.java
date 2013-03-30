package com.szofttech.snake;

import java.util.LinkedList;

public abstract class NetworkManager {
	public abstract void getSnakeDirections(Snake.Direction [] destionation);
	public abstract void putLocalDirection(Snake.Direction direction);
	
	public abstract long getFrameStartTimeInMills();
	
	public abstract void putNewObject(NewObjectPlacement object);
	public abstract void getNewObjects(LinkedList<NewObjectPlacement> objects);
	
	public abstract User [] getUserList();
}
