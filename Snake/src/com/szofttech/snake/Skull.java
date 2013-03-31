package com.szofttech.snake;

import android.content.Context;

public class Skull extends Collectable{

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

}
