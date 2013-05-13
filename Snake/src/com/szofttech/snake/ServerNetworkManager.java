package com.szofttech.snake;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import android.util.Log;

import com.szofttech.snake.Snake.Direction;

public class ServerNetworkManager implements NetworkManager {
	private final String TAG="Snake.ServerNetworkManager";
	private volatile SnakeBluetoothSocket []clientSockets;
	private volatile boolean []socketError;
	private static final long MAX_DIRECTION_TIMEOUT=1000;
	
	public static enum FlagPacket{NEW_TIMEFRAME, END_GAME, START}; 
	
	private volatile AtomicInteger gameTime;
	
	private void setErrorState(final int id){
		Log.w(TAG, "Setting error state for socket "+id);
		
		socketError[id]=true;

		if (id!=0){
			receiveThreads[id].stopMe();
			sendThreads[id].stopMe();
			
			try {
				clientSockets[id].close();
			} catch (IOException e) {
			}
		}
		
		ClientDisconnectPacket packet=new ClientDisconnectPacket();
		packet.id=id;
		broadcast(packet);
	}
	
	/**
	 * This is thread for receiving packets from clients.
	 * 
	 *
	 */
	private class ReceiveThread extends Thread{
		private static final String TAG="Snake.ServerNetworkManager.ReceiveThread";
		
		
		private volatile boolean running;
		private final int id;
		
		
		public ReceiveThread(final int id){
			running=true;
			this.id=id;
		}
		
		public void stopMe(){
			running=false;
		}
		
		public void setErrorState(){
			ServerNetworkManager.this.setErrorState(id);
		}
		
		private void broadcastExceptMe(Object o){
			synchronized (users){
				for (int a=1; a<userCount; a++){
					if (socketError[a]==false && a!=id){
						sendThreads[a].add(o);
					}
				}
			}
		}
		
		private void processNewUserPacket(UserRegisterPacket userPacket){
			Log.w("Snake server","New user packet. User name: "+userPacket.name);
			synchronized (users){
				users[id].name=userPacket.name;
				users[id].color=userPacket.color;
				Log.w(TAG, "User data received about user "+id+" with name: \""+users[id].name+"\"");
			}
			
			broadcastExceptMe(userPacket);
		}
		
		private void processDirectionPacket(Direction d){
			
			Log.w(TAG, "Direction packet received from user "+id);
			synchronized (lastDirections){
				lastDirections[id]=d;
				directionUpdated[id]=true;
				lastDirections.notify();
			}
			Log.w(TAG, "Updated flag set for id "+id);
			
			
			SnakeMovementPacket movement=new SnakeMovementPacket();
			movement.id=id;
			movement.direction=d;
			broadcastExceptMe(movement);
		}
		
		private Object receiveObject(){
			Object packet=null;
			
			//while (packet==null){
				try {
					packet=clientSockets[id].read();
				} catch (IOException e) {
					Log.e(TAG, "I/O Exception reading socket");
					e.printStackTrace();
					setErrorState();
				} catch (ClassNotFoundException e) {
					Log.e(TAG, "Class not found exception on network socket");
					setErrorState();
				}
			//}
			
			return packet;
		}
		
		private void handleFlagPacket(FlagPacket packet){
			if (packet.equals(FlagPacket.END_GAME)){
				Log.w(TAG, "Game ended by user "+id);
				setErrorState();
			} else if (packet.equals(FlagPacket.START)){
				synchronized (gameStarted){
					gameStarted[id]=true;
					gameStarted.notify();
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
				Object packet=receiveObject();
				
				if (packet==null)
					break;		
				Log.w("Snake server", "Packet received from user");
				
				if (packet instanceof UserRegisterPacket){
					processNewUserPacket((UserRegisterPacket)packet);
				} else if (packet instanceof Snake.Direction){
					processDirectionPacket((Snake.Direction)packet);
				} else if (packet instanceof FlagPacket){
					handleFlagPacket((FlagPacket)packet);
				}
			}
		}
	}
	
	/**
	 * This is a simple thread plus an object queue for sending packets to
	 * clients.
	 *
	 */
	private class SendThread extends Thread{
		
		private static final String TAG="Snake.ServerNetworkManager.SendThread";
		
		private volatile boolean running;
		private final int id;
		
