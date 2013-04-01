package com.szofttech.snake;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class NetworkPacket implements Externalizable{
	
	public static enum Type {LOGIN, USER, NEW_OBJECT, MOVEMENT};

	public Type type;
	
	
	@Override
	public void readExternal(ObjectInput stream) throws IOException, ClassNotFoundException {
		Object temp=stream.readObject();
		if (!(temp instanceof Type)){
			throw new ClassNotFoundException();
		}
		type=(Type)temp;
		
	}

	@Override
	public void writeExternal(ObjectOutput stream) throws IOException {
		stream.writeObject(type);
	}

}
