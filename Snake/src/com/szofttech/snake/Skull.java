package com.szofttech.snake;

import java.util.Random;

import android.content.Context;

public class Skull extends Collectable{
	
	private static final int MIN_DISPLAYED_TIME=5000;
	private static final int MAX_DISPLAYED_TIME=30000;
	
	private long timeout;

	public Skull(Context appContext) {
		super(appContext);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected int getTextureID() {
		return TextureTable.SKULL;
	}

	@Override
	public int collectedScoreTransform(int oldScore) {
		return Math.round(0.5f*oldScore);
	}

	@Override
	public boolean isGrowNeeded() {
		return false;
	}

	@Override
	public boolean isTimedOut() {
		return System.currentTimeMillis()>=timeout;
	}

	@Override
	public void setTimeout(Random random) {
		timeout=System.currentTimeMillis() + MIN_DISPLAYED_TIME + random.nextInt(MAX_DISPLAYED_TIME - MIN_DISPLAYED_TIME + 1);	
	}

}
