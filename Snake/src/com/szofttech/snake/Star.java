package com.szofttech.snake;

import android.content.Context;

public class Star extends Collectable{

	public Star(Context appContext) {
		super(appContext);
	}

	@Override
	protected int getTextureID() {
		return TextureTable.STAR;
	}

}
