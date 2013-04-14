package com.szofttech.snake;

import java.io.IOException;
import java.util.LinkedList;

import android.util.Log;

import com.szofttech.snake.Snake.Direction;

public class ClientNetworkManager implements NetworkManager {
	private static final String TAG="Snake.ClientNetworkManager: ";
	private static final int TIMEOUT=1500;
	private static Boolean idAssigned;
	
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
			checkId(up.id);
			
			synchronized(users){
				users[up.id].name=up.name;
				users[up.id].color=up.color;
				
				userCount=Math.max(userCount, up.id);
			}
		}
		
		private void handleSnakeMovementPacket(SnakeMovementPacket packet){
			synchronized (lastDirections){
				checkId(packet.id);
				lastDirections[packet.id]=packet.direction;
				directionUpdated[packet.id]=true;
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
					}
					lastDirections.notify();
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
			Game.getInstance().settings.copyFrom(packet);
		}
		
		@Override
		public void run(){
			while (running){
				Object packet=receiveObject();
				
				if (packet==null)
					return;
				
				if (packet instanceof LoginPacket){
					localId=((LoginPacket)packet).id;
					synchronized(idAssigned){
						idAssigned=true;
						idAssigned.notify();
					}
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
		
		
		
		@Override
		public void run(){
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
					socket.write(packet);
					socket.flush();
				} catch (IOException e) {
					Log.w(TAG, "I/O error writing socket");
					setErrorState(localId);
				}
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
	
	
	public ClientNetworkManager(){
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
	}

	@Override
	public void getSnakeDirections(Direction[] destionation) {
		if (!waitForTimeframeEnd(TIMEOUT)){
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
			lastDirections[localId]=direction;
			directionUpdated[localId]=true;
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

}
