/*
 * $Revision: 1.4 $
 * 
 * $Date: 2004/06/05 17:14:23 $
 *
 * Author: Boris Danev and Aurelien Frossard
 *
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology
 * All Rights Reserved.
 */
package ch.epfl.lsr.adhoc.simulator.config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Deamons storage configuration module
 * 
 * @version $Revision: 1.4 $ $Date: 2004/06/05 17:14:23 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public class DaemonsConfig extends AbstractConfig {
	public static final String codeRevision =
		"$Revision: 1.4 $ $Date: 2004/06/05 17:14:23 $ Author: Boris Danev and Aurelien Frossard";
		
	/** Store the list of deamons' IP addresses in a list */
	private List m_list = new ArrayList();
	
	
	/** Adds an IP address in the list */
	public void addDaemon(String[] p_daemon){
		m_list.add(p_daemon);
	}
	
	/** @return a list of String[] array of all IP addresses and associated ports */
	public List getDeamonIPList(){
		return m_list;
	}
	
	/** Prints the content of this class */
	public String toString() {
		StringBuffer sb = new StringBuffer("DeamonsConfig[");
		for (Iterator i = m_list.iterator(); i.hasNext();){
			String[] s = (String[])i.next();
			sb.append(s[0]+ " " + s[1]+"\n");
		}
		sb.append("]");
		return (sb.toString());
	}
}
