package com.szofttech.snake;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class SnakeBluetoothSocket {
	private static final String TAG="Snake.SnakeBluetoothSocket";
	private BluetoothSocket socket;
	private ObjectInputStream inputStream;
	private ObjectOutputStream outputStream;
	
	SnakeBluetoothSocket(BluetoothSocket socket) throws IOException{
		this.socket=socket;

		Log.w(TAG,"Creating object output stream...");
		outputStream=new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
		
		outputStream.flush();
		
		Log.w(TAG,"Creating object input stream...");
		inputStream=new ObjectInputStream(socket.getInputStream());
		

		Log.w(TAG,"Done");
	}
	
	void close() throws IOException{
		socket.close();
	}
	
	public void write(byte[] buffer) throws IOException{
		outputStream.write(buffer);
	}
	
	public void write(Object ... objects) throws IOException{
		for (int a=0; a<objects.length; a++)
			outputStream.writeObject(objects[a]);
	}
	
	public int read(byte[] buffer) throws IOException{	
		return inputStream.read(buffer);
	}
	
	public Object read() throws IOException, ClassNotFoundException{	
		return inputStream.readObject();
	}
	
	public void flush() throws IOException{
		outputStream.flush();
	}
}
