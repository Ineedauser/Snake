package com.szofttech.snake;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import android.content.Context;
import android.graphics.Point;
import android.opengl.GLES20;
import android.opengl.Matrix;

public class Snake  extends Renderable{
	OpenGLProgram program;
	
	private FloatBuffer vertexData;
	
	public enum Direction {UP, DOWN, RIGHT, LEFT};
	
	int mvpMatrixHandle;
	int positionHandle;
	int textureShaderHandle;
	int coordinateShiftHandle;
	int colorHandle;
	int rotateVectorHandle;
		
	private final int POS_SIZE = 2;
	private final int STRIDE = POS_SIZE * BYTES_PER_FLOAT;
	
	LinkedList<Point> snakePoints;
	
	private GLColor color=null;
	
	public synchronized void setColor(GLColor color){
		this.color=color;
	}

	public synchronized void setColor(int color){
		if (this.color==null)
			this.color=new GLColor(color);
		else
			this.color.set(color);
	}
	

	
	public Snake(Context appContext) {
		super(appContext);
		vertexData=null;
		snakePoints=new LinkedList<Point>();
		
		Matrix.setIdentityM(modelMatrix, 0);
		Matrix.translateM(modelMatrix, 0, CoordinateManager.getInstance().getCellX(0) + 0.5f,
	   		 CoordinateManager.getInstance().getCellY(0)+0.5f, 0.0f);
	}
	
	public synchronized void addPoint(Point p){
		snakePoints.add(p);
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
		}
	}
	
	private void rotateBasedOnDirectionVector(int dx, int dy){
		 if ((dx==0) && (dy==-1))
        	rotateTexture(Direction.DOWN);
        else if ((dx==0) && (dy==1))
        	rotateTexture(Direction.UP);
        else if ((dy==0) && (dx==1))
        	rotateTexture(Direction.RIGHT);
        else if ((dy==0) && (dx==-1))
        	rotateTexture(Direction.LEFT);
        else
        	throw new RuntimeException("Pieces of snake must be tied together.");
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
        if (snakePoints.size()<2)
        	throw new RuntimeException("Snake must be at least 2 long.");
                    
        Iterator<Point> itr = snakePoints.iterator();
        Point first=itr.next();
        Point pos=itr.next();
        
        int dx=first.x-pos.x;
        int dy=first.y-pos.y;
        
        rotateBasedOnDirectionVector(dx,dy);
       
        
      //Set color
      		GLES20.glUniform4f( colorHandle, 0.5f*color.r+0.5f, 0.5f*color.g+0.5f, 0.5f*color.b+0.5f, 1.0f);
      		
        drawSnakeSegment(first);
        
      //Set color
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
}
