package com.szofttech.snake;

import java.nio.FloatBuffer;
import java.security.InvalidParameterException;
import java.util.Iterator;
import android.content.Context;
import android.graphics.Point;
import android.opengl.GLES20;
import android.opengl.Matrix;

public class Snake extends ActiveGameObject{
	OpenGLProgram program;
	
	private FloatBuffer vertexData;
	
	public enum Direction {UP, DOWN, RIGHT, LEFT, UNCHANGED};
	
	private int mvpMatrixHandle;
	private int positionHandle;
	private int textureShaderHandle;
	private int coordinateShiftHandle;
	private int colorHandle;
	private int rotateVectorHandle;
	
	private boolean dead;
	
		
	private final int POS_SIZE = 2;
	private final int STRIDE = POS_SIZE * BYTES_PER_FLOAT;
	
	PointList snakePoints;
	
	private GLColor color=null;
	
	public static Direction getOppositeDirection(Direction d){
		switch (d){
			case UP:
				return Direction.DOWN;
			case DOWN:
				return Direction.UP;
			case LEFT:
				return Direction.RIGHT;
			case RIGHT:
				return Direction.LEFT;
			default:
				throw new IllegalArgumentException("Invalid direction");
		}
	}
	
	public synchronized void setColor(GLColor color){
		this.color=color;
	}

	public synchronized void setColor(int color){
		if (this.color==null)
			this.color=new GLColor(color);
		else
			this.color.set(color);
	}
	
	public synchronized void setDead(boolean dead){
		this.dead=dead;
	}
	
	public synchronized boolean isDead(){
		return dead;
	}

	
	public Snake(Context appContext) {
		super(appContext);
		vertexData=null;
		snakePoints=new PointList();
		dead=true;
		
		Matrix.setIdentityM(modelMatrix, 0);
		Matrix.translateM(modelMatrix, 0, CoordinateManager.getInstance().getCellX(0) + 0.5f,
	   		 CoordinateManager.getInstance().getCellY(0)+0.5f, 0.0f);
	}
	
	public synchronized void addPoint(Point p){
		snakePoints.add(ObjectPool.getInstance().copyPoint(p));
	}
	
	public synchronized void clearPoints(){
		snakePoints.clear();
	}
	
	private Point getNextPosition(Direction dir){
		Point result=ObjectPool.getInstance().getPoint();
		Point first=snakePoints.getFirst();
		
		result.set(first.x, first.y);
		
		Direction snakeDirection=getSnakeDirection();
		
		if (dir==Direction.UNCHANGED){
			dir=snakeDirection;
		} else {
			Direction opposite=getOppositeDirection(dir);
			if (snakeDirection.equals(opposite)){
				dir=snakeDirection;
			}
		}
		
		switch(dir){
			case UP:
				result.y++;
				break;
			case DOWN:
				result.y--;
				break;
			case LEFT:
				result.x--;
				break;
			case RIGHT:
				result.x++;
				break;
			case UNCHANGED:
				throw new RuntimeException("This cannot happen!");
		}
		
		return result;
	}
	
	public synchronized Point getFuturePosition(Direction dir){
		return getNextPosition(dir);
	}
	
	public synchronized boolean isSnakeValid(){
		return (!dead) && (snakePoints.size()>=2);
	}
	
	public synchronized void move(Direction dir, boolean grow){
		if (dead)
			return;
		
		snakePoints.addFirst(getNextPosition(dir));
		if (!grow){
			Point p=snakePoints.pollLast();
			ObjectPool.getInstance().putPoint(p);
		}
	}
	
	
	private void buildVertexData(){
		//One cell sized box.
		float []vertexData={
			-0.5f,-0.5f,
			 -0.5f, 0.5f,
			 0.5f, -0.5f,
			 0.5f, 0.5f,
		};
		
		this.vertexData=createFloatBufferFromData(vertexData);
	}
	
	
	@Override
	public void init() {
		program=new OpenGLProgram(appContext, R.raw.snake_vert, R.raw.snake_frag);
		
		mvpMatrixHandle = program.getUniformLocation("u_MVPMatrix");        
	    positionHandle = program.getAttributeLocation("a_Position");
	    textureShaderHandle = program.getUniformLocation("u_Texture");
	    coordinateShiftHandle = program.getUniformLocation("coordinateShift");
	    colorHandle = program.getUniformLocation("color");
	    rotateVectorHandle = program.getUniformLocation("rotateVector");
	    
	    buildVertexData();	 
	}
	
	@Override
	public void renderPrepare(long time) {
	}
	
