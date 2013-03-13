package com.szofttech.snake;

import java.nio.FloatBuffer;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

public class Grid extends Renderable{
	
	OpenGLProgram program;

	private FloatBuffer vertexData;
	
	int mvpMatrixHandle;
	int positionHandle;
	int colorHandle;
	int nLines;
	
	
	private final int POS_SIZE = 2;
	private final int STRIDE = 2 * BYTES_PER_FLOAT;
	private final int FLOATS_PER_LINE = 4;
	

	public Grid(Context appContext) {
		super(appContext);
		vertexData=null;
	}
	
	void buildVertexData(){
		CoordinateManager cm=CoordinateManager.getInstance();
		Point mapDimensions=cm.getMapDimensions();
		PointF bottomLeft=cm.getCellCorner(0, 0);
		RectF mapBoundaries=cm.getMapBoundaries();
		
		float []vertexData;
		
		nLines=mapDimensions.x + mapDimensions.y + 2;	
		vertexData=new float[nLines*FLOATS_PER_LINE];
		
		Log.w("SNAKE -------:", "left: "+mapBoundaries.left+" right: "+mapBoundaries.right+" top: "+mapBoundaries.top+" bottom: "+mapBoundaries.bottom);
		
		float accumulator=bottomLeft.x;
		for (int x=0; x<=mapDimensions.x; x++){

			Log.w("SNAKE ---------:", "X: "+accumulator);
			
			vertexData[x*FLOATS_PER_LINE]=accumulator;
			vertexData[x*FLOATS_PER_LINE+1]=mapBoundaries.top-0.001f;
			vertexData[x*FLOATS_PER_LINE+2]=accumulator;
			vertexData[x*FLOATS_PER_LINE+3]=mapBoundaries.bottom+0.001f;
			
			accumulator=accumulator + 1.0f;
		}
		
		accumulator=bottomLeft.y;
		int offset=(mapDimensions.x+1)*FLOATS_PER_LINE;
		for (int y=0; y<=mapDimensions.y; y++){
			Log.w("SNAKE ---------:", "Y: "+accumulator);
			
			vertexData[offset+y*FLOATS_PER_LINE]=mapBoundaries.left+0.001f;
			vertexData[offset+y*FLOATS_PER_LINE+1]=accumulator;
			vertexData[offset+y*FLOATS_PER_LINE+2]=mapBoundaries.right-0.001f;
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
		//float angleInDegrees = (360.0f / 10000.0f) * ((int) time);
        
        // Draw the triangle facing straight on.
        //Matrix.setIdentityM(modelMatrix, 0);
       // Matrix.rotateM(modelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);
	}

	@Override
	public void render() {
		if (vertexData == null)
			return;
		
		GLES20.glUniform4f( colorHandle, 1.0f, 0.0f, 0.0f, 1.0f);
		
		// Pass in the position information
		//vertexData.position(8);
        GLES20.glVertexAttribPointer(positionHandle, POS_SIZE, GLES20.GL_FLOAT, false,
        		STRIDE, vertexData);        
                
        GLES20.glEnableVertexAttribArray(positionHandle);        
        
       GLES20.glLineWidth(2.0f);
       
        // GLES20.glEnable(GLES20.GL_LIN)
        
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, nLines*2); 
		// TODO Auto-generated method stub
		
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
