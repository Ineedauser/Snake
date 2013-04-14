package com.szofttech.snake;

public class User {
	public String name;
	public int score;
	public GLColor color;
	
	public void copyFrom(final User other){
		this.name=other.name;
		this.score=other.score;
		this.color=other.color;
	}
}
