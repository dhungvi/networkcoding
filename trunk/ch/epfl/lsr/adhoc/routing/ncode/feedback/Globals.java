package ch.epfl.lsr.adhoc.routing.ncode.feedback;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.PropertyConfigurator;

import ch.epfl.lsr.adhoc.routing.ncode.ExtendedGaloisField;
import ch.epfl.lsr.adhoc.routing.ncode.GaloisException;
import ch.epfl.lsr.adhoc.routing.ncode.GaloisField;
import ch.epfl.lsr.adhoc.routing.ncode.Ncode;
import ch.epfl.lsr.adhoc.runtime.DefaultLoggerConfig;
import ch.epfl.lsr.adhoc.runtime.SimLogger;

public class Globals {
    public static int MaxBufLength= 1000;
    public static int pool=10; 
    public static GaloisField base;
    public static ExtendedGaloisField GF;
	public static InetAddress localAdd;
	public static final SimLogger logger = new SimLogger(Ncode.class);

	
    static {
		try {
			base = new GaloisField(2);
			GF = new ExtendedGaloisField(base,'a',8);
		} catch (GaloisException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try{
			localAdd=InetAddress.getLocalHost();
		} catch(UnknownHostException e) {
			System.err.println(e);
		}
		PropertyConfigurator.configure(
				DefaultLoggerConfig.getLoggerConfig("error.log", "simulation.log"));
	}
}