	private void rotateTexture(Direction dir){
		switch (dir){
			case DOWN:
				GLES20.glUniform4f( rotateVectorHandle, 0.0f, -1.0f, 1.0f, 0.0f);
				break;
			case UP:
				GLES20.glUniform4f( rotateVectorHandle, 0.0f, 1.0f, 1.0f, 0.0f);
				break;
			case RIGHT:
				GLES20.glUniform4f( rotateVectorHandle, 1.0f, 0.0f, 0.0f, 1.0f);
				break;
			case LEFT:
				GLES20.glUniform4f( rotateVectorHandle, -1.0f, 0.0f, 0.0f, 1.0f);
				break;
			default:
				throw new InvalidParameterException("Cannot rotate texture to unknown direction");
		}
	}
	
	private void rotateBasedOnDirectionVector(int dx, int dy){
		rotateTexture(getDirectionVector(dx,dy));
	}
	
	private Direction getDirectionVector(int dx, int dy){
		if ((dx==0) && (dy==-1))
        	return Direction.DOWN;
        else if ((dx==0) && (dy==1))
        	return Direction.UP;
        else if ((dy==0) && (dx==1))
        	return Direction.RIGHT;
        else if ((dy==0) && (dx==-1))
        	return Direction.LEFT;
        else
        	throw new RuntimeException("Pieces of snake must be tied together.");
	}
	
	private Direction getSnakeDirection(){
		Iterator<Point> itr = snakePoints.iterator();
        Point first=itr.next();
        Point pos=itr.next();
        
        int dx=first.x-pos.x;
        int dy=first.y-pos.y;
        
        return getDirectionVector(dx,dy);        
	}
	
	private void drawSnakeSegment(Point p){
		GLES20.glUniform2f( coordinateShiftHandle, p.x, p.y);
    	GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
	}

	@Override
	public synchronized void render() {
		if (vertexData == null)
			return;
		
			
		// Pass in the position information
        GLES20.glVertexAttribPointer(positionHandle, POS_SIZE, GLES20.GL_FLOAT, false,
        		STRIDE, vertexData);        
                
        //Set vertex data
        GLES20.glEnableVertexAttribArray(positionHandle);        
        GLES20.glLineWidth(2.0f);
        
        
        // Set the active texture unit to texture unit 0.
	    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
	    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, TextureTable.getTextureHandle(TextureTable.SNAKE_FRONT));
	    GLES20.glUniform1i(textureShaderHandle, 0);
	    
        
        //Draw first part of the snake
        if (snakePoints.size()>=2){
            Iterator<Point> itr = snakePoints.iterator();
	        Point first=itr.next();
	        Point pos=itr.next();
	        
	        int dx=first.x-pos.x;
	        int dy=first.y-pos.y;
	        
	        rotateBasedOnDirectionVector(dx,dy);
	       

	        //Set color
	        if (dead){
	        	GLES20.glUniform4f( colorHandle, 0.5f*color.r, 0.5f*color.g, 0.5f*color.b, 1.0f);
	        } else {
	        	GLES20.glUniform4f( colorHandle, 0.5f*color.r+0.5f, 0.5f*color.g+0.5f, 0.5f*color.b+0.5f, 1.0f);
	        }
	        
	        drawSnakeSegment(first);
	        
	        //Set color
	        if (!dead)
	        	GLES20.glUniform4f( colorHandle, color.r, color.g, color.b, 1.0f);
	      		
	        
	    	//Set the active texture unit to texture unit 0.
		    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, TextureTable.getTextureHandle(TextureTable.SNAKE));
		    GLES20.glUniform1i(textureShaderHandle, 0);
	    	
	        while (true){
	        	drawSnakeSegment(pos);
	        	        	
	        	if (!itr.hasNext())
	        		break;
	        	
	        	pos=itr.next();
	        }
        }
               
	}

	@Override
	public void resize(int h, int w) {
	}
	
	
	@Override
	public int getMVPMatrixHandle() {
		return mvpMatrixHandle;
	}

	@Override
	public void useProgram() {
		program.load();
	}
	
	@Override
	public synchronized int getDistanceSquared(final Point position) {
		int minDistance=Integer.MAX_VALUE;
		
		Iterator<Point> itr = snakePoints.iterator();
		while (itr.hasNext()){
			Point p=itr.next();
			minDistance=Math.min(minDistance, Helpers.pointDistanceSquared(p, position));
			
			if (minDistance==0)
				break;
		}
		
		return minDistance;
	}
}
