/*
 * $Revision: 1.5 $
 * 
 * $Date: 2004/06/05 17:14:27 $
 * 
 * Author: Boris Danev, Aurelien Frossard 
 *
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology
 * All Rights Reserved.
 */
package ch.epfl.lsr.adhoc.runtime;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;

/**
 * This class represents the logger used through the simulator.
 *  
 * @version $Revision: 1.5 $ $Date: 2004/06/05 17:14:27 $
 * @author Boris Danev, Aurelien Frossard
 */
public class SimLogger {
    public static final String codeRevision =
        "$Revision: 1.5 $ $Date: 2004/06/05 17:14:27 $ Author: Boris Danev, Aurelien Frossard ";

    static String FQCN = SimLogger.class.getName();

    private Logger m_logger;

    public SimLogger(String p_name) {
        m_logger = Logger.getLogger(p_name);
    }

    public SimLogger(Class p_class) {
        this(p_class.getName());
    }

    public void debug(Object msg) {
        m_logger.log(FQCN, Level.DEBUG, msg, null);
    }
    public void debug(Object msg, Throwable t) {
        m_logger.log(FQCN, Level.DEBUG, msg, t);
    }
    public boolean isDebugEnabled() {
        return m_logger.isDebugEnabled();
    }

    public void info(Object msg) {
        m_logger.log(FQCN, Level.INFO, msg, null);
    }

    public void info(Object msg, Throwable t) {
        m_logger.log(FQCN, Level.INFO, msg, t);
    }

    public boolean isInfoEnabled() {
        return m_logger.isInfoEnabled();
    }

    public void warn(Object msg) {
        m_logger.log(FQCN, Level.WARN, msg, null);
    }
    public void warn(Object msg, Throwable t) {
        m_logger.log(FQCN, Level.WARN, msg, t);
    }

    public void error(Object msg) {
        m_logger.log(FQCN, Level.ERROR, msg, null);
    }
    public void error(Object msg, Throwable t) {
        m_logger.log(FQCN, Level.ERROR, msg, t);
    }

    public void fatal(Object msg) {
        m_logger.log(FQCN, Level.FATAL, msg, null);
    }
    public void fatal(Object msg, Throwable t) {
        m_logger.log(FQCN, Level.FATAL, msg, t);
    }
}
