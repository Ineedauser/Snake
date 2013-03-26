package com.szofttech.snake;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;

public class NetworkManager {
	static private BluetoothAdapter bluetoothAdapter = null;
	
	static void init(){
		bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
	}
	
	NetworkManager createClient(){
		
		//bluetoothAdapter.is
		return null;
	}
}
