/*
 * $Revision: 1.6 $
 * 
 * $Date: 2004/08/17 11:33:31 $
 *
 * Author: Boris Danev and Aurelien Frossard
 *
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology
 * All Rights Reserved.
 */
package ch.epfl.lsr.adhoc.simulator.daemon;

import java.util.Properties;

/**
 * Default configuration for the logging of the daemon
 * 
 * @version $Revision: 1.6 $ $Date: 2004/08/17 11:33:31 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public class DaemonLoggerConfig {
	public static final String codeRevision =
		"$Revision: 1.6 $ $Date: 2004/08/17 11:33:31 $ Author: Boris Danev and Aurelien Frossard";

	/**
	 * Getter for a customized logger properties configuration file 
	 */
	public static Properties getLoggerConfig(){
		return config.properties;
	}

	/** Stores the default properties file */
	private Properties properties;
	private static DaemonLoggerConfig config = new DaemonLoggerConfig();
	private DaemonLoggerConfig() {
		init();
	}
	/** Initialize the default properties */
	private void init() {
		properties = new Properties();
		properties.setProperty("log4j.rootLogger", "DEBUG, FILELOG");
		properties.setProperty(
			"log4j.appender.FILELOG",
			"org.apache.log4j.RollingFileAppender");
		properties.setProperty("log4j.appender.FILELOG.File", "daemon.log");
		properties.setProperty("log4j.appender.FILELOG.MaxFileSize", "1024KB");
		properties.setProperty("log4j.appender.FILELOG.Threshold", "DEBUG");
		properties.setProperty(
			"log4j.appender.FILELOG.layout",
			"org.apache.log4j.PatternLayout");
		properties.setProperty(
			"log4j.appender.FILELOG.layout.ConversionPattern",
			"%d %-5p %c - %m%n");
	}
}
