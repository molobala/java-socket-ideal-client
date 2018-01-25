package com.molo.socket.ideal.client;

import com.fasterxml.jackson.annotation.JsonIgnore;


public class MediaFile {
	//File file;
	public String type;
	public long size;
	public String name;
	@JsonIgnore
	public  String absolutePath;
}
