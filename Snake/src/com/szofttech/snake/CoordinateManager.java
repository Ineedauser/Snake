package com.szofttech.snake;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;

/**
 * Class responsible for calculating grid parameters.
 * 
 * It is a SINGLETON.
 *
 */
public class CoordinateManager {
	private float ratio;
	private int boardSizeSmaller;
	private int boardSizeBigger;
	private float sceneRatio;
	
	private float bottom;
	private float left;
	private float bottomWithOffset;
	private float leftWithOffset;
	private int mapSizeX;
	private int mapSizeY;
	
	static private CoordinateManager instance=null;
	
	public static CoordinateManager getInstance(){
		if (instance == null)
			instance = new CoordinateManager();
		
		return instance;
	}
	
	private CoordinateManager(){
	}
	
	private void caclulate(){
		if (ratio>1.0f){
			mapSizeX=boardSizeSmaller;
			mapSizeY=boardSizeBigger;
		} else {
			mapSizeY=boardSizeSmaller;
			mapSizeX=boardSizeBigger;		
		}
			
			
		if (ratio < sceneRatio){
			//Set coordinates based on the smaller side
			bottom=-(float)boardSizeSmaller/2.0f;
			bottomWithOffset=bottom;
			left=bottom/ratio;
			leftWithOffset=bottom/sceneRatio;
		} else {
			left=-(float)boardSizeBigger/2.0f;
			leftWithOffset=left;
			bottom=left*ratio;
			bottomWithOffset=left*sceneRatio;
		}
	}
	
	public void resizeScreen(int h, int w){
		ratio=(float)w/h;
		caclulate();
	}
	
	public void resizeMap(int s1, int s2){
		boardSizeSmaller=Math.min(s1, s2);
		boardSizeBigger=Math.max(s1, s2);
		sceneRatio=(float)boardSizeSmaller/boardSizeBigger;
		caclulate();
	}
	
	public RectF getOpenGLBoundaries(){
		return new RectF(left, -bottom, -left, bottom);
	}
	
	public RectF getMapBoundaries(){
		return new RectF(leftWithOffset, -bottomWithOffset, -leftWithOffset, bottomWithOffset);
	}
	
	public float getCellX(int x){
		return leftWithOffset+x;
	}
	
	public float getCellY(int y){
		return bottomWithOffset+y;
	}
	
	public PointF getCellCorner(int x, int y){
		return new PointF(getCellX(x), getCellY(y));
	}
	
	
	/**
	 * DO NOT FORGET TO PUT THE RESULTING POINT BACK IN THE OBJECT POOL.
	 * 
	 * @return The size of the map.
	 */
	public Point getMapDimensions(){
		Point result=ObjectPool.getInstance().getPoint();
		result.set(mapSizeX, mapSizeY);
		return result;
	}
	
	public boolean isValidPosition(Point p){
		return (p.x>=0 && p.y>=0 && p.x<mapSizeX && p.y<mapSizeY);
	}
	
	
}
