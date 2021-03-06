package com.sharkhunter.subsonic;

import java.io.*;
import net.pms.network.HTTPResource;
import net.pms.dlna.virtual.*;


public class SearchAction extends VirtualFolder {
	private Search sobj;
	private char ch;
	private String name;

	public SearchAction(Search sobj,char ch) {
		this(sobj,ch,String.valueOf(ch));
	}
	
	public SearchAction(Search sobj,char ch,String name) {
		super(name,"images/Play1Hot_120.jpg");
		this.sobj=sobj;
		this.ch=ch;
		this.name=name;
	}
	
	public InputStream getThumbnailInputStream() {
        return getResourceInputStream("images/Play1Hot_120.jpg");
	}
	
	public void resolve() {
		discovered=false;  // we can't clear this enough
	}
	
	public void discoverChildren() {
		sobj.append(ch);
		discovered=false;
	}
	
	public void refreshChildren() {
		discovered=false;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isFolder() {
		return true;
	//	return false;
	}

	@Override
	public long length() {
		return -1; //DLNAMediaInfo.TRANS_SIZE;
	}

	public long lastModified() {
		return 0;
	}
	
	 public String getThumbnailContentType() {
         return HTTPResource.JPEG_TYPEMIME;
	 }


	@Override
	public boolean isValid() {
		return true;
	}
}
