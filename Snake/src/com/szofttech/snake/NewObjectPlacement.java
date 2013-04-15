package com.szofttech.snake;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import android.graphics.Point;

/**
 * Used to transfer the placements of the new objects from the server to the client.
 *
 */
public class NewObjectPlacement implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 317402542575986365L;

	public static enum Type {SNAKE, FRUIT, STAR, SKULL};
	
	public Type type;
	public Point position=null;
	public int user;
	
	public NewObjectPlacement(){
		position=new Point();
	}
	
	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.writeObject(type);
		oos.writeInt(user);
		oos.writeInt(position.x);
		oos.writeInt(position.y);
	}
 
	
	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		type=(Type)ois.readObject();
		user=ois.readInt();
		if (position==null)
			position=new Point();
		position.x=ois.readInt();
		position.y=ois.readInt();
    }
}
