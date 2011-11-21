package com.sharkhunter.subsonic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import net.pms.PMS;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.virtual.VirtualFolder;

public class SubServer extends VirtualFolder implements SearchObj {
	
	private String name;
	private String url;
	
	private String usr;
	private String pwd;
	
	private String streamMethod;

	public SubServer(String name,String url) {
		super(name+" Subsonic",null);
		this.name=name;
		this.url=SubUtil.concatUrl(url,"rest");
		usr=null;
		pwd=null;
		streamMethod="stream.view";
//		streamMethod=null;
	}
	
	public void setCred(String usr,String pwd) {
		this.usr=usr;
		this.pwd=pwd;
		getStreamMethod();
	}
	
	public String getStreamMethod() {
		if(streamMethod!=null)
			return streamMethod;
		try {
			String page=apiMethod("getUser.view","username="+usr);
			String download=SubUtil.find(page, "downloadRole");
			if(StringUtils.isEmpty(download)||download.equals("false")) {
				streamMethod="stream.view";
			}
			else {
				streamMethod="download.view";
			}
		} catch (Exception e) {
			streamMethod="stream.view";
		}
		PMS.debug("streammethod "+streamMethod);
		return streamMethod;
	}
	
	public String name() {
		return name;
	}
	
	public boolean stream() {
		if(streamMethod==null)
			return true;
		return streamMethod.startsWith("stream");
	}

	
	public String apiMethod(String method,String data) throws Exception {
		String query="c=shark&v=1.6.0&u="+usr+"&p="+pwd;
		if(!StringUtils.isEmpty(data))
			query=query+"&"+data;
		URL u=new URL(url+"/"+method);
		return SubUtil.postPage(u.openConnection(), query);			
	}
	
	public InputStream binary(String method,String data) throws Exception {
		String query="c=shark&v=1.6.0&u="+usr+"&p="+pwd;
		if(!StringUtils.isEmpty(data))
			query=query+"&"+data;
		URL u=new URL(url+"/"+method);
		return SubUtil.postBinary(u.openConnection(), query);
	}
	
	public String getUrl(String method,String data) throws Exception {
		String query="c=shark&v=1.6.0&u="+usr+"&p="+pwd;
		if(!StringUtils.isEmpty(data))
			query=query+"&"+data;
		return url+"/"+method+"?"+query;
	}
	
	public InputStream getThumb(String id) throws Exception {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		InputStream in=binary("getCoverArt.view", "id="+id);
		byte buf[] = new byte[4096];
		int n = -1;
		while ((n = in.read(buf)) > -1) {
			bytes.write(buf, 0, n);
		}
		in.close();
		return new ByteArrayInputStream(bytes.toByteArray());
	}
	
	public void discoverChildren() {
		try {
			SubFolder sf=new SubFolder(this,"Folders",null,null);
			sf.setMethod("getMusicFolders.view");
			sf.setMatcher(SubUtil.musicFolders());
			sf.setATZ(true);
			addChild(sf);			
			addChild(new SubATZ(this));
			addChild(new SubPlaylist(this));
			addChild(new SearchFolder("Search",this));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void search(String searchString, DLNAResource searcher) {
		try {
			String page=apiMethod("search2.view","query="+searchString);
			match(SubUtil.artists(),page,searcher);
			match(SubUtil.albums(),page,searcher);
			match(SubUtil.musicDirectory(),page,searcher);
		} catch (Exception e) {
		}
	}
	
	private void match(SubMatcher m,String page,DLNAResource res) {
		m.startMatch(page);
		while(m.match()) {
			String name=StringEscapeUtils.unescapeHtml(m.getMatch("name",false));
			String id=m.getMatch("id",true);
			String dir=m.getMatch("dir",false);
			String rest=m.getMatch("rest",false);
			String thumb=SubUtil.find(rest, "coverArt");
			if(StringUtils.isEmpty(dir)||dir.equals("true")) {
				SubFolder sf=new SubFolder(this,name,id,thumb);
				sf.setMethod("getMusicDirectory.view");
				sf.setMatcher(SubUtil.musicDirectory());
				res.addChild(sf);
			}
			else { // no dir
				if(StringUtils.isEmpty(thumb))
						thumb=thumbnailIcon;
				String suffix=null;
				suffix=SubUtil.find(rest, "transcodedSuffix");
				if(StringUtils.isEmpty(suffix))
					suffix=SubUtil.find(rest, "suffix");
				SubMedia sm=new SubMedia(this,name,id,thumb);
				if(!StringUtils.isEmpty(suffix))
					sm.setFormat("."+suffix);
				sm.extractInfo(rest);
				res.addChild(sm);
			}
		}
	}
}
