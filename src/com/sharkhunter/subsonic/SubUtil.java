package com.sharkhunter.subsonic;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.regex.Pattern;

import net.pms.dlna.DLNAResource;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

public class SubUtil {
	
	public static final String defAgentString="Mozilla/5.0 (Windows; U; Windows NT 6.1; sv-SE; rv:1.9.2.3) Gecko/20100409 Firefox/3.6.3";
	
	public static boolean ignoreLine(String line) {
		if(StringUtils.isEmpty(line))
			return true;
		return (line.charAt(0)=='#');
	}
	
	public static String append(String res,String sep,String data) {
  	  	if(StringUtils.isEmpty(res))
  	  		return data;
  	  	if(StringUtils.isEmpty(data))
  	  		return res;
  	  	if(StringUtils.isEmpty(sep))
  	  		return res+data;
  	  	return res+sep+data;
	}
	
	public static InputStream postBinary(URLConnection connection,String query) {
		connection.setDoOutput(true);   
		connection.setDoInput(true);   
		connection.setUseCaches(false);   
		connection.setDefaultUseCaches(false);   
		//connection.setAllowUserInteraction(true);   

		connection.setRequestProperty ("Content-Type", "application/x-www-form-urlencoded");
		connection.setRequestProperty("User-Agent",defAgentString);
		connection.setRequestProperty("Content-Length", "" + query.length());  

		try {
			connection.setConnectTimeout(10000);

			connection.connect();
			// open up the output stream of the connection
			if(!StringUtils.isEmpty(query)) {
				DataOutputStream output = new DataOutputStream(connection.getOutputStream());
				output.writeBytes(query);   
				output.flush ();   
				output.close();
			}
			return connection.getInputStream();
		}
		catch (Exception e) {
			return null;
		}
	}
	
	public static String postPage(URLConnection connection,String query) { 
		connection.setDoOutput(true);   
		connection.setDoInput(true);   
		connection.setUseCaches(false);   
		connection.setDefaultUseCaches(false);   
		//connection.setAllowUserInteraction(true);   

		connection.setRequestProperty ("Content-Type", "application/x-www-form-urlencoded");
		connection.setRequestProperty("User-Agent",defAgentString);
		connection.setRequestProperty("Content-Length", "" + query.length());  

		try {
			connection.setConnectTimeout(10000);

			connection.connect();
			// open up the output stream of the connection
			if(!StringUtils.isEmpty(query)) {
				DataOutputStream output = new DataOutputStream(connection.getOutputStream());
				output.writeBytes(query);   
				output.flush ();   
				output.close();
			}
				

			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuilder page=new StringBuilder();
			String str;
			while ((str = in.readLine()) != null) {
				//	page.append("\n");
				page.append(str.trim());
				page.append("\n");
			}
			in.close();
			return page.toString();
		}
		catch (Exception e) {
			return "";
		}
	}
	
	public static String concatUrl(String a,String b) {
		if(StringUtils.isEmpty(b))
			return a;
		if(StringUtils.isEmpty(a))
			return b;
		if(a.charAt(a.length()-1)=='/')
			if(b.charAt(0)=='/') // remove this
				return a+b.substring(1);
			else
				return a+b;
		if(b.charAt(0)=='/')
			return a+b;
		return a+"/"+b;
	}
	
	public static SubMatcher musicFolders() {
		String re="musicFolder id=\\\"([^\\\"]+)\\\" name=\"([^\\\"]+)\\\"";
		return new SubMatcher(re,"id,name");
	}
	
	public static SubMatcher musicDirectory() {
		return musicDirectory("child");
	}
	public static SubMatcher musicDirectory(String str) {
		String re=str+" id=\\\"([^\\\"]+)\\\".*? title=\\\"([^\\\"]+)\\\""+
			   ".*? isDir=\\\"([^\\\"]+)([^>]+)";
		return new SubMatcher(re,"id,name,dir,rest");
	}
	
	public static SubMatcher albums() {
		return musicDirectory("album");
	}
	
	public static SubMatcher artists() {
		String re="artist name=\\\"([^\\\"]+)\\\" id=\\\"([^\\\"]+)\\\"";
		return new SubMatcher(re,"name,id");
	}
	
	public static SubMatcher playlists() {
		//<playlist id="1" name="Best of the 70s"/>
		String re="playlist id=\\\"([^\\\"]+)\\\" name=\\\"([^\\\"]+)\\\"";
		return new SubMatcher(re,"id,name");
	}
	
	public static SubMatcher playEntry() {
		return musicDirectory("entry");
	}
	
	public static String find(String rest,String field) {
		int pos=rest.indexOf(field);
		if(pos==-1)
			return null;
		pos+=field.length();
		int pos2=rest.indexOf("\"",pos+2);
		return rest.substring(pos+2,pos2);
	}	
}
