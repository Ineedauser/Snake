package com.szofttech.snake;

import java.util.LinkedList;

import android.graphics.Point;

public class PointList extends LinkedList<Point> {
	private static final long serialVersionUID = -2702012128730431603L;

	@Override
	public void clear(){
		ObjectPool op=ObjectPool.getInstance();
		
		while (!isEmpty()){
			op.putPoint(pop());
		}
	}
}
