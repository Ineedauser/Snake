package com.szofttech.snake;

import java.util.ArrayList;
import java.util.Iterator;

import android.graphics.Point;
import android.util.Log;

public class CollectableList extends ArrayList<Collectable> {
	private static final long serialVersionUID = 2271960335980929177L;
	
	
	public int getDistanceSquared(final Point p){
		int minDistance=Integer.MAX_VALUE;
		
		Iterator<Collectable> i= iterator();
		while (i.hasNext()){
			Collectable c=i.next();
			minDistance=Math.min(minDistance, c.getDistanceSquared(p));
		}
		
		return minDistance;
	}
	
	public Collectable findByPos(final Point pos){
		Iterator<Collectable> i= iterator();
		while (i.hasNext()){
			Collectable c=i.next();
			Point collectablePos=c.getPosition();
			
			if (pos.equals(collectablePos)){
				ObjectPool.getInstance().putPoint(collectablePos);
				return c;
			}
			
			ObjectPool.getInstance().putPoint(collectablePos);
			
		}
		
		return null;
	}
	
	
}
