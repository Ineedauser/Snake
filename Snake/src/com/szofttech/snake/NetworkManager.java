package com.szofttech.snake;

import java.util.ArrayList;

public abstract class NetworkManager {
	public abstract void getSnakeDirections(Snake.Direction [] destionation);
	public abstract void putLocalDirection(Snake.Direction direction);
	public abstract long getFrameStartTimeInMills();
}
