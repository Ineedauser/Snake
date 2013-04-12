package com.szofttech.snake;

import com.szofttech.snake.GameScreen;
import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainWindow extends Activity{
	private static final String TAG="Snake.MainWindow";
	
	private final int BLUETOOTH_ENABLE_TIMEOUT=300;
	private final int BLUETOOTH_DISCOVERABLE_RESULT=1;
	private final int BLUETOOTH_ENABLE_RESULT=2;
	private final int BLUETOOTH_SELECTED=3;
	

	BluetoothAdapter bluetoothAdapter;
	BluetoothServer server;
	
	private ProgressDialog busyDialog;
	private ClientNetworkManager clientNetworkManager;
	
	
	
	private class ConnectToServerThread extends AsyncTask<BluetoothDevice, Void, BluetoothClientSocket> {	
		@Override
		protected BluetoothClientSocket doInBackground(BluetoothDevice... devs) {
			BluetoothClientSocket result=BluetoothClientSocket.createClientSocket(devs[0]);
			Log.w(TAG,"Client socket created!");
			return result;
		}
		
		@Override
		protected void onPreExecute() {  
			busyDialog.setMessage(getText(R.string.connecting_wait_text));
			busyDialog.show();
		}
			
		@Override
		protected void onPostExecute(BluetoothClientSocket socket) {
			Log.w(TAG,"onPostExecute reached");
			busyDialog.hide();
			if (socket==null){
				Helpers.showErrorMessage(MainWindow.this, R.string.server_connect_failed_message, R.string.failed_to_connect_title);
			} else {
				Log.w("                 SNAKE             ","Connected.");
				//clientNetworkManager=new ClientNetworkManager();
				//clientNetworkManager.login(socket);
			}
	    }
	}

	
	
	void showBluetoothNotFoundError(){
		Helpers.showErrorMessage(this, R.string.bluetooth_not_found_message, R.string.bluetooth_not_found_title);
	}
	
	
	
	void startClientGame(BluetoothDevice dev){
		new ConnectToServerThread().execute(dev);
	}
	
	void showClientListWindow(){
		Intent scannerIntent = new Intent(getBaseContext(), BluetoothDeviceList.class);
		startActivityForResult(scannerIntent,BLUETOOTH_SELECTED);
	}
	
	void stopServer(){
		if (server!=null){
			server.stopListening();
			server.closeClients();
		}
	}
	
	void startServerGame(){
		stopServer();
		server=new BluetoothServer();
		server.startListening();
	}
	
	void startSingleGame(){
		Game game=Game.getInstance();
		game.networkManager=new DummyNetworkManager();
		game.isServer=true;
		game.renderer=new SnakeRenderer(getBaseContext());
		game.settings=new GameSettings();
		game.settings.height=24;
		game.settings.width=40;
		//game.settings.height=10;
		//game.settings.width=15;
		game.settings.stepTime=200;
		game.settings.starProbability=0.005f;
		game.settings.skullProbability=0.01f;
		game.context=this;
		
    	Intent intent = new Intent(getBaseContext(), GameScreen.class);
    	startActivity(intent);
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
        setContentView(R.layout.activity_main_window);
        
        
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
       
        
        ImageView image=(ImageView)findViewById(R.id.startImage);
        //Disable hardware acceleration for drawing the image
        image.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        SVG svg = SVGParser.getSVGFromResource(getResources(), R.raw.snake);
        image.setImageDrawable(svg.createPictureDrawable());
        
        Button testButton=(Button)findViewById(R.id.testButton);
        Button clientButton=(Button)findViewById(R.id.clientSelectedButton);
        Button serverButton=(Button)findViewById(R.id.serverSelectedButton);
        
        busyDialog=new ProgressDialog(this);
        busyDialog.setCancelable(false);
        
        //MainWindow windowReference = this;
        testButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	startSingleGame();
            }
        });
        
        serverButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if (bluetoothAdapter==null){
            		showBluetoothNotFoundError();
            		return;
            	}
            	
                //Make device discoverable
            	Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, BLUETOOTH_ENABLE_TIMEOUT);
        		startActivityForResult(discoverableIntent,BLUETOOTH_DISCOVERABLE_RESULT);
      
            }
        });
        
        
        clientButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if (bluetoothAdapter==null){
            		showBluetoothNotFoundError();
            		return;
            	}
            	
            	if (!bluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, BLUETOOTH_ENABLE_RESULT);
                } else
                	showClientListWindow();
            }
            
        });
        
    }
    
    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data){
    	switch (requestCode){
    	 	case BLUETOOTH_SELECTED:
    	 		// When DeviceListActivity returns with a device to connect
    	 		if (resultCode == Activity.RESULT_OK) {
    	 			BluetoothDevice mac = (BluetoothDevice)data.getExtras()
    	 		            .get(BluetoothDeviceList.EXTRA_DEVICE);
    	 			startClientGame(mac);
    	 		}
    	 		break;
    		case(BLUETOOTH_DISCOVERABLE_RESULT):
    			//This is the case when server mode is selected
    			if (resultCode == Activity.RESULT_CANCELED) {
                    // User did not enable Bluetooth or an error occurred
    				Helpers.showErrorMessage(this,  R.string.bluetooth_must_be_enabled, R.string.bluetooth_not_enabled_title);
                } else {
                	startServerGame();
                }
    			break;
    			
    		case(BLUETOOTH_ENABLE_RESULT):
    			if (resultCode == Activity.RESULT_CANCELED) {
                    // User did not enable Bluetooth or an error occurred
    				Helpers.showErrorMessage(this,  R.string.bluetooth_must_be_enabled, R.string.bluetooth_not_enabled_title);
                } else {
		      		
                	showClientListWindow();
                }
    			break;
    	}
    }
 
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main_window, menu);
        return true;
    }
    
    
    @Override
    public void onDestroy() {
    	stopServer();
    	super.onDestroy();
    }

	
}
