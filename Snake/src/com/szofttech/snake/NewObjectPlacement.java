package com.szofttech.snake;

import android.graphics.Point;

/**
 * Used to transfer the placements of the new objects from the server to the client.
 *
 */
public class NewObjectPlacement {
	public static enum Type {Snake, Fruit, Star, Skull};
	
	public Type type;
	public Point position;
	public int user;
}
