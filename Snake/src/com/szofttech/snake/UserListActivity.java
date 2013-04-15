package com.szofttech.snake;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class UserListActivity extends Activity {

	private static class UserAdapter extends BaseAdapter{
		private LayoutInflater inflater=null;
		
		public UserAdapter(final Activity owner) {
			inflater = (LayoutInflater)owner.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			if(convertView==null)
				view = inflater.inflate(R.layout.userlist_row, null);   
			else
				view = convertView;
		  
		    if (view!=null){
		    	TextView name = (TextView)view.findViewById(R.id.snakeName);
		    	View color=view.findViewById(R.id.snakeColor);
		    	
		    	User [] users=Game.getInstance().networkManager.getUserList();

		    	synchronized(users){
		    		name.setText(users[position].name);
		    		color.setBackgroundColor(users[position].color);
		    	}
		    }           
		    view.setClickable(false);
		    return view;    
		}

		@Override
		public int getCount() {
			return Game.getInstance().networkManager.getUsetCount();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
	}
	
	private static final String TAG="Snake.UserListActivity";
	
	private static final String PREFS_NAME = "com.szofttech.snake.userSettings";

	private View colorView;
	private int color;
	private String username;
	
	private final String COLOR_NAME="color";
	private final int COLOR_DEFAULT=Color.RED;
	
	private final String USERNAME_NAME="username";
	private final String USERNAME_DEFAULT="?";
	
	private volatile Timer changeTimer=null;
	private volatile TimerTask onChangeFinishedTimer=null;
	
	private volatile long TYPING_TIMEOUT=1500;
	
	private volatile long LIST_REFRESH_INTERVAL=1000;
	
	private Timer listRefreshTimer;
	
	private UserAdapter adapter;
	
	private void setUserData(){
		Game.getInstance().networkManager.setLocalUserData(username, color);
	}
	
	private void loadSettings(){
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		color=settings.getInt(COLOR_NAME, COLOR_DEFAULT);
		username=settings.getString(USERNAME_NAME, USERNAME_DEFAULT);
	}
	
	private void saveSettings(){
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		Editor e=settings.edit();
		e.putString(USERNAME_NAME, username);
		e.putInt(COLOR_NAME, color);
		e.commit();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_list);
		
		colorView=(View)findViewById(R.id.userColorEditor);
		final TextView usernameEditor=(TextView)findViewById(R.id.usernameEditor);
		
		loadSettings();
		setUserData();
		
		colorView.setBackgroundColor(color);
		usernameEditor.setText(USERNAME_DEFAULT);
		
		changeTimer=new Timer();
		
		usernameEditor.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable arg0) {
				synchronized (changeTimer){
					if (onChangeFinishedTimer!=null)
						onChangeFinishedTimer.cancel();
					
					onChangeFinishedTimer=new TimerTask(){
						@Override
						public void run() {
							username=usernameEditor.getText().toString();

							setUserData();
							synchronized (changeTimer){
								if (onChangeFinishedTimer!=null){
									onChangeFinishedTimer.cancel();
									onChangeFinishedTimer=null;
								}
							}
						}
					
					};
					
					changeTimer.schedule(onChangeFinishedTimer, TYPING_TIMEOUT);
				}
				
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				
			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				
			}
			
		});
		
		colorView.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				ColorPickerDialog colorDialog=new ColorPickerDialog(UserListActivity.this, new ColorPickerDialog.OnColorChangedListener() {
					
					@Override
					public void colorChanged(int color) {
						UserListActivity.this.color=color;
						colorView.setBackgroundColor(color);
						setUserData();
					}
				}, color);
				
										
				colorDialog.show();
				
			}
			
		});
		
		
		final Handler handler = new Handler() {

	        public void handleMessage(Message msg) {
	        	Log.w(TAG, "User count: "+Game.getInstance().networkManager.getUsetCount());
	        	for (int a=0; a<Game.getInstance().networkManager.getUsetCount(); a++ ){
	        		Log.w(TAG, "User "+a+": "+Game.getInstance().networkManager.getUserList()[a].name);
	        	}
	        	adapter.notifyDataSetChanged();
	        }
        };
		
		adapter=new UserAdapter(this);
		ListView deviceListView=(ListView)findViewById(R.id.userList);
		deviceListView.setAdapter(adapter);
		
		listRefreshTimer=new Timer();
		listRefreshTimer.scheduleAtFixedRate(new TimerTask(){

			@Override
			public void run() {
				handler.obtainMessage(1).sendToTarget();
				//Log.w("SNAKE        ", "User count="+Game.getInstance().networkManager.getUsetCount());
			}
		
		}, 0, LIST_REFRESH_INTERVAL);
		
		
	}
	
	@Override
	protected void onDestroy() {
		listRefreshTimer.cancel();
		listRefreshTimer.purge();
		
		changeTimer.cancel();
		changeTimer.purge();
		
	    super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.user_list, menu);
		return true;
	}

}
