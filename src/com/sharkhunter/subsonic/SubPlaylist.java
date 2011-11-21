package com.sharkhunter.subsonic;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import net.pms.dlna.virtual.VirtualFolder;

public class SubPlaylist extends VirtualFolder {
	
	private SubServer srv;
	private SubMatcher match;
	
	public SubPlaylist(SubServer srv) {
		super("Playlists",null);
		this.srv=srv;
		match=SubUtil.playlists();
	}
	
	public void discoverChildren() {
		try {
			String page=srv.apiMethod("getPlaylists.view", null);
			match.startMatch(page);
			while(match.match()) {
				String name=StringEscapeUtils.unescapeHtml(match.getMatch("name",false));
				String id=match.getMatch("id",true);
				SubFolder sf=new SubFolder(srv,name,id,null);
				sf.setMethod("getPlaylist.view");
				sf.setMatcher(SubUtil.playEntry());
				addChild(sf);
			}
		} catch (Exception e) {
		}
		
	}

}
