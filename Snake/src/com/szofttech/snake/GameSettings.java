package com.szofttech.snake;

public class GameSettings {
	public int width;
	public int height;
	public int stepTime;
	public float starProbability;
	public float skullProbability;
	
	public void copyFrom(final GameSettings other){
		this.width=other.width;
		this.height=other.height;
		this.stepTime=other.stepTime;
		this.starProbability=other.starProbability;
		this.skullProbability=other.skullProbability;
	}
}
