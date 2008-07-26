/*
 * $Workfile$ $Revision: 1.7 $ $Date: 2004/08/19 18:57:57 $ $Archive$ Author:
 * Boris Danev and Aurelien Frossard Copyright (C) 2003 EPFL - Swiss Federal
 * Institute of Technology All Rights Reserved.
 */

package ch.epfl.lsr.adhoc.simulator.events;

import ch.epfl.lsr.adhoc.mhm.Message;

/**
 * PATTERN : singleton
 * @see ch.epfl.lsr.adhoc.simulator.events.AbstractMessageEvent
 * @version $Revision: 1.7 $ $Date: 2004/08/19 18:57:57 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public class MessageReceivedEvent extends AbstractMessageEvent {
    public static final String          codeRevision = "$Revision: 1.7 $ $Date: 2004/08/19 18:57:57 $ Author: Boris Danev and Aurelien Frossard";

    private double[]                    m_receiver;

    /** Singleton pattern : unique instance allowed */
    static private MessageReceivedEvent instance     = null;

    /** Singlton pattern : private constructor */
    private MessageReceivedEvent(long p_simTime, Message p_msg,
                                 double p_senderX, double p_senderY,
                                 double p_senderRange, double p_receiverX,
                                 double p_receiverY, double p_receiverRange,
                                 double p_receiverQuality, boolean p_dropped) {

        super(p_simTime, p_msg, p_senderX, p_senderY, p_dropped, p_senderRange);
        m_receiver = new double[] { p_receiverX, p_receiverY, p_receiverRange,
                p_receiverQuality};
    }

    /** Singleton pattern : returns the unique instance of this class and sets its content */
    static public MessageReceivedEvent getInstance(long p_simTime,
                                                   Message p_msg,
                                                   double p_senderX,
                                                   double p_senderY,
                                                   double p_senderRange,
                                                   double p_receiverX,
                                                   double p_receiverY,
                                                   double p_receiverRange,
                                                   double p_receiverQuality,
                                                   boolean p_dropped) {
        if (instance == null) {
            instance = new MessageReceivedEvent(p_simTime, p_msg, p_senderX,
                                                p_senderY, p_senderRange,
                                                p_receiverX, p_receiverY,
                                                p_receiverRange,
                                                p_receiverQuality, p_dropped);
        }
        else {
            instance.setAll(p_simTime, p_msg, p_senderX, p_senderY,
                            p_senderRange, p_receiverX, p_receiverY,
                            p_receiverRange, p_receiverQuality, p_dropped);
        }
        return instance;
    }

    /** @see AbstractLoggableEvent#setAll(long) */
    private void setAll(long p_simTime, Message p_msg, double p_senderX,
                        double p_senderY, double p_senderRange,
                        double p_receiverX, double p_receiverY,
                        double p_receiverRange, double p_receiverQuality,
                        boolean p_dropped) {
        super.setAll(p_simTime, p_msg, p_senderX, p_senderY, p_dropped, p_senderRange);
        m_receiver = new double[] { p_receiverX, p_receiverY, p_receiverRange,
                p_receiverQuality};
    }

    /**@see AbstractLoggableEvent#parameters()
     * @see AbstractLoggableEvent#toString()*/
    String[][] parameters() {
        String[][] params = parametersMsg(4);
        params[0][5] = "receiverX";
        params[1][5] = "" + m_receiver[0];
        params[0][6] = "receiverY";
        params[1][6] = "" + m_receiver[1];
        params[0][7] = "receiverRange";
        params[1][7] = "" + m_receiver[2];
        params[0][8] = "receiverQuality";
        params[1][8] = "" + m_receiver[3];
        return params;
    }
}