package com.sharkhunter.subsonic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;

import org.apache.commons.lang.StringUtils;

import net.pms.PMS;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAResource;
import net.pms.encoders.Player;
import net.pms.external.AdditionalFolderAtRoot;
import net.pms.external.AdditionalFoldersAtRoot;
import net.pms.external.FinalizeTranscoderArgsListener;
import net.pms.external.StartStopListener;
import net.pms.io.OutputParams;

public class SubPlug implements AdditionalFoldersAtRoot, FinalizeTranscoderArgsListener {

	private ArrayList<DLNAResource> srvs;
	
	public static final String version="0.12";
	
	public SubPlug() {
		String srvStr=(String)PMS.getConfiguration().getCustomProperty("subsonic.servers");
		srvs=new ArrayList<DLNAResource>();
		if(StringUtils.isEmpty(srvStr)) {
			return;
		}
		String[] srv=srvStr.split(" ");
		for(int i=0;i<srv.length;i++) {
			String[] srvData=srv[i].split(",");
			SubServer s=new SubServer(srvData[0],srvData[1]);
			srvs.add(s);
		}
		addCreds(srvs);	
		PMS.minimal("Starting Subsonic "+version);
	}
	
	private void addCreds(ArrayList<DLNAResource> srvs) {
		BufferedReader in;
		try {
			File f=new File((String) PMS.getConfiguration().getCustomProperty("cred.path"));
			in = new BufferedReader(new FileReader(f));
			String str;
			while ((str = in.readLine()) != null) {
				str=str.trim();
				if(SubUtil.ignoreLine(str))
					continue;
				String[] s=str.split("\\s*=\\s*",2);
				if(s.length<2)
					continue;
				String[] s1=s[0].split("\\.");
				if(s1.length<2)
					continue;
				if(!s1[0].equalsIgnoreCase("subsonic"))
					continue;
				String[] s2=s[1].split(",",2);
				if(s2.length<2)
					continue;
				String name=s1[1];
				for(int i=0;i<srvs.size();i++) {
					SubServer srv=(SubServer)srvs.get(i);
					if(name.equals(srv.name())) {
						srv.setCred(s2[0],s2[1]);
						break;
					}
				}
			}
		}
    	catch (Exception e) {
    		PMS.debug("subsonic err "+e);
    	} 
    }
	
	public Iterator<DLNAResource> getChildren() {
		if(srvs==null)
			return null;
		return (Iterator<DLNAResource>)srvs.iterator();
	}

	@Override
	public JComponent config() {
		return null;
	}

	@Override
	public String name() {
		return null;
	}

	@Override
	public void shutdown() {		
	}

	@Override
	public List<String> finalizeTranscoderArgs(Player player, String name,
			DLNAResource res, DLNAMediaInfo media, OutputParams params,
			List<String> cmdList) {
		if(!(res instanceof SubMedia))
			return cmdList;
		SubMedia sm=(SubMedia)res;
		for(int i=0;i<cmdList.size();i++) {
			String arg=cmdList.get(i);
			if(arg.equals(sm.getSystemName())) {
				cmdList.set(i, sm.getRealUrl());
				break;
			}
		}
		return cmdList;
	}
	

}
