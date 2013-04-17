package com.szofttech.snake;

import java.util.Random;

import android.content.Context;
import android.util.Log;

public class Star extends Collectable{
	private static final int MIN_DISPLAYED_TIME=5000;
	private static final int MAX_DISPLAYED_TIME=15000;
	
	private long timeout;

	public Star(Context appContext) {
		super(appContext);
	}

	@Override
	protected int getTextureID() {
		return TextureTable.STAR;
	}

	@Override
	public int collectedScoreTransform(int oldScore) {
		return Math.round(1.2f*oldScore);
	}

	@Override
	public boolean isGrowNeeded() {
		return true;
	}
	
	
	@Override
	public boolean isTimedOut() {
		return Game.getInstance().networkManager.getGameTime()>timeout;
	}

	public static int generateTimeout(Random random) {
		return (int) ((MIN_DISPLAYED_TIME + random.nextInt(MAX_DISPLAYED_TIME - MIN_DISPLAYED_TIME + 1))/Game.getInstance().settings.stepTime);	
	}


	@Override
	public void setTimeout(int timeout) {
		Log.w("Snake", "Setting timeout to "+timeout);
		this.timeout=Game.getInstance().networkManager.getGameTime()+timeout;
	}

}
