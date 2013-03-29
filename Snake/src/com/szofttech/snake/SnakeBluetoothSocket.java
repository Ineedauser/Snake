package com.szofttech.snake;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothSocket;

public class SnakeBluetoothSocket {
	private BluetoothSocket socket;
	private InputStream inputStream;
	private OutputStream outputStream;
	
	SnakeBluetoothSocket(BluetoothSocket socket) throws IOException{
		this.socket=socket;
		inputStream=socket.getInputStream();
		outputStream=socket.getOutputStream();
	}
	
	void close() throws IOException{
		socket.close();
	}
	
	public void write(byte[] buffer) throws IOException{
		outputStream.write(buffer);
	}
	
	public int read(byte[] buffer) throws IOException{	
		return inputStream.read(buffer);
	}
}
