/*
 * $Revision: 1.8 $ $Date: 2004/08/19 18:57:58 $ Author: Boris Danev and
 * Aurelien Frossard Copyright (C) 2003 EPFL - Swiss Federal Institute of
 * Technology All Rights Reserved.
 */

package ch.epfl.lsr.adhoc.simulator.events;

import ch.epfl.lsr.adhoc.mhm.Message;

/**
 * Abstract class that unifies the logging of incoming and outgoing messages. It
 * groups the common parameters of MessageSentEvent and MessageReceivedEvent.
 * 
 * @version $Revision: 1.8 $ $Date: 2004/08/19 18:57:58 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
abstract class AbstractMessageEvent extends AbstractLoggableEvent {
    public static final String codeRevision = "$Revision: 1.8 $ $Date: 2004/08/19 18:57:58 $ Author: Boris Danev and Aurelien Frossard";

    protected boolean          m_dropped;

    protected String           m_msgContent;
    protected String           m_msgHeader;
    protected double[]         m_sender;

    protected AbstractMessageEvent(long p_simTime, Message p_msg,
                                   double p_senderX, double p_senderY,
                                   boolean p_dropped, double p_senderRange) {

        super(p_simTime);
        m_msgHeader = p_msg.headerToString();
        m_msgContent = p_msg.toString();
        m_sender = new double[] { p_senderX, p_senderY, p_senderRange};
        m_dropped = p_dropped;
    }

    /** @see AbstractLoggableEvent#setAll(long) */
    protected void setAll(long p_simTime, Message p_msg,
                          double p_senderX, double p_senderY,
                          boolean p_dropped, double p_senderRange) {
        super.setAll(p_simTime);
        m_msgHeader = p_msg.headerToString();
        m_msgContent = p_msg.toString();
        m_sender = new double[] { p_senderX, p_senderY, p_senderRange};
        m_dropped = p_dropped;
    }

    /**
     * @see AbstractLoggableEvent#parameters()
     * @see AbstractLoggableEvent#toString()
     */
    protected String[][] parametersMsg(int p_moreParams) {
        String[][] params = new String[2][5 + p_moreParams];
        params[0][0] = "Message " + m_msgHeader;
        params[1][0] = m_msgContent;
        params[0][1] = "msgDropped";
        params[1][1] = "" + m_dropped;
        params[0][2] = "senderX";
        params[1][2] = "" + m_sender[0];
        params[0][3] = "senderY";
        params[1][3] = "" + m_sender[1];
        params[0][4] = "senderRange";
        params[1][4] = "" + m_sender[2];
        return params;
    }
}