package com.sharkhunter.subsonic;

import java.io.InputStream;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import net.pms.PMS;
import net.pms.dlna.DLNAMediaAudio;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAResource;

public class SubMedia extends DLNAResource {
	
	private SubServer srv;
	private String id;
	private String thumb;
	private String name;
	
	private String form;
	
	public SubMedia(SubServer srv,String name,String id,String thumb) {
		this.name=name;
		this.id=id;
		this.thumb=thumb;
		this.srv=srv;
		form=".mp3";
	}
	
	public void setFormat(String f) {
		form=f;
	}
	
	public void extractInfo(String blob) {
		if(media==null) {
			media=new DLNAMediaInfo();
			DLNAMediaAudio audio=new DLNAMediaAudio();
			audio.album=StringEscapeUtils.unescapeHtml(SubUtil.find(blob, "album"));
			audio.artist=StringEscapeUtils.unescapeHtml(SubUtil.find(blob, "artist"));
			audio.songname=getName();
			media.audioCodes.add(audio);
		}
	}
	
	public InputStream getInputStream() {
		try {
			return srv.binary(srv.getStreamMethod(), "id="+id);
		} catch (Exception e) {
			return null;
		}
	}
	
	public String getRealUrl() {
		try {
			return srv.getUrl(srv.getStreamMethod(), "id="+id);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getSystemName() {
		return getName()+form;
	}

	@Override
	public boolean isFolder() {
		return false;
	}

	@Override
	public boolean isValid() {
		checktype();
		return true;
	}

	@Override
	public long length() {
		return DLNAMediaInfo.TRANS_SIZE;
	}
	
	public InputStream getThumbnailInputStream() {
		try {
			if(!StringUtils.isEmpty(thumb)) {
				return srv.getThumb(thumb);
			}
		}
		catch (Exception e) {
			PMS.debug("blupp "+e);
		}
		return null;
	}
}

