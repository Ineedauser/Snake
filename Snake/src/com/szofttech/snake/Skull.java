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

}
