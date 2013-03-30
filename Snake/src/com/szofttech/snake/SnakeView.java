package com.szofttech.snake;

import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

public class SnakeView extends GLSurfaceView {
	private final Game game;
	private final PointF downPoint;
	private static final float TOUCH_THRESHOLD=1.5f;
	private boolean waitForTouchUp;
	
	public SnakeView(final Game game) {
		super(game.context);
		this.game=game;
		downPoint=new PointF();
		waitForTouchUp=false;
		
		setEGLContextClientVersion(2);
		setRenderer(game.renderer);
	}

	
	@Override
	public boolean onTouchEvent(MotionEvent e) {
		float x = e.getX();
	    float y = e.getY();
	    
	   /* switch (e.getAction()) {
       		case MotionEvent.ACTION_MOVE:
       			Log.w("          SNAKE       ","MOVE x:"+x+" y:"+y);
       			break;
       			
       		case MotionEvent.ACTION_DOWN:
       			Log.w("          SNAKE       ","DOWN x:"+x+" y:"+y);
       			break;
       			
       		case MotionEvent.ACTION_UP:
       			Log.w("          SNAKE       ","UP x:"+x+" y:"+y);
       			break;
	    }*/
	    
	    if (waitForTouchUp){
	    	if (e.getAction()==MotionEvent.ACTION_UP)
	    		waitForTouchUp=false;
	    	
	    	return true;
	    }
	    
	    switch (e.getAction()) {
		    case MotionEvent.ACTION_MOVE:
	   			float dx=downPoint.x-x;
	   			float dy=downPoint.y-y;
	   			
	   			
	   			if (Math.abs(dx/dy)>TOUCH_THRESHOLD){
	   				if (dx>0)
	   					game.networkManager.putLocalDirection(Snake.Direction.UP);
	   				else
	   					game.networkManager.putLocalDirection(Snake.Direction.DOWN);
	   				waitForTouchUp=true;
	   			} else if (Math.abs(dy/dx)>TOUCH_THRESHOLD){
	   				if (dy>0)
	   					game.networkManager.putLocalDirection(Snake.Direction.RIGHT);
	   				else
	   					game.networkManager.putLocalDirection(Snake.Direction.LEFT);
	   				waitForTouchUp=true;
	   			}
	   			break;
	   			
	   		case MotionEvent.ACTION_DOWN:
	   			downPoint.set(x, y);
	   			break;
   			
	    }
		return true;
	}
}