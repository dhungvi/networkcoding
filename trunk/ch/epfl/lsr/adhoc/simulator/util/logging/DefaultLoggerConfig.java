/*
 * $Revision: 1.11 $
 * 
 * $Date: 2004/08/17 11:33:31 $
 *
 * Author: Boris Danev and Aurelien Frossard
 *
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology
 * All Rights Reserved.
 */
package ch.epfl.lsr.adhoc.simulator.util.logging;

import java.util.Properties;

/**
 * Default configuration for the logger
 * 
 * @version $Revision: 1.11 $ $Date: 2004/08/17 11:33:31 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public class DefaultLoggerConfig {
    public static final String codeRevision =
        "$Revision: 1.11 $ $Date: 2004/08/17 11:33:31 $ Author: Boris Danev and Aurelien Frossard";

    /**
     * Getter for a customized logger properties configuration file 
     * @param p_debugFile : file name for logging debugging information
     * @param p_logFile : file name for storing simulation information
     */
    public static Properties getLoggerConfig(
        String p_debugFile,
        String p_logFile) {
        config.properties.setProperty(DEBUG_FILE_KEY, p_debugFile);
        config.properties.setProperty(LOG_FILE_KEY, p_logFile);
        return config.properties;
    }

    /** Stores the default properties file */
    private Properties properties;
    private static DefaultLoggerConfig config = new DefaultLoggerConfig();
    private DefaultLoggerConfig() {
        init();
    }
    /** Initialize the default properties */
    private void init() {
        properties = new Properties();
        properties.setProperty("log4j.rootLogger", "DEBUG, FILEDEBUG, FILELOG");
        properties.setProperty(
            "log4j.appender.FILEDEBUG",
            "org.apache.log4j.FileAppender");
        properties.setProperty("log4j.appender.FILEDEBUG.File", "debug.log");
        properties.setProperty("log4j.appender.FILEDEBUG.Threshold", "DEBUG");
        properties.setProperty(
            "log4j.appender.FILEDEBUG.layout",
            "org.apache.log4j.PatternLayout");
        properties.setProperty(
            "log4j.appender.FILEDEBUG.layout.ConversionPattern",
            "%d %-5p %c - %m%n");

        properties.setProperty(
            "log4j.appender.FILELOG",
            "org.apache.log4j.FileAppender");
        properties.setProperty("log4j.appender.FILELOG.File", "simulation.log");
        properties.setProperty("log4j.appender.FILELOG.Threshold", "INFO");
        properties.setProperty(
            "log4j.appender.FILELOG.layout",
            "org.apache.log4j.PatternLayout");
        properties.setProperty(
            "log4j.appender.FILELOG.layout.ConversionPattern",
            "%m%n");
    }

    /** Properties which a user can modify */
    private static final String DEBUG_FILE_KEY =
        "log4j.appender.FILEDEBUG.File";
    private static final String LOG_FILE_KEY = "log4j.appender.FILELOG.File";
	private static final String DEBUG_MAXSIZE_KEY =
			"log4j.appender.FILEDEBUG.MaxFileSize";
	private static final String LOG_MAXSIZE_KEY = "log4j.appender.FILELOG.MaxFileSize";
}