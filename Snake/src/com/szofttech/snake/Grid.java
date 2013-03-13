package com.szofttech.snake;

import java.nio.FloatBuffer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.opengl.GLES20;

public class Grid extends Renderable{
	
	OpenGLProgram program;

	private FloatBuffer vertexData;
	
	int mvpMatrixHandle;
	int positionHandle;
	int colorHandle;
	int nLines;
	
	private GLColor color;
	
	private final int POS_SIZE = 2;
	private final int STRIDE = POS_SIZE * BYTES_PER_FLOAT;
	private final int FLOATS_PER_LINE = 4;
	

	public Grid(Context appContext) {
		super(appContext);
		color = new GLColor();
		vertexData=null;
	}
	
	void setColor(GLColor color){
		this.color=color;
	}
	
	void setColor(int color){
		this.color.set(color);
	}
	
	void buildVertexData(){
		CoordinateManager cm=CoordinateManager.getInstance();
		Point mapDimensions=cm.getMapDimensions();
		PointF bottomLeft=cm.getCellCorner(0, 0);
		RectF mapBoundaries=cm.getMapBoundaries();
		
		float []vertexData;
		
		nLines=mapDimensions.x + mapDimensions.y + 2;	
		vertexData=new float[nLines*FLOATS_PER_LINE];
		

		float accumulator=bottomLeft.x;
		for (int x=0; x<=mapDimensions.x; x++){
			vertexData[x*FLOATS_PER_LINE]=accumulator;
			vertexData[x*FLOATS_PER_LINE+1]=mapBoundaries.top;
			vertexData[x*FLOATS_PER_LINE+2]=accumulator;
			vertexData[x*FLOATS_PER_LINE+3]=mapBoundaries.bottom;
			
			accumulator=accumulator + 1.0f;
		}

		
		accumulator=bottomLeft.y;
		int offset=(mapDimensions.x+1)*FLOATS_PER_LINE;
		
		for (int y=0; y<=mapDimensions.y; y++){			
			vertexData[offset+y*FLOATS_PER_LINE]=mapBoundaries.left;
			vertexData[offset+y*FLOATS_PER_LINE+1]=accumulator;
			vertexData[offset+y*FLOATS_PER_LINE+2]=mapBoundaries.right;
			vertexData[offset+y*FLOATS_PER_LINE+3]=accumulator;
			
			accumulator=accumulator + 1.0f;
		}
		
		this.vertexData=createFloatBufferFromData(vertexData);
	}
	
	
	@Override
	public void init() {
		program=new OpenGLProgram(appContext, R.raw.singlecolor_vert, R.raw.passthrough_frag);
		
		mvpMatrixHandle = program.getUniformLocation("u_MVPMatrix");        
	    positionHandle = program.getAttributeLocation("a_Position");
	    colorHandle = program.getUniformLocation("u_Color");
	}
	
	@Override
	public void renderPrepare(long time) {
		
	}

	@Override
	public void render() {
		if (vertexData == null)
			return;
		
		GLES20.glUniform4f( colorHandle, color.r, color.g, color.b, 1.0f);
		
		// Pass in the position information
		//vertexData.position(8);
        GLES20.glVertexAttribPointer(positionHandle, POS_SIZE, GLES20.GL_FLOAT, false,
        		STRIDE, vertexData);        
                
        GLES20.glEnableVertexAttribArray(positionHandle);        
        
        GLES20.glLineWidth(2.0f);
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, nLines*2); 
	}

	@Override
	public void resize(int h, int w) {
		buildVertexData();
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
