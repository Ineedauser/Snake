package com.szofttech.snake;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class UserListActivity extends Activity {
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
		TextView usernameEditor=(TextView)findViewById(R.id.usernameEditor);
		
		loadSettings();
		
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
							Log.w("SNAKE", "Username changed.");
							synchronized (changeTimer){
								onChangeFinishedTimer.cancel();
								onChangeFinishedTimer=null;
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
						saveSettings();
					}
				}, color);
				
										
				colorDialog.show();
				
			}
			
		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.user_list, menu);
		return true;
	}

}
