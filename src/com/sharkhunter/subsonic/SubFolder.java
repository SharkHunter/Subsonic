package com.sharkhunter.subsonic;

import java.io.InputStream;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import net.pms.PMS;
import net.pms.dlna.virtual.VirtualFolder;

public class SubFolder extends VirtualFolder {
	
	private SubServer srv;
	private String id;
	private String method;
	
	private SubMatcher matcher;
	private SubMatcher matcher2;
	
	private String sort;
	private String parName;
	
	private boolean atz;
	
	public SubFolder(SubServer srv,String name,String id,String thumb) {
		super(name,thumb);
		this.srv=srv;
		this.id=id;
		matcher=null;
		matcher2=null;
		method=null;
		sort="";
		atz=false;
		parName="id";
	}
	
	public void setMethod(String method) {
		this.method=method;
	}
	
	public void setMatcher(SubMatcher m) {
		matcher=m;
	}
	
	public void setSecondMatcher(SubMatcher m) {
		matcher2=m;
	}
	
	public void setSort(String b) {
		sort=b;
	}
	
	public void setATZ(boolean b) {
		atz=b;
	}
	
	public void setParName(String p) {
		parName=p;
	}
	
	public void discoverChildren() {
		try {
			String realId="";
			if(!StringUtils.isEmpty(id))
				realId=parName+"="+id;
			String page=srv.apiMethod(method, realId);
			match(this.matcher,page);
			if(matcher2!=null)
				match(matcher2,page);
		} catch (Exception e) {
		}
	}
	
	private void match(SubMatcher m,String page) {
		m.startMatch(page);
		while(m.match()) {
			String name=StringEscapeUtils.unescapeHtml(m.getMatch("name",false));
			if(!StringUtils.isEmpty(name)&&
					!StringUtils.isEmpty(sort)&&
					!name.startsWith(sort))
				continue;
			String id=m.getMatch("id",true);
			String dir=m.getMatch("dir",false);
			if(atz) {
				addChild(new SubATZ(srv,name,id));
				continue;
			}
			String rest=m.getMatch("rest",false);
			String thumb=SubUtil.find(rest, "coverArt");
			if(StringUtils.isEmpty(dir)||dir.equals("true")) {
				SubFolder sf=new SubFolder(srv,name,id,thumb);
				sf.setMethod("getMusicDirectory.view");
				sf.setMatcher(SubUtil.musicDirectory());
				addChild(sf);
			}
			else { // no dir
				if(StringUtils.isEmpty(thumb))
						thumb=thumbnailIcon;
				String suffix=null;
				if(srv.stream())
					suffix=SubUtil.find(rest, "transcodedSuffix");
				if(StringUtils.isEmpty(suffix))
					suffix=SubUtil.find(rest, "suffix");
				SubMedia sm=new SubMedia(srv,name,id,thumb);
				if(!StringUtils.isEmpty(suffix))
					sm.setFormat("."+suffix);
				sm.extractInfo(rest);
				addChild(sm);
			}
		}
	}
	
	public InputStream getThumbnailInputStream() {
			try {
				if(!StringUtils.isEmpty(thumbnailIcon)) {
					return srv.getThumb(thumbnailIcon);
				}
			}
			catch (Exception e) {
				PMS.debug("blupp "+e);
			}
			return super.getThumbnailInputStream();
		}
	}
