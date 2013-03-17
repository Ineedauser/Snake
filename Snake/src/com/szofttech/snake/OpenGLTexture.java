package com.szofttech.snake;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

public class OpenGLTexture {
	final int[] textureHandle = new int[1];
	
	public void load(final Bitmap bitmap){
		GLES20.glGenTextures(1, textureHandle, 0);
	    if (textureHandle[0] != 0){	        
	    	GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
	 
	        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
	        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
	 
	        // Load the bitmap into the bound texture.
	        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
	        
	        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
	    }
	 
	    if (textureHandle[0] == 0){
	        throw new RuntimeException("Error loading texture.");
	    }
	}
	
	public void load(final Context context, final int resource){  
    	final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inScaled = false;
		    
        final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resource, options);       
        
        load(bitmap);
        
        bitmap.recycle();
	}
	
	public OpenGLTexture(){
	}
	
	public OpenGLTexture(final Bitmap bitmap){
		load(bitmap);
	}
	
	
	public OpenGLTexture(final Context context, final int resource){    
		GLES20.glGenTextures(1, textureHandle, 0);
    	load(context, resource);
	}
	
	
	
	public int getHandle(){
		return textureHandle[0];
	}
}
