package com.szofttech.snake;

public class GLColor {
	public float r;
	public float g;
	public float b;
	
	void set(int color){
		b=(float)(color & 0xFF)/255.0f;
		g=(float)((color >> 8) & 0xFF)/255.0f;
		r=(float)((color >> 16) & 0xFF)/255.0f;
	}
	
	void set(float r, float g, float b){
		this.r=r;
		this.g=g;
		this.b=b;
	}
	
	public GLColor(int color){
		set(color);
	}
	
	public GLColor(float r, float g, float b){
		set(r,g,b);
	}
	
	public GLColor(){
	}
}
