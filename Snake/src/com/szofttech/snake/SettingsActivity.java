package com.szofttech.snake;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class SettingsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
	
		setResult(Activity.RESULT_CANCELED);
		
		Button okButton=(Button)findViewById(R.id.settingsOkButton);
						
		okButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	setResult(Activity.RESULT_OK);
            	finish();
            }
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

}
