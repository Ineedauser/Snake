package com.szofttech.snake;

import java.util.ArrayList;
import java.util.Iterator;

import android.graphics.Point;

public class CollectableList extends ArrayList<Collectable> {
	private static final long serialVersionUID = 2271960335980929177L;
	
	
	int getDistanceSquared(final Point p){
		int minDistance=Integer.MAX_VALUE;
		
		Iterator<Collectable> i= iterator();
		while (i.hasNext()){
			Collectable c=i.next();
			minDistance=Math.min(minDistance, c.getDistanceSquared(p));
		}
		
		return minDistance;
	}
	
}
