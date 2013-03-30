package com.szofttech.snake;

import java.util.ArrayList;

import com.szofttech.snake.Snake.Direction;

/**
 * Dummy network emulator for single player mode.
 * 
 */
public class DummyNetworkManager extends NetworkManager {
	private Direction lastDirection;
	
	@Override
	public void putLocalDirection(Direction direction) {
		lastDirection=direction;

	}

	@Override
	public void getSnakeDirections(Direction[] destionation) {
		if (destionation.length!=1)
			throw new IllegalArgumentException("The length of the destination buffer must be equal to the number of users!");
		
		destionation[0]=lastDirection;
	}

	@Override
	public long getFrameStartTimeInMills() {
		return System.currentTimeMillis();
	}

}
