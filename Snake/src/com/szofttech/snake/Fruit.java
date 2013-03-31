package com.szofttech.snake;

import android.content.Context;

public class Fruit extends Collectable{	
	public Fruit(Context appContext) {
		super(appContext);
	}

	@Override
	protected int getTextureID() {
		return TextureTable.CHERRY;
	}

	@Override
	public int collectedScoreTransform(int oldScore) {
		return oldScore+10;
	}

	@Override
	public boolean isGrowNeeded() {
		return true;
	}

}
