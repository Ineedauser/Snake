package com.szofttech.snake;

import java.io.IOException;

import android.util.Log;

import com.szofttech.snake.Snake.Direction;

public class ClientNetworkManager implements NetworkManager {
	private static final String TAG="Snake.NetworkManager: ";
	
	
	private BluetoothClientSocket socket;
	private ReceiveThread thread;
	
	public boolean error; 
	
	private class ReceiveThread extends Thread{
		private boolean running;

		public ReceiveThread(){
			running=true;
		}
		
		
		public void stopMe(){
			running=false;
		}
		
		@Override
		public void run(){
			try {
				NetworkPacket packet=(NetworkPacket)socket.read();
			} catch (IOException e) {
				Log.e(TAG, "I/O Exception reading socket");
				error=true;
			} catch (ClassNotFoundException e) {
				Log.e(TAG, "ClassNotFoundException reading socket");
				error=true;
			}
						
		}
	}
	
	public void stop(){
		if (thread!=null){
			thread.stopMe();
			
			try {
				this.socket.close();
			} catch (IOException e) {
			}
			
			try {
				thread.join();
			} catch (InterruptedException e1) {
			}
			
		}
	}
	
	public void login(BluetoothClientSocket socket){
		stop();
		
		error=false;
		
		this.socket=socket;
		thread=new ReceiveThread();
		thread.start();
	}
	
	public ClientNetworkManager(){
		socket=null;
		thread=null;
	}

	@Override
	public void getSnakeDirections(Direction[] destionation) {
		// TODO Auto-generated method stub

	}

	@Override
	public void putLocalDirection(Direction direction) {
		// TODO Auto-generated method stub

	}

	@Override
	public long getFrameStartTimeInMills() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void putNewObjects(NewObjectPlacement... object) {
		// TODO Auto-generated method stub

	}

	@Override
	public void getNewObjects(ObjectPlacementList objects) {
		// TODO Auto-generated method stub

	}

	@Override
	public User[] getUserList() {
		// TODO Auto-generated method stub
		return null;
	}

}
