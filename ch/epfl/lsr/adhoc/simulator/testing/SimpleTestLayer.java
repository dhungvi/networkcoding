/*
 * $Workfile$
 *
 * $Revision: 1.13 $
 * 
 * $Date: 2005/09/21 22:10:49 $
 *
 * $Archive$
 *
 * Author: Boris Danev and Aurelien Frossard
 *
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology
 * All Rights Reserved.
 */

package ch.epfl.lsr.adhoc.simulator.testing;

import java.text.DateFormat;
import java.util.Date;

import ch.epfl.lsr.adhoc.communicationslayer.AsynchronousLayer;
import ch.epfl.lsr.adhoc.runtime.FrancRuntime;
import ch.epfl.lsr.adhoc.communicationslayer.SendMessageFailedException;
import ch.epfl.lsr.adhoc.mhm.Message;
import ch.epfl.lsr.adhoc.mhm.MessagePool;
import ch.epfl.lsr.adhoc.simulator.util.logging.SimLogger;

/**
 * This layer simply sends messages regularly and warns each time it receives
 * or sends a message. The warning process is done via logging and not via
 * System.out.println
 */
public class SimpleTestLayer extends AsynchronousLayer {

    private final SimLogger m_logger = new SimLogger(this.getClass());

    private static final DateFormat m_timeFormater =
        DateFormat.getTimeInstance(DateFormat.LONG);

    /** 
     * Time interval, in seconds, between two successive send operations.
     * Read from XML config file */
    private int m_frequency;
    /** Time To Live (in hops), from config file */
    private int m_TTL;
    /** Destination Node, from config file */
    private long m_destination;
    /** A reference to the message pool of this node*/
    private MessagePool messagePool;
    /** A reference to the CommunicationLayer of this node*/
    private FrancRuntime runtime;
    /** The ID of this node */
    private long nodeID;
    /** A reference to the thread that sends messages */
    private Thread m_sendingThread;
    /** true if the layer sends messages, from the config file */
    private boolean m_sendingThreadActive;

    public SimpleTestLayer() {
        super();
    }

    /* (non-Javadoc)
     * @see ch.epfl.lsr.adhoc.communicationslayer.AsynchronousLayer#initialize(ch.epfl.lsr.adhoc.communicationslayer.CommunicationLayer)
     */
    public void initialize(FrancRuntime runtime) {
	this.runtime = runtime;
	this.messagePool = runtime.getMessagePool();

        try {
            m_sendingThreadActive =
                Boolean
                    .valueOf(runtime.returnParamValue("sendingThreadActive"))
                    .booleanValue();
            m_frequency =
                Integer.parseInt(runtime.returnParamValue("frequency"));
            m_TTL = Integer.parseInt(runtime.returnParamValue("TTL"));
            m_destination =
                Integer.parseInt(runtime.returnParamValue("destination"));
        }
        catch (Exception ex) {
            RuntimeException e = new RuntimeException(ex.getMessage());
            m_logger.fatal("Error reading parameteres from xml file", e);
            throw e;
        }
        m_sendingThread = new SendingThread();
    }

    /* (non-Javadoc)
     * @see ch.epfl.lsr.adhoc.communicationslayer.AsynchronousLayer#startup()
     */
    public void startup() {
        nodeID = runtime.getNodeID();
        start();
        if (m_sendingThreadActive)
            m_sendingThread.start();
    }

    /**  
     * Logs the incoming messages with the time of arrival and the ID of the sender
     * */
    protected void handleMessage(Message msg) {
        System.out.println(
            getStamp() + "RECEIVED msg FROM node " + msg.getSrcNode());
        messagePool.freeMessage(msg);
    }

    /** Header for every log message*/
    private String getStamp() {
        return "Node "
            + nodeID
            + " @"
            + m_timeFormater.format(new Date())
            + " : ";
    }

    void sendAndSleep() {
        Message msg = messagePool.getMessage((char)32);
        msg.setTTL(m_TTL);
        msg.setDstNode(m_destination);
        try {
            sendMessage(msg);
            System.out.println(
                getStamp() + "SENT msg TO node " + m_destination);
            msg = null;
        }
        catch (SendMessageFailedException e) {
            m_logger.warn(getStamp() + "could not send message", e);
        }
    }

    /**
     * Sends messages every xxx seconds.
     * The time interval is defined by m_frequency
     * in the containing class SimpleTestLayer
     */
    private class SendingThread extends Thread {

        /* (non-Javadoc)
        * @see java.lang.Runnable#run()
        */
        public void run() {
            while (true) {
                sendAndSleep();
                try {
                    sleep(m_frequency * 1000);
                }
                catch (InterruptedException e) {
                    m_logger.debug(getStamp() + "Could not perform sleep", e);
                }
            }
        }
    }
}
