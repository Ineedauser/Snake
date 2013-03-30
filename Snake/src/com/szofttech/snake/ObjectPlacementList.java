package com.szofttech.snake;

import java.util.LinkedList;

public class ObjectPlacementList extends LinkedList<NewObjectPlacement>{
	private static final long serialVersionUID = -5432144104922988907L;
	
	
	@Override
	public void clear(){
		ObjectPool op=ObjectPool.getInstance();
		
		while (!isEmpty()){
			op.putNewObjectPlacement(pop());
		}
	}
	
}
