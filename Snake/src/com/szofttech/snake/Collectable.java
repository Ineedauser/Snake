package com.szofttech.snake;

import java.nio.FloatBuffer;

import android.content.Context;
import android.graphics.Point;
import android.opengl.GLES20;
import android.opengl.Matrix;

public abstract class Collectable  extends Renderable{
	OpenGLProgram program;
	
	private FloatBuffer vertexData;
	
	int mvpMatrixHandle;
	int positionHandle;
	int textureShaderHandle;
		
	private final int POS_SIZE = 2;
	private final int STRIDE = POS_SIZE * BYTES_PER_FLOAT;
	
	private Point position;
	
	abstract protected int getTextureID();
	
	public Collectable(Context appContext) {
		super(appContext);
		vertexData=null;
		setPosition(new Point(0,0));
	}
	
	
	public synchronized void setPosition(Point position){
		this.position=position;
	}
	
	public synchronized void setPosition(int x, int y){
		position.x=x;
		position.y=y;
	}
	
	
	void buildVertexData(){
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
		program=new OpenGLProgram(appContext, R.raw.simple_texture_vert, R.raw.texture_frag);
		
		mvpMatrixHandle = program.getUniformLocation("u_MVPMatrix");        
	    positionHandle = program.getAttributeLocation("a_Position");
	    textureShaderHandle = program.getUniformLocation("u_Texture");
	    
	    buildVertexData();	 
	}
	
	@Override
	public void renderPrepare(long time) {
		float angleInDegrees = (360.0f / 10000.0f) * ((int) time);
        
		CoordinateManager cm=CoordinateManager.getInstance();
		
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, cm.getCellX(position.x)+0.5f, cm.getCellY(position.y)+0.5f, 0.0f);
        Matrix.rotateM(modelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f);   
    
	}

	@Override
	public synchronized void render() {
		if (vertexData == null)
			return;
		
		// Set the active texture unit to texture unit 0.
	    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
	    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, TextureTable.getTextureHandle(getTextureID()));
	    GLES20.glUniform1i(textureShaderHandle, 0);
		
		
		// Pass in the position information
        GLES20.glVertexAttribPointer(positionHandle, POS_SIZE, GLES20.GL_FLOAT, false,
        		STRIDE, vertexData);        
                
        GLES20.glEnableVertexAttribArray(positionHandle);        
        
        GLES20.glLineWidth(2.0f);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4); 
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
