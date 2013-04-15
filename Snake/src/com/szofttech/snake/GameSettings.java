package com.szofttech.snake;

import java.io.Serializable;

public class GameSettings implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2562228210347686054L;
	
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
