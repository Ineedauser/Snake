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

}
