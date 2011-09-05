package ch.epfl.lsr.adhoc.routing.ncode.fast;

import ch.epfl.lsr.adhoc.runtime.DefaultLoggerConfig;
import ch.epfl.lsr.adhoc.runtime.SimLogger;
import org.apache.log4j.PropertyConfigurator;


import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.PropertyConfigurator;


public class NCGlobals {
    public static int MaxBufLength= 250;
    public static int pool=10; 
    public static GField GF;
    public static InetAddress localAdd;
	public static long nodeID_;
	public static final SimLogger logger = new SimLogger(NcodeFast.class);

	
    static {
		GF = new GField(8);
		try{
			localAdd=InetAddress.getLocalHost();
		} catch(UnknownHostException e) {
			System.err.println(e);
		}
		PropertyConfigurator.configure(
				DefaultLoggerConfig.getLoggerConfig("error.log", "simulation.log"));

	}
    
}