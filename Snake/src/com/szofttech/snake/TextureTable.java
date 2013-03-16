package com.szofttech.snake;

import android.content.Context;

/*
 * Static table for textures. Do not create a separate texture
 * for all objects of same kind.
 * 
 */
public class TextureTable {
	static final private int N_TEXTURES = 3;
	
	static final public int CHERRY = 0;
	static final public int SNAKE = 1;
	static final public int SNAKE_FRONT = 2;
	
	static private int[] resources={R.drawable.cseresznye, R.drawable.snakesegment, R.drawable.snakesegment_eye};
	
	static private OpenGLTexture[] textures=null;
	
	static public void init(final Context context){
		if (textures == null){
			textures=new OpenGLTexture[N_TEXTURES];
			for (int a=0; a<N_TEXTURES; a++){
				textures[a]=new OpenGLTexture(context, resources[a]);
			}
		} else {
			for (int a=0; a<N_TEXTURES; a++){
				textures[a].load(context, resources[a]);
			}
		}
	}
	
	public static int getTextureHandle(int identifier){
		if ((identifier<0) || (identifier >= N_TEXTURES))
			throw new RuntimeException("Invalid argument for getTexture()");
		
		if (textures==null)
			throw new RuntimeException("Trying to get texture ID before initializing it.");
		
		return textures[identifier].getHandle();
	}
}
