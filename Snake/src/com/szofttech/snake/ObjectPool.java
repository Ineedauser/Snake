package com.szofttech.snake;

import java.util.LinkedList;

import android.graphics.Point;

public class ObjectPool {
	private LinkedList <Point> pointPool;
	private LinkedList <NewObjectPlacement> newObjectPlacementPool;
	
	static private ObjectPool instance=null;
	
	private ObjectPool(){
		pointPool=new LinkedList<Point>();
		newObjectPlacementPool=new LinkedList<NewObjectPlacement>();
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
			return pointPool.pop();
	}
	
	public void putPoint(Point p){
		pointPool.addFirst(p);
	}
	
	public Point copyPoint(final Point p){
		Point result=getPoint();
		result.set(p.x, p.y);
		return result;
	}
	
	void clear(){
		pointPool.clear();
		newObjectPlacementPool.clear();
	}
	
	
	public NewObjectPlacement getNewObjectPlacement(){
		if (newObjectPlacementPool.isEmpty()){
			return new NewObjectPlacement();
		} else
			return newObjectPlacementPool.pop();
	}
	
	
	public void putNewObjectPlacement(NewObjectPlacement p){
		newObjectPlacementPool.addFirst(p);
	}
}
