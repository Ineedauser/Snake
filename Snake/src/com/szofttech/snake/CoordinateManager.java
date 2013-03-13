package com.szofttech.snake;

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
	
	static private CoordinateManager instance=null;
	
	public static CoordinateManager getInstance(){
		if (instance == null)
			instance = new CoordinateManager();
		
		return instance;
	}
	
	private CoordinateManager(){
	}
	
	private void caclulate(){
		
		
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
			bottomWithOffset=bottom*sceneRatio;
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
	
	RectF getOpenGLBoundaries(){
		return new RectF(left, -bottom, -left, bottom);
	}
	
	PointF getCellCorner(int x, int y){
		return new PointF(leftWithOffset+x, bottomWithOffset+y);
	}
}
