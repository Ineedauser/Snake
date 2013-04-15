package com.szofttech.snake;

import java.io.Serializable;

public class UserRegisterPacket  implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6812149278288276565L;
	
	public String name;
	public int color;
	public int id;
}
