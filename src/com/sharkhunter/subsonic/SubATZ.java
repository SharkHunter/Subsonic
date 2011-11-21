package com.sharkhunter.subsonic;

import java.io.InputStream;

import net.pms.dlna.virtual.VirtualFolder;

public class SubATZ extends VirtualFolder{
	private SubServer srv;
	private String id;
	
	public SubATZ(SubServer srv) {
		this(srv,null,null);
	}
	
	public SubATZ(SubServer srv,String name,String id) {
		super(name==null?"A-Z":name,null);
		this.srv=srv;
		this.id=id;
	}
	
	public void discoverChildren() {
		for(char i='A';i<='Z';i++) {
			SubFolder sf=new SubFolder(srv,String.valueOf(i),id,null);
			sf.setMatcher(SubUtil.artists());
			sf.setSecondMatcher(SubUtil.musicDirectory());
			sf.setMethod("getIndexes.view");
			sf.setSort(String.valueOf(i));
			sf.setParName("musicFolderId");
			addChild(sf);
		}
	}
	
	public InputStream getThumbnailInputStream() {
		return null;
	}
}
