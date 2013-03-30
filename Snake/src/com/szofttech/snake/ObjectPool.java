package com.szofttech.snake;

import java.util.LinkedList;

import android.graphics.Point;

public class ObjectPool {
	private LinkedList <Point> pointPool;
	
	static private ObjectPool instance=null;
	
	private ObjectPool(){
		pointPool=new LinkedList<Point>();
	}
	
	
	static public ObjectPool getInstance(){
		if (instance==null)
			instance=new ObjectPool();
		return instance;
	}
	
	public Point getPoint(){
		if (pointPool.isEmpty()){
			return new Point();
		} else
			return pointPool.getFirst();
	}
	
	public void putPoint(Point p){
		pointPool.addFirst(p);
	}
	
	void clear(){
		pointPool.clear();
	}
}
