package com.szofttech.snake;

import java.io.IOException;
import java.util.LinkedList;

import android.util.Log;

import com.szofttech.snake.Snake.Direction;

public class ClientNetworkManager implements NetworkManager {
	private static final String TAG="Snake.ClientNetworkManager: ";
	private static final int TIMEOUT=1500;
	private static volatile Boolean idLock=true;
	private static volatile boolean idAssigned=false;
	private volatile boolean gameStarted=false;
	
	private void checkId(int id){
		if ((id<0) || (id>BluetoothServer.MAX_CONNECTIONS))
			throw new RuntimeException("User ID out of range!");
	}
	
	private class ReceiveThread extends Thread{
		private boolean running;

		public ReceiveThread(){
			running=true;
		}
		
		
		public void stopMe(){
			running=false;
		}
		
		private void setErrorState(){
			ClientNetworkManager.this.setErrorState(localId);
		}
		
		private Object receiveObject(){
			Object packet=null;
			
			try {
				packet=socket.read();
			} catch (IOException e) {
				Log.e(TAG, "I/O Exception reading socket");
				setErrorState();
			} catch (ClassNotFoundException e) {
				Log.e(TAG, "Class not found exception on network socket");
				setErrorState();
			}
			
			return packet;
		}
		
		private void handleNewUserPacket(UserRegisterPacket up){
			Log.w(TAG, "User data for user "+up.id+" received");
			checkId(up.id);
			
			synchronized(users){
				users[up.id].name=up.name;
				users[up.id].color=up.color;
				
				userCount=Math.max(userCount, up.id+1);
			}
		}
		
		private void handleSnakeMovementPacket(SnakeMovementPacket packet){
			synchronized (lastDirections){
				checkId(packet.id);
				lastDirections[packet.id]=packet.direction;
				directionUpdated[packet.id]=true;
				
				lastDirections.notifyAll();
			}
		}
		
		private void handleClientDisconnectPacket(ClientDisconnectPacket packet){
			ClientNetworkManager.this.setErrorState(packet.id);
		}
		
		private void handleServerFlagPacket(ServerNetworkManager.FlagPacket packet){
			switch (packet){
				case NEW_TIMEFRAME:
					synchronized (lastDirections){
						frameStartTime=System.currentTimeMillis();
						newTimeframe=true;
						directionSent=false;
						gameStarted=true;
						lastDirections.notifyAll();
					}
					break;
				case END_GAME:
					//TODO
					break;
			}
		}
		
		private void handleNewObjectPacket(NewObjectPlacement packet){
			synchronized (newObjectList){
				newObjectList.add(packet);
			}
		}
		
		private void handleSettingsPacket(GameSettings packet){
			Log.w(TAG, "Game settings packet received.");
			Game.getInstance().settings.copyFrom(packet);
		}
		
		private void handleLoginPacket(LoginPacket packet){
			Log.w(TAG, "Login packet received. Local id is "+packet.id);
			localId=packet.id;
			userCount=localId+1;
			synchronized(idLock){
				idAssigned=true;
				idLock.notify();
			}
		}
		
