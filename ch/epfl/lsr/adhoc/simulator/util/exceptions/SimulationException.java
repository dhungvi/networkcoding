/*
 * $Revision: 1.7 $
 * 
 * $Date: 2004/06/05 17:14:23 $
 * 
 * Author: Boris Danev and Aurelien Frossard
 *
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology
 * All Rights Reserved.
 */
package ch.epfl.lsr.adhoc.simulator.util.exceptions;

import java.io.PrintStream;
import java.io.PrintWriter;

import ch.epfl.lsr.adhoc.simulator.util.logging.SimLogger;

/**
 *  This is the common superclass for all exceptions of the simulator. This
 *  class and its subclasses support the chained exception facility that allows
 *  a root cause Throwable to be wrapped by this class or one of its
 *  descendants.
 * 
 * @version $Revision: 1.7 $ $Date: 2004/06/05 17:14:23 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public class SimulationException extends Exception {

    public static final String codeRevision =
        "$Revision: 1.7 $ $Date: 2004/06/05 17:14:23 $ Author: Boris Danev and Aurelien Frossard";

    /** Logger for the exceptions of the Simulation process */
    public static final SimLogger logger =
        new SimLogger(SimulationException.class);

    /**
     *  An optional nested exception used to provide the ability to encapsulate
     *  a lower-level exception to provide more detailed context information
     *  concerning the exact cause of the exception.
     */
    protected Throwable m_rootCause = null;

    /** 
     * Stores the messages in the exceptions. Each exceptions can take a key and an 
     * array of Objects. This is useful for debugging to give message keys
     * and values to these keys
     */
    private Object[] m_messageArgs = null;
    private String m_messageKey = null;

    /** Empty Constructor. */
    public SimulationException() {
        super();
    }

    /** A Constructor that takes a root cause throwable. */
    public SimulationException(Throwable p_rootCause) {
        m_rootCause = p_rootCause;
        logger.debug(null, p_rootCause);
    }

    /** A constructor that takes a message and the root cause */
    public SimulationException(String p_msgKey, Throwable p_rootCause) {
        m_rootCause = p_rootCause;
        m_messageKey = p_msgKey;
        logger.debug(p_msgKey, p_rootCause);
    }

    /** A constructor for a key and object values */
    public SimulationException(String p_msgKey, Object[] p_args) {
        m_messageKey = p_msgKey;
        m_messageArgs = p_args;
        logger.debug(p_msgKey);
    }

    /** Print both the normal and rootCause stack traces. */
    public void printStackTrace(PrintWriter p_writer) {
        super.printStackTrace(p_writer);
        if (getRootCause() != null) {
            getRootCause().printStackTrace(p_writer);
        }
        p_writer.flush();
    }

    /**  Print both the normal and rootCause stack traces. */
    public void printStackTrace(PrintStream outStream) {
        printStackTrace(new PrintWriter(outStream));
    }

    /** Print both the normal and rootCause stack traces. */
    public void printStackTrace() {
        printStackTrace(System.err);
    }

    /** 
     * Return the root cause exception, if one exists.
     * @return the root cause of the exception
     */
    public Throwable getRootCause() {
        return m_rootCause;
    }

    /** 
     * Retrieve the optional arguments. 
     * @return any messages stored in the exception
     */
    public Object[] getMessageArgs() {
        return m_messageArgs;
    }

    /** 
     * Set a nested, encapsulated exception to provide more low-level detailed
     * information to the client.
     */
    public void setRootCause(Throwable p_exception) {
        m_rootCause = p_exception;
        logger.debug(null, p_exception);
    }

    /** Set the key to the bundle. */
    public void setMessageKey(String p_key) {
        m_messageKey = p_key;
    }

    /**
     * Retrieve the message bundle key.
     * @return the key of the messages stored
     */
    public String getMessageKey() {
        return m_messageKey;
    }

    /** Set an object array that can be used for parametric replacement. */
    public void setMessageArgs(Object[] p_args) {
        m_messageArgs = p_args;
    }
}
