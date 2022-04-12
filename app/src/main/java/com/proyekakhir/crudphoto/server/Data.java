package com.proyekakhir.crudphoto.server;

import com.google.gson.annotations.SerializedName;

public class Data{

	@SerializedName("password")
	private String password;

	@SerializedName("level")
	private String level;

	@SerializedName("photo")
	private String photo;

	@SerializedName("username")
	private String username;

	public String getPassword(){
		return password;
	}

	public String getLevel(){
		return level;
	}

	public String getPhoto(){
		return photo;
	}

	public String getUsername(){
		return username;
	}
}