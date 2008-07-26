package ch.epfl.lsr.adhoc.runtime;

/**
 * This is the main class of the MANET Framework.
 * 
 * This class is responsible for initializing and starting the framework. It
 * will read a configuration file and initialize the stack of network layers
 * as specified in this config file.
 * 
 * @author David Cavin
 * @version 1.0
 */

//import jist.runtime.*;
//import jist.swans.Constants;
import simjistwrapper.exceptions.*;
import simjistwrapper.utils.simstruct.SimConfig;
import ch.epfl.lsr.adhoc.real.FrancRealSystem;

import org.w3c.dom.*;
import java.io.*;


public final class Main
{

	private static Document configFile;

	/**
	 * There is (yet) no need to instantiate Main.
	 */
	private Main()
	{
	}

	/**
	 * This is the main application to start the framework.
	 * 
	 * An application using franc should not start the framework, but should
	 * rather be started by the framework. This is done for instance by
	 * implementing the application as an AsynchronousLayer. See the Chat
	 * application for an example.
	 * 
	 * Franc expects the following parameters:
	 * 
   * Real execution
   *  <franc config file>
   * Simulation mode 
   *  -sim <sim config file>
	 *
	 */

	public static void main(String[] args)
	{
		String file = null;
		if (args.length == 1) {
			System.out.println("FRANC is starting up");
      new FrancRealSystem();
      Config config = new Config(args[0]);
      config.loadConfig();
      RuntimeInterface runtime = config.createRuntime();
      runtime.initialize();
      runtime.startup();
    } else if(args.length == 2 && args[0].compareTo("-sim")==0) {
			System.out.println("FRANC is starting up in simulation mode");
			new FrancRealSystem();
			try {
        new SimConfig(args[1]);
      } catch(ParsingException e) {
        System.err.println("An error occured during the parsing step");
				e.printStackTrace();
      } catch(SemanticException e1) {
				System.err.println("An error occured, one of the parameter is not in the defined range");
        e1.printStackTrace();
      } catch(SimRuntimeException e2) {
        System.err.println("Some error occured after the models creation");
        e2.printStackTrace();
      }
		} else
			usage();
	}

	static void usage()
	{
		System.out.println("In a real execution :");
		System.out.println("usage: ch.epfl.lsr.adhoc.runtime.Main <config file>\n");
		System.out.println("In simulation mode :");
		System.out.println("usage: ch.epfl.lsr.adhoc.runtime.Main -sim <simulation config file>\n");
		System.exit(-1);
	}
}