		@Override
		public void run(){
			while (running){
				Log.w(TAG, "Waiting for network packet...");
				Object packet=receiveObject();
				
				if (packet==null)
					return;
				
				if (packet instanceof LoginPacket){
					handleLoginPacket((LoginPacket)packet);
				} else if (packet instanceof UserRegisterPacket){
					handleNewUserPacket((UserRegisterPacket)packet);
				} else if (packet instanceof SnakeMovementPacket){
					handleSnakeMovementPacket((SnakeMovementPacket)packet);
				} else if (packet instanceof ClientDisconnectPacket){
					handleClientDisconnectPacket((ClientDisconnectPacket)packet);
				} else if (packet instanceof ServerNetworkManager.FlagPacket){
					handleServerFlagPacket((ServerNetworkManager.FlagPacket)packet);
				} else if (packet instanceof NewObjectPlacement){
					handleNewObjectPacket((NewObjectPlacement)packet);
				} else if (packet instanceof GameSettings){
					handleSettingsPacket((GameSettings)packet);
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
		
		private volatile boolean running;
		
		private LinkedList<Object> sendList;
		
		
		public SendThread(){
			running=true;
			sendList=new LinkedList<Object> ();
		}
		
		public void stopMe(){
			running=false;
		}
		
		public void add(Object o){
			if (!socketError[localId]){
				synchronized (sendList){
					sendList.add(o);
					sendList.notify();
				}
			}
		}
		
		private void sendDummyDirection(){
			SnakeMovementPacket packet=new SnakeMovementPacket();
			packet.direction=Snake.Direction.UNCHANGED;
			packet.id=localId;
			sendPacket(packet);	
			
			Log.w("SNAKE Client", "Sending dummy direction");
		}
		
		private void sendPacket(Object packet){
			if (packet!=null){
				try {
					socket.write(packet);
					socket.flush();
				} catch (IOException e) {
					Log.w(TAG, "I/O error writing socket");
					setErrorState(localId);
				}
			}
		}
		
		@Override
		public void run(){
			/**
			 * Process packets received from client.
			 * 
			 */
			while (running){
				Object packet=null;
				
				long endTime=frameStartTime+(int)(0.7*Game.getInstance().settings.stepTime);
				long currTime=System.currentTimeMillis();
				
				synchronized (lastDirections){
					if (currTime>=endTime){
						if (directionSent==false){
							directionSent=true;
							sendDummyDirection();
						}
					}
				}
				
				synchronized (sendList){
					if (sendList.isEmpty()){
						try {
							
							if (currTime<endTime)
								sendList.wait(endTime-currTime);
							else
								sendList.wait(100);
						} catch (InterruptedException e) {
						}
					}
					
					
					if (sendList.isEmpty()==false)
						packet=sendList.pop();
				}
				
				
				sendPacket(packet);
			}
		}
	}
	
	
	
	private BluetoothClientSocket socket;
	private volatile ReceiveThread receiveThread;
	private volatile SendThread sendThread;
	
	private volatile boolean []socketError;
	private volatile int localId;
	private volatile User [] users;
	private volatile Direction [] lastDirections;
	private volatile boolean [] directionUpdated;
	private volatile long frameStartTime;
	private volatile boolean newTimeframe; 
	private volatile int userCount;
	private volatile ObjectPlacementList newObjectList;
	private volatile boolean directionSent;
	
	private void setErrorState(int id){
		Log.w(TAG, "Setting error state.");
		
		socketError[id]=true;

		if (id==localId){
			receiveThread.stopMe();
			sendThread.stopMe();
		
			try {
				socket.close();
			} catch (IOException e) {
			}
		}
	}
	
	public boolean waitForTimeframeEnd(long timeout){
		long endTime=System.currentTimeMillis()+timeout;
		synchronized (lastDirections){
			while (newTimeframe==false){
				long now=System.currentTimeMillis();
				if (endTime<=now){
					return false;
				}
				
				try {
					lastDirections.wait(endTime-now);
				} catch (InterruptedException e) {
				}
			}
			newTimeframe=false;
		}
		
		return true;
	}
	
	
	public ClientNetworkManager(BluetoothClientSocket socket){
		idAssigned=false;
		socketError=new boolean[BluetoothServer.MAX_CONNECTIONS];
		users=new User[BluetoothServer.MAX_CONNECTIONS];
		for (int a=0; a<users.length; a++){
			users[a]=new User();
		}
		lastDirections=new Direction[BluetoothServer.MAX_CONNECTIONS];
		directionUpdated=new boolean[BluetoothServer.MAX_CONNECTIONS];
		newTimeframe=false;
		userCount=1;
		newObjectList=new ObjectPlacementList();
		this.socket=socket;
		
		sendThread=new SendThread();
		receiveThread=new ReceiveThread();
	
		receiveThread.start();
		sendThread.start();
		
	}

	@Override
	public void getSnakeDirections(Direction[] destionation) {
		if (!waitForTimeframeEnd(TIMEOUT)){
			Log.w(TAG, "Timeout waiting for server to respond.");
			setErrorState(localId);
			return;
		}
		
		synchronized (lastDirections){
			for (int a=0; a<userCount; a++){
				if (socketError[a]==false){
					if (directionUpdated[a]==false){
						throw new RuntimeException("Inconsistent system state. Protocol error.");
					}
					
					directionUpdated[a]=false;
				}
			}
			
			for (int a=0; a<userCount; a++)
				destionation[a]=lastDirections[a];
		}
		

	}

	@Override
	public void putLocalDirection(Direction direction) {
		synchronized (lastDirections){
			if (directionSent)
				return;
			
			directionSent=true;
			
			lastDirections[localId]=direction;
			directionUpdated[localId]=true;
			
			lastDirections.notifyAll();
		}
		
		SnakeMovementPacket packet=new SnakeMovementPacket();
		packet.direction=direction;
		packet.id=localId;
		sendThread.add(packet);		
	}

	@Override
	public long getFrameStartTimeInMills() {
		return frameStartTime;
	}

	@Override
	public void putNewObjects(NewObjectPlacement... object) {
		throw new RuntimeException("Only server can put new objects on the board.");
	}

	@Override
	public ObjectPlacementList getNewObjects() {
		return newObjectList;
	}

	@Override
	public User[] getUserList() {
		return users;
	}

	@Override
	public boolean[] getErrorList() {
		return socketError;
	}

	@Override
	public int getUsetCount() {
		return userCount;
	}
	
	public boolean waitForLocalId(int timeout){
		long endTime=System.currentTimeMillis()+timeout;
		synchronized (idLock){
			while (idAssigned==false){
				long now=System.currentTimeMillis();
				if (endTime<=now){
					return false;
				}
				
				try {
					idLock.wait(endTime-now);
				} catch (InterruptedException e) {
				}
			}
		}
		
		return true;
	}

	@Override
	public void setLocalUserData(String name, int color) {
		Log.w(TAG, "setLocalUserData() called");
		if (waitForLocalId(TIMEOUT)==false)
			throw new RuntimeException("Server not responding.");
		
		Log.w(TAG, "Setting local user data. Local user ID is "+localId);
		
		UserRegisterPacket userPacket=new UserRegisterPacket();
		synchronized (users){
			users[localId].name=name;
			users[localId].color=color;
		}
		
		userPacket.id=localId;
		userPacket.name=name;
		userPacket.color=color;
		
		sendThread.add(userPacket);
		
	}

	@Override
	public boolean waitForGameStart(long timeoutInMills) {
		long endTime=System.currentTimeMillis()+timeoutInMills;
		synchronized (lastDirections){
			while (gameStarted==false){
				long now=System.currentTimeMillis();
				if (endTime<=now){
					return false;
				}
				
				try {
					lastDirections.wait(endTime-now);
				} catch (InterruptedException e) {
				}
			}
		}
		
		return true;		
	}

	@Override
	public void startLocalGame() {
		sendThread.add(ServerNetworkManager.FlagPacket.START);
	}

}
