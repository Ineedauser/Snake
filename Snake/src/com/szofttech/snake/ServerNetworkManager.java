package com.szofttech.snake;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;

import com.szofttech.snake.Snake.Direction;

public class ServerNetworkManager implements NetworkManager {
	private final String TAG="Snake.ServerNetworkManager";
	public volatile SnakeBluetoothSocket []clientSockets;
	public volatile boolean []socketError;
	public static final long MAX_DIRECTION_TIMEOUT=1000;
	
	public static enum FlagPacket{NEW_TIMEFRAME, END_GAME}; 
	
	void setErrorState(final int id){
		Log.w(TAG, "Setting error state for socket "+id);
		
		socketError[id]=true;

		receiveThreads[id].stopMe();
		sendThreads[id].stopMe();
		
		try {
			clientSockets[id].close();
		} catch (IOException e) {
		}
	}
	
	/**
	 * This is thread for receiving packets from clients.
	 * 
	 * @author xdever
	 *
	 */
	private class ReceiveThread extends Thread{
		private static final String TAG="Snake.ServerNetworkManager.ReceiveThread";
		
		
		private boolean running;
		private final int id;
		
		
		public ReceiveThread(final int id){
			running=true;
			this.id=id;
		}
		
		public void stopMe(){
			running=false;
		}
		
		public void setErrorState(){
			stopMe();
		}
		
		private void broadcastExceptMe(Object o){
			synchronized (users){
				for (int a=0; a<userCount; a++){
					if (a!=id){
						sendThreads[a].add(o);
					}
				}
			}
		}
		
		private void processNewUserPacket(UserRegisterPacket userPacket){
			synchronized (users){
				users[id]=new User();
				users[id].name=userPacket.name;
				users[id].color=userPacket.color;
				users[id].score=0;					
				Log.w(TAG, "User data received about user "+id+" with name: \""+users[id].name+"\"");
			}
			
			broadcastExceptMe(userPacket);
		}
		
		private void processDirectionPacket(Direction d){
			synchronized (lastDirections){
				lastDirections[id]=d;
				directionUpdated[id]=true;
				lastDirections.notify();
			}
			
			
			SnakeMovementPacket movement=new SnakeMovementPacket();
			movement.id=id;
			movement.direction=d;
			broadcastExceptMe(movement);
		}
		
		private Object receiveObject(){
			Object packet=null;
			
			try {
				packet=clientSockets[id].read();
			} catch (IOException e) {
				Log.w(TAG, "I/O Exception reading socket");
				setErrorState();
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("Class not found exception on network socket");
			}
			
			return packet;
		}
		
		@Override
		public void run(){
			/**
			 * Process packets received from client.
			 * 
			 */
			while (running){
				Object packet=receiveObject();
				
				if (packet==null)
					break;		
				
				if (packet instanceof UserRegisterPacket){
					processNewUserPacket((UserRegisterPacket)packet);
				} else if (packet instanceof Snake.Direction){
					processDirectionPacket((Snake.Direction)packet);
				}
			}
		}
	}
	
	/**
	 * This is a simple thread plus an object queue for sending packets to
	 * clients.
	 * 
	 * @author xdever
	 *
	 */
	private class SendThread extends Thread{
		
		private static final String TAG="Snake.ServerNetworkManager.SendThread";
		
		private boolean running;
		private final int id;
		
		private LinkedList<Object> sendList;
		
		
		public SendThread(final int id){
			running=true;
			this.id=id;
		}
		
		public void stopMe(){
			running=false;
		}
		
		public void add(Object o){
			if (!socketError[id]){
				synchronized (sendList){
					sendList.add(o);
					sendList.notify();
				}
			}
		}
		
		private void assignSnakeID(){
			LoginPacket p=new LoginPacket();
			p.id=id;
			
			/**
			 * Send login packet.
			 * 
			 */
			try {
				clientSockets[id].write(p);
				clientSockets[id].flush();
			} catch (IOException e1) {
				Log.w(TAG, "I/O Exception reading socket");
				setErrorState(id);
			}
		}
		
		@Override
		public void run(){
			assignSnakeID();
			
			/**
			 * Process packets received from client.
			 * 
			 */
			while (running){
				Object packet;
				
				synchronized (sendList){
					while (running && sendList.isEmpty()){
						try {
							sendList.wait(100);
						} catch (InterruptedException e) {
						}
					}
					
					if (!running){
						return;
					}
					
					packet=sendList.pop();
				}
				
				try {
					clientSockets[id].write(packet);
					clientSockets[id].flush();
				} catch (IOException e) {
					Log.w(TAG, "I/O error writing socket for user "+id);
					setErrorState(id);
				}
			}
		}
	}
	
	
	
