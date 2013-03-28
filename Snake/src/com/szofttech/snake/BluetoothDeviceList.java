package com.szofttech.snake;

import java.util.ArrayList;
import java.util.Set;

import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class BluetoothDeviceList extends Activity {
	
	private static class BluetoothDeviceListItem{
		public enum Type {COMPUTER, PHONE, UNKNOWN};
		
		public String name;
		public String mac;
		public Type type;
	
		BluetoothDeviceListItem(String name, String mac, Type type){
			this.name = name;
			this.mac = mac;
			this.type=type;
		}
	}
	
	
	private static class BluetoothDeviceAdapter extends BaseAdapter{
		private LayoutInflater inflater=null;
		private ArrayList<BluetoothDeviceListItem> devices;
		
		public BluetoothDeviceAdapter(final Activity owner, ArrayList<BluetoothDeviceListItem> devices) {
			inflater = (LayoutInflater)owner.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.devices=devices;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			if(convertView==null)
				view = inflater.inflate(R.layout.devicetable_row, null);   
			else
				view = convertView;
		  
		    if (view!=null){
		    	TextView name = (TextView)view.findViewById(R.id.deviceName);
		    	TextView mac = (TextView)view.findViewById(R.id.macAddress);
		    	ImageView icon= (ImageView)view.findViewById(R.id.deviceIcon);
		    	
		    	BluetoothDeviceListItem device=(BluetoothDeviceListItem)getItem(position);
		    	name.setText(device.name);
		    	mac.setText(device.mac);
		    	
		    	switch (device.type){
		    		case COMPUTER:
		    			icon.setImageResource(R.drawable.hardware_computer);
		    			break;
		    		case PHONE:
		    			icon.setImageResource(R.drawable.hardware_phone);
		    			break;
		    		case UNKNOWN:
		    			icon.setImageResource(R.drawable.action_help);
		    			break;
		    	}
		    	
		    }           
		    view.setClickable(false);
		    return view;    
		}

		@Override
		public int getCount() {
			return devices.size();
		}

		@Override
		public Object getItem(int position) {
			return devices.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
	}
	
	
	private BluetoothDeviceAdapter deviceAdapter;
	private ArrayList<BluetoothDeviceListItem> deviceList;
	private BluetoothAdapter bluetoothAdapter;

	private void addDevice(BluetoothDevice device){
		BluetoothDeviceListItem.Type type;
		switch (device.getBluetoothClass().getMajorDeviceClass()){
			case(BluetoothClass.Device.Major.PHONE):
				type=BluetoothDeviceListItem.Type.PHONE;
				break;
			case(BluetoothClass.Device.Major.COMPUTER):
				type=BluetoothDeviceListItem.Type.COMPUTER;
				break;
			default:
				type=BluetoothDeviceListItem.Type.UNKNOWN;
				break;
		}
		
		deviceList.add(new BluetoothDeviceListItem(device.getName(), device.getAddress(), type));
		deviceAdapter.notifyDataSetChanged();
	}
	
	private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                	addDevice(device);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            	hideScanningIndicator();
            }
        }
    };
	
    void showScanningIndicator(){
    	setProgressBarIndeterminateVisibility(true);
	    setProgressBarVisibility(true);
    }
    
    void hideScanningIndicator(){
    	setProgressBarIndeterminateVisibility(false);
	    setProgressBarVisibility(false);
    }
	
	private void fetchPairedDevices(){
		 // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
            	addDevice(device);
            }
        }
	}
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
		
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        requestWindowFeature(Window.FEATURE_PROGRESS);
        
		setContentView(R.layout.activity_bluetooth_device_list);
		
		deviceList=new ArrayList<BluetoothDeviceListItem>();
		deviceAdapter=new BluetoothDeviceAdapter(this,deviceList);
		
		ListView deviceListView=(ListView)findViewById(R.id.bluetoothList);
		deviceListView.setAdapter(deviceAdapter);
		
		
		
		// Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(eventReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(eventReceiver, filter);

		
		
		setResult(Activity.RESULT_CANCELED);
		
		startScanning();
	}
	
	
	
	
	void startScanning(){
	     showScanningIndicator();
	     
	     deviceList.clear();
	     fetchPairedDevices();
	     
	     if (bluetoothAdapter.isDiscovering()) {
	    	 bluetoothAdapter.cancelDiscovery();
	     }
	     
	     bluetoothAdapter.startDiscovery();
	}
	
	@Override
	protected void onDestroy() {
	    super.onDestroy();
	
	  
	    if (bluetoothAdapter != null) {
	    	bluetoothAdapter.cancelDiscovery();
	    }
	
	    // Unregister broadcast listeners
	    this.unregisterReceiver(eventReceiver);
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_bluetooth_device_list, menu);
		return true;
	}

}
