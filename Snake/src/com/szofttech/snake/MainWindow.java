package com.szofttech.snake;

import com.szofttech.snake.GameScreen;
import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MainWindow extends Activity {
	
	private final int BLUETOOTH_ENABLE_TIMEOUT=300;
	private final int BLUETOOTH_DISCOVERABLE_RESULT=1;
	private final int BLUETOOTH_ENABLE_RESULT=2;

	BluetoothAdapter bluetoothAdapter;
	
	void showBluetoothNotFoundError(){
		Helpers.showErrorMessage(this, R.string.bluetooth_not_found_message, R.string.bluetooth_not_found_title);
	}
	
	void startClientGame(){
		
	}
	
	void startServerGame(){
		
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
        
        //MainWindow windowReference = this;
        testButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	Intent intent = new Intent(getBaseContext(), GameScreen.class);
            	startActivity(intent);
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
                    startActivityForResult(enableBtIntent, BLUETOOTH_DISCOVERABLE_RESULT);
                } else
                	startClientGame();
            }
        });
        
        
        

        
        
    }
    
    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data){
    	switch (requestCode){
    		case(BLUETOOTH_DISCOVERABLE_RESULT):
    			//This is the case when server mode is selected
    			if (resultCode == Activity.RESULT_CANCELED) {
                    // User did not enable Bluetooth or an error occurred
    				Helpers.showErrorMessage(this,  R.string.bluetooth_must_be_enabled, R.string.bluetooth_not_enabled_title);
                }
    		
    			startServerGame();
    			break;
    			
    		case(BLUETOOTH_ENABLE_RESULT):
    			if (resultCode == Activity.RESULT_CANCELED) {
                    // User did not enable Bluetooth or an error occurred
    				Helpers.showErrorMessage(this,  R.string.bluetooth_must_be_enabled, R.string.bluetooth_not_enabled_title);
                }
    		
    			startClientGame();
    			break;
    	}
    }
 
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main_window, menu);
        return true;
    }
        
}
