package com.szofttech.snake;

import android.content.Context;
import android.graphics.Point;

public abstract class ActiveGameObject extends Renderable {

	public ActiveGameObject(Context appContext) {
		super(appContext);
	}
	
	public abstract int getDistanceSquared(final Point position);
}
