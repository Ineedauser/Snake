package com.szofttech.snake;

import java.nio.FloatBuffer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;

public class Collectable  extends Renderable{
	OpenGLProgram program;
	
	private FloatBuffer vertexData;
	
	int mvpMatrixHandle;
	int positionHandle;
	int textureShaderHandle;
	int nLines;
	
	private GLColor color;
	OpenGLTexture texture;
	
	private final int POS_SIZE = 2;
	private final int STRIDE = POS_SIZE * BYTES_PER_FLOAT;
	

	public Collectable(Context appContext) {
		super(appContext);
		color = new GLColor(Color.GREEN);
		vertexData=null;
	}
	
	void setColor(GLColor color){
		this.color=color;
	}
	
	void setColor(int color){
		this.color.set(color);
	}
	
	void buildVertexData(){
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
	 
	}
	
	@Override
	public void renderPrepare(long time) {
		float angleInDegrees = (360.0f / 10000.0f) * ((int) time);
         
        // Draw the triangle facing straight on.
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, 0.5f, 0.5f, 0.0f);
        Matrix.rotateM(modelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f);   
    
	}

	@Override
	public void render() {
		if (vertexData == null)
			return;
		
		// Set the active texture unit to texture unit 0.
	    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
	    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, TextureTable.getTextureHandle(TextureTable.CHERRY));
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
