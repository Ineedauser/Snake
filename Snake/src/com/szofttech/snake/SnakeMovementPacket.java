package com.szofttech.snake;

import java.io.Serializable;

public class SnakeMovementPacket  implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6854434916595804226L;
	public int id;
	public Snake.Direction direction;
}