		private LinkedList<Object> sendList;
		
		
		public SendThread(final int id){
			running=true;
			this.id=id;
			sendList=new LinkedList<Object>();
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
				Log.w(TAG, "I/O Exception writing socket");
				e1.printStackTrace();
				setErrorState(id);
			}
		}
		
		private void sendUsers(){
			synchronized (users){
				for (int a=0; a<userCount; a++){
					UserRegisterPacket packet=new UserRegisterPacket();
					packet.color=users[a].color;
					packet.name=users[a].name;
					packet.id=a;
					
					synchronized (sendList){
						sendList.add(packet);
					}
				}
			}
		}
		
		private void sendSettings(){
			sendList.add(Game.getInstance().settings);
		}
		
		@Override
		public void run(){
			Log.w(TAG, "Assigning snake ID...");
			assignSnakeID();
			Log.w(TAG, "Sending game settings...");
			sendSettings();
			Log.w(TAG, "Sending user list...");
			sendUsers();
			Log.w(TAG, "Done. Sending regular objects...");
			
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
					Log.w(TAG, "Object sent...");
				} catch (IOException e) {
					Log.w(TAG, "I/O error writing socket for user "+id);
					e.printStackTrace();
					setErrorState(id);
				}
			}
		}
	}
	
	
	
	private volatile ReceiveThread [] receiveThreads;
	private volatile SendThread [] sendThreads;
	
	private volatile User [] users;
	private volatile Direction [] lastDirections;
	private volatile Direction [] comittedDirections;
	private volatile boolean directionComitted;
	private volatile boolean [] directionUpdated;
	private volatile boolean [] gameStarted;
	private volatile int userCount=1;
	private LinkedList<NewObjectPlacement> newObjectList;
	
	private Timer gameTimer;
	private volatile long frameStartTime;
	private volatile Object frameEndSyncObject;
	private volatile boolean frameEnded=false;
	
	
	
	public ServerNetworkManager(){
		newObjectList=new LinkedList<NewObjectPlacement>();
		receiveThreads=new ReceiveThread[BluetoothServer.MAX_CONNECTIONS];
		sendThreads=new SendThread[BluetoothServer.MAX_CONNECTIONS];
		users=new User[BluetoothServer.MAX_CONNECTIONS];
		lastDirections=new Direction[BluetoothServer.MAX_CONNECTIONS];
		comittedDirections=new Direction[BluetoothServer.MAX_CONNECTIONS];
		directionUpdated=new boolean[BluetoothServer.MAX_CONNECTIONS];
		socketError=new boolean[BluetoothServer.MAX_CONNECTIONS];
		clientSockets=new SnakeBluetoothSocket[BluetoothServer.MAX_CONNECTIONS];
		gameStarted=new boolean[BluetoothServer.MAX_CONNECTIONS];
		lastDirections[0]=Snake.Direction.UNCHANGED;
		frameEndSyncObject=new Object();
		
		gameTime=new AtomicInteger(0);
			
		for (int a=0; a<BluetoothServer.MAX_CONNECTIONS; a++){
			users[a]=new User();
			directionUpdated[a]=false;
			gameStarted[a]=false;
		}
		
		gameTimer=new Timer();
	}
	
	private void broadcast(Object o){
		synchronized (users){
			for (int a=1; a<userCount; a++){
				if (users[a]!=null && socketError[a]==false){
					Log.w(TAG,"Sending broadcast message to "+a);
					sendThreads[a].add(o);
				}
			}
		}
	}
	
	private void sendLocalDirection(){
		Log.w(TAG, "Broadcasting local direction");
		SnakeMovementPacket packet=new SnakeMovementPacket();
		packet.direction=lastDirections[0];
		packet.id=0;
		broadcast(packet);
	}
	
	private void processDirectionsOnTimeframeEnd(){
		synchronized (lastDirections){
			synchronized (comittedDirections){
				sendLocalDirection();
				
				directionComitted=true;
				for (int a=0; a<userCount; a++){
					comittedDirections[a]=lastDirections[a];
					
					if ((directionUpdated[a]==false) && (a!=0)){
						setErrorState(a);
						Log.w(TAG, "Socket "+a+" lost because of timeout.");
					} else
						directionUpdated[a]=false;
				}

				directionComitted=true;
				comittedDirections.notify();
				lastDirections[0]=Snake.Direction.UNCHANGED;
			}
		}
	}
	
	private void startGame(long stepTime){
		frameStartTime=0;
		gameTimer.scheduleAtFixedRate(new TimerTask(){

			@Override
			public void run() {
				gameTime.incrementAndGet();
				
				long time=System.currentTimeMillis();
				if (frameStartTime!=0){
					boolean sucsess=waitForDirections(MAX_DIRECTION_TIMEOUT);
					for (int a=0; a<userCount; a++)
						Log.w(TAG, "User "+a+" reported: "+directionUpdated[a]);
					processDirectionsOnTimeframeEnd();
					
					if (!sucsess)
						throw new RuntimeException("Some of the snakes not reported their direction");
					
				}
				frameStartTime=System.currentTimeMillis();
				
				Log.w(TAG,  "Waited for client directions "+(frameStartTime-time)+"ms");
				broadcast(FlagPacket.NEW_TIMEFRAME);
				
				synchronized (frameEndSyncObject){
					frameEnded=true;
					frameEndSyncObject.notify();
				}
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
				for (int a=1; a<userCount; a++){
					if (socketError[a]==false && directionUpdated[a]==false){
						notReceived=true;
						break;
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
			clientSockets[id]=socket;
			
			socketError[id]=false;
		}
		
		tr.start();
		ts.start();
	}
	
	@Override
	public void getSnakeDirections(Direction[] destionation) {
		long endTime=System.currentTimeMillis()+MAX_DIRECTION_TIMEOUT;
		
		synchronized (comittedDirections){
			while (!directionComitted){
				long now=System.currentTimeMillis();
				
				if (now>endTime){
					throw new RuntimeException("Error waiting for directions.");
				}
				
				try {
					comittedDirections.wait(endTime-now);
				} catch (InterruptedException e) {
				}
			}
			
			for (int a=0; a<userCount; a++){
				destionation[a]=comittedDirections[a];
			}
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
	public void putNewObjects(NewObjectPlacement... object) {
		for (int b=0; b<object.length; b++)
			broadcast(object[b]);
		
		synchronized (newObjectList){
			for (int b=0; b<object.length; b++)
				newObjectList.add(object[b]);
		}
		
	}

	@Override
	public LinkedList<NewObjectPlacement> getNewObjects() {
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

	@Override
	public void setLocalUserData(String name, int color) {
		UserRegisterPacket userPacket=new UserRegisterPacket();
		synchronized (users){
			users[0].name=name;
			users[0].color=color;
		}
		
		userPacket.id=0;
		userPacket.name=name;
		userPacket.color=color;
		
		broadcast(userPacket);
	}

	
	private boolean waitForGameStartInternal(long timeoutInMills, int startIndex){
		long endTime=System.currentTimeMillis()+timeoutInMills;
		
		synchronized (gameStarted){
			boolean started;
			
			while (true){
				started=true;
				for (int a=startIndex; a<userCount; a++){
					if (gameStarted[a]==false){
						started=false;
						break;
					}
				}
				
				if (started)
					return true;
				
				long now=System.currentTimeMillis();
				if (now>=endTime){
					return false;
				}
				
				
				try {
					gameStarted.wait(endTime-now);
				} catch (InterruptedException e) {
				}
			}
		}
	}
	
	@Override
	public boolean waitForGameStart(long timeoutInMills) {
		return waitForGameStartInternal(timeoutInMills,0);
	}

	@Override
	public void startLocalGame() {
		synchronized (gameStarted){
			gameStarted[0]=true;
			gameStarted.notify();
		}
		
		while (waitForGameStartInternal(1000,1)==false);
		
		Log.w(TAG, "Starting game with step time "+Game.getInstance().settings.stepTime);
		startGame(Game.getInstance().settings.stepTime);
	}

	@Override
	public boolean waitForFrameEnd(long timeoutInMills) {
		long endTime=System.currentTimeMillis()+timeoutInMills;
		
		synchronized (frameEndSyncObject){
			while (true){
				if (frameEnded){
					frameEnded=false;
					return true;
				}
				
				long now=System.currentTimeMillis();
				if (now>=endTime){
					return false;
				}
				
				
				try {
					frameEndSyncObject.wait(endTime-now);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	@Override
	public int getGameTime() {
		return gameTime.get();
	}

}
