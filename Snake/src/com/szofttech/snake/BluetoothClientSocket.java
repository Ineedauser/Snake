package com.szofttech.snake;

import java.io.IOException;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class BluetoothClientSocket extends SnakeBluetoothSocket{
	private static final String TAG = "Snake.BluetoothClient";
	

	
	public static BluetoothClientSocket createClientSocket(BluetoothDevice device){		
		BluetoothSocket socket = null;
		for (int uuid=0; uuid<BluetoothServer.MAX_CONNECTIONS; uuid++){
        	try {
        		socket = device.createInsecureRfcommSocketToServiceRecord(BluetoothServer.uuids[uuid]);
        		socket.connect();
        		Log.w(TAG, "Connecte to server with uuid "+uuid);
        		break;
        	} catch (IOException e) {
        		socket=null;
        		Log.e(TAG, "Failed to create connection with UUID "+uuid, e);
        	}
        }
		
		Log.w(TAG, "UUID search returned.");
		BluetoothClientSocket result=null;
		if (socket!=null){
			try {
				result = new BluetoothClientSocket(socket);
			} catch (IOException e) {
			}
		}
		
		if (result==null)
			Log.w(TAG, "Returning null.");
		else
			Log.w(TAG, "Returning non null.");
		return result;
	}
	
	private BluetoothClientSocket(BluetoothSocket socket) throws IOException {
		super(socket);
	}
}
