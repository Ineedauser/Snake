package com.szofttech.snake;

import java.util.LinkedList;

import android.content.Context;
import android.graphics.Point;

public class ObjectPool {
	private LinkedList <Point> pointPool;
	private LinkedList <Fruit> fruitPool;
	private LinkedList <Skull> skullPool;
	private LinkedList <Star> starPool;
	
	static private ObjectPool instance=null;
	
	private ObjectPool(){
		pointPool=new LinkedList<Point>();
		fruitPool=new LinkedList<Fruit>();
		skullPool=new LinkedList<Skull>();
		starPool=new LinkedList<Star>();
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
	

	
	
	public Fruit getFruit(final Context appContext){
		if (fruitPool.isEmpty()){
			return new Fruit(appContext);
		} else
			return fruitPool.pop();
	}
	
	public void putFruit(Fruit f){
		fruitPool.push(f);
	}
	
	public Star getStar(final Context appContext){
		if (starPool.isEmpty()){
			return new Star(appContext);
		} else
			return starPool.pop();
	}
	
	public void putStar(Star f){
		starPool.push(f);
	}
	
	public Skull getSkull(final Context appContext){
		if (skullPool.isEmpty()){
			return new Skull(appContext);
		} else
			return skullPool.pop();
	}
	
	public void putSkull(Skull f){
		skullPool.push(f);
	}
	
	public void putCollectable(Collectable c){
		if (c instanceof Fruit)
			putFruit((Fruit)c);
		else if (c instanceof Skull)
			putSkull((Skull)c);
		else if (c instanceof Star)
			putStar((Star)c);
		else
			throw new IllegalArgumentException("Invalid collectable type");
	}
	
	
	
	
	
	
	void clear(){
		pointPool.clear();
		fruitPool.clear();
		skullPool.clear();
		starPool.clear();
	}
}
