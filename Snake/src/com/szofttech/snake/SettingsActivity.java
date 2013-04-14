package com.szofttech.snake;

import android.os.Bundle;
import android.app.Activity;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SettingsActivity extends Activity {
	
	private static TextView timeLabel;
	private static TextView dim1Label;
	private static TextView dim2Label;
	
	private static final String PREFS_NAME = "com.szofttech.snake.gameSettings";
	
	private static final String PLAY_TIME_STRING="playTime";
	private static final int PLAY_TIME_OFFSET=1;	
	private static final int DEFAULT_PLAY_TIME=5;
	
	private static final String SPEED_STRING="snakeSpeed";
	private static final int DEFAULT_SPEED=300;
	private static final int SPEED_OFFSET=50;
	private static final int SPEED_STEP_SIZE=5;
	
	private static final String BONUS_PROBABILITY_STRING="skullProbability";
	private static final int DEFAULT_BONUS_PROBABILITY=10;
	private static final float BONUS_STEP_SIZE=0.001f;
	
	private static final String SKULL_PROBABILITY_STRING="skullProbability";
	private static final int DEFAULT_SKULL_PROBABILITY=5;
	private static final float SKULL_STEP_SIZE=0.001f;
	
	private static final String DIM1_STRING="dimension1";
	private static final int DIM1_OFFSET=20;
	private static final int DEFAULT_DIM1=40;
	private static final int DIM1_STEP_SIZE=2;
	
	private static final String DIM2_STRING="dimension2";
	private static final int DIM2_OFFSET=20;
	private static final int DEFAULT_DIM2=24;
	private static final int DIM2_STEP_SIZE=2;
			
	private SharedPreferences settings;
	
	private void setTimeLabel(int progress){
		timeLabel.setText(""+progress+"min");
	}
	
	private void setDim1Label(int progress){
		dim1Label.setText(""+progress);
	}
	
	private void setDim2Label(int progress){
		dim2Label.setText(""+progress);
	}
	
	
	private int dim1ToReal(int dim1){
		return DIM1_OFFSET+DIM1_STEP_SIZE*dim1;
	}
	
	private int dim2ToReal(int dim2){
		return DIM2_OFFSET+DIM2_STEP_SIZE*dim2;
	}
	
	private int stepTimeToReal(int stepTime){
		return SPEED_OFFSET+SPEED_STEP_SIZE*(100-stepTime);
	}
	
	private float skullProbabilityToReal(int skullProbability){
		return SKULL_STEP_SIZE*skullProbability;
	}
	
	private float bonusProbabilityToReal(int skullProbability){
		return BONUS_STEP_SIZE*skullProbability;
	}
	
	private void loadSettings(){
		SeekBar timeBar=(SeekBar)findViewById(R.id.timeSeek);
		SeekBar speedBar=(SeekBar)findViewById(R.id.speedSeek);
		SeekBar bonusBar=(SeekBar)findViewById(R.id.bonusSeek);
		SeekBar skullBar=(SeekBar)findViewById(R.id.skullSeek);
		SeekBar dim1Bar=(SeekBar)findViewById(R.id.dim1Seek);
		SeekBar dim2Bar=(SeekBar)findViewById(R.id.dim2Seek);
		
		int progress=settings.getInt(PLAY_TIME_STRING, DEFAULT_PLAY_TIME-PLAY_TIME_OFFSET);
		timeBar.setProgress(progress);
		setTimeLabel(progress+PLAY_TIME_OFFSET);
		
		progress=settings.getInt(DIM1_STRING, (DEFAULT_DIM1-DIM1_OFFSET)/DIM1_STEP_SIZE);
		dim1Bar.setProgress(progress);
		setDim1Label(dim1ToReal(progress));
		
		progress=settings.getInt(DIM2_STRING, (DEFAULT_DIM2-DIM2_OFFSET)/DIM1_STEP_SIZE);
		dim2Bar.setProgress(progress);
		setDim2Label(dim2ToReal(progress));
		
		speedBar.setProgress(settings.getInt(SPEED_STRING, (DEFAULT_SPEED-SPEED_OFFSET)/SPEED_STEP_SIZE));
		bonusBar.setProgress(settings.getInt(BONUS_PROBABILITY_STRING, DEFAULT_BONUS_PROBABILITY));
		skullBar.setProgress(settings.getInt(SKULL_PROBABILITY_STRING, DEFAULT_SKULL_PROBABILITY));
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		settings = getSharedPreferences(PREFS_NAME, 0);
	
		setResult(Activity.RESULT_CANCELED);
		
		Button okButton=(Button)findViewById(R.id.settingsOkButton);
						
		okButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	SeekBar timeBar=(SeekBar)findViewById(R.id.timeSeek);
        		SeekBar speedBar=(SeekBar)findViewById(R.id.speedSeek);
        		SeekBar bonusBar=(SeekBar)findViewById(R.id.bonusSeek);
        		SeekBar skullBar=(SeekBar)findViewById(R.id.skullSeek);
        		SeekBar dim1Bar=(SeekBar)findViewById(R.id.dim1Seek);
        		SeekBar dim2Bar=(SeekBar)findViewById(R.id.dim2Seek);
        		
        		SharedPreferences.Editor editor = settings.edit();
        		editor.putInt(PLAY_TIME_STRING, timeBar.getProgress());
        		editor.putInt(SPEED_STRING, speedBar.getProgress());
        		editor.putInt(BONUS_PROBABILITY_STRING, bonusBar.getProgress());
        		editor.putInt(SKULL_PROBABILITY_STRING, skullBar.getProgress());
        		editor.putInt(DIM1_STRING, dim1Bar.getProgress());
        		editor.putInt(DIM2_STRING, dim2Bar.getProgress());
        		editor.commit();
            	
        		
        		Game game=Game.getInstance();
        		game.settings.height=dim1ToReal(dim1Bar.getProgress());
        		game.settings.width=dim1ToReal(dim2Bar.getProgress());
        		game.settings.stepTime=stepTimeToReal(speedBar.getProgress());
        		game.settings.skullProbability=skullProbabilityToReal(skullBar.getProgress());
        		game.settings.starProbability=bonusProbabilityToReal(bonusBar.getProgress());
        		
        		//game.settings.stepTime
            	
            	setResult(Activity.RESULT_OK);
            	finish();
            }
		});
		
		timeLabel=(TextView)findViewById(R.id.timeLabel);
		dim1Label=(TextView)findViewById(R.id.dim1Label);
		dim2Label=(TextView)findViewById(R.id.dim2Label);
		
		SeekBar timeBar=(SeekBar)findViewById(R.id.timeSeek);
		timeBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
				setTimeLabel(progress+1);
			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
			}
			
		});
		
		SeekBar dim1Bar=(SeekBar)findViewById(R.id.dim1Seek);
		dim1Bar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
				setDim1Label(dim1ToReal(progress));
			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
			}
			
		});
		
		SeekBar dim2Bar=(SeekBar)findViewById(R.id.dim2Seek);
		dim2Bar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
				setDim2Label(dim2ToReal(progress));
			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
			}
			
		});
		
		loadSettings();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

}