	private volatile ReceiveThread [] receiveThreads;
	private volatile SendThread [] sendThreads;
	
	private volatile User [] users;
	private volatile Direction [] lastDirections;
	private volatile boolean [] directionUpdated;
	private volatile int userCount=1;
	private ObjectPlacementList newObjectList;
	
	private Timer gameTimer;
	private volatile long frameStartTime;
	
	
	
	
	public ServerNetworkManager(){
		newObjectList=new ObjectPlacementList();
		receiveThreads=new ReceiveThread[BluetoothServer.MAX_CONNECTIONS];
		sendThreads=new SendThread[BluetoothServer.MAX_CONNECTIONS];
		users=new User[BluetoothServer.MAX_CONNECTIONS];
		lastDirections=new Direction[BluetoothServer.MAX_CONNECTIONS];
		directionUpdated=new boolean[BluetoothServer.MAX_CONNECTIONS];
			
		for (int a=0; a<BluetoothServer.MAX_CONNECTIONS; a++){
			users[a]=null;
			directionUpdated[a]=false;
		}
		
		gameTimer=new Timer();
	}
	
	private void broadcast(Object o){
		synchronized (users){
			for (int a=1; a<userCount; a++){
				if (users[a]!=null && socketError[a]==false){
					sendThreads[a].add(o);
				}
			}
		}
	}
	
	
	private void processDirectionsOnTimeframeEnd(){
		synchronized (lastDirections){
			for (int a=0; a<lastDirections.length; a++){
				if (directionUpdated[a]==false){
					setErrorState(a);
					Log.w(TAG, "Socket "+a+" lost because of timeout.");
				} else
					directionUpdated[a]=false;
			}
		}
	}
	
	public void startGame(long stepTime){
		frameStartTime=0;
		gameTimer.scheduleAtFixedRate(new TimerTask(){

			@Override
			public void run() {
				if (frameStartTime!=0){
					waitForDirections(MAX_DIRECTION_TIMEOUT);
					processDirectionsOnTimeframeEnd();
				}
				frameStartTime=System.currentTimeMillis();
				
				broadcast(FlagPacket.NEW_TIMEFRAME);
			}
			
		}, 0, stepTime);
	}
	
	public void stopGame(){
		gameTimer.cancel();
		gameTimer.purge();
		
		broadcast(FlagPacket.END_GAME);
	}
	
	private boolean waitForDirections(long timeout){
		long endTime=System.currentTimeMillis()+timeout;
		
		while (endTime>System.currentTimeMillis()){
			boolean notReceived=false;
			synchronized (lastDirections){
				for (int a=0; a<lastDirections.length; a++){
					if (directionUpdated[a]==false){
						notReceived=true;
						break;
					}
				}
			}
			
			
			if (notReceived){
				try {
					lastDirections.wait(endTime-System.currentTimeMillis());
				} catch (InterruptedException e) {
				}
			} else
				return true;			
		}
			
		return false;
	}
	
	public void registerClient(SnakeBluetoothSocket socket){
		ReceiveThread tr;
		SendThread ts;
		
		
		synchronized(users){
			int id=userCount;
			userCount++;
			
			tr=new ReceiveThread(id);
			ts=new SendThread(id);
			
			receiveThreads[id]=tr;
			sendThreads[id]=ts;
			
			socketError[id]=false;
		}
		
		tr.start();
		ts.start();
	}
	
	@Override
	public void getSnakeDirections(Direction[] destionation) {
		synchronized (lastDirections){
			for (int a=0; a<userCount; a++)
				destionation[a]=lastDirections[a];
		}
	}

	@Override
	public void putLocalDirection(Direction direction) {
		synchronized (lastDirections){
			lastDirections[0]=direction;
			directionUpdated[0]=true;
		}		
	}

	@Override
	public long getFrameStartTimeInMills() {
		return frameStartTime;
	}

	@Override
	public void putNewObjects(NewObjectPlacement... object) {
		for (int b=0; b<object.length; b++)
			broadcast(object[b]);
		
		synchronized (newObjectList){
			for (int b=0; b<object.length; b++)
				newObjectList.add(object[b]);
		}
		
	}

	@Override
	public ObjectPlacementList getNewObjects() {
		return newObjectList;		
	}

	@Override
	public User[] getUserList() {
		return users;
	}

}
