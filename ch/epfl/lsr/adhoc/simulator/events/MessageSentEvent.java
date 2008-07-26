/*
 * $Workfile$ $Revision: 1.6 $ $Date: 2004/08/19 18:57:58 $ $Archive$ Author:
 * Boris Danev and Aurelien Frossard Copyright (C) 2003 EPFL - Swiss Federal
 * Institute of Technology All Rights Reserved.
 */

package ch.epfl.lsr.adhoc.simulator.events;

import ch.epfl.lsr.adhoc.mhm.Message;

/**
 * PATTERN : Singleton
 * 
 * @version $Revision: 1.6 $ $Date: 2004/08/19 18:57:58 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public class MessageSentEvent extends AbstractMessageEvent {
    public static final String      codeRevision = "$Revision: 1.6 $ $Date: 2004/08/19 18:57:58 $ Author: Boris Danev and Aurelien Frossard";

    /** Singleton pattern : unique instance allowed */
    static private MessageSentEvent instance     = null;

    /** Singlton pattern : private constructor */
    private MessageSentEvent(long p_simTime, Message p_msg, double p_senderX,
                             double p_senderY, boolean p_dropped,
                             double p_senderRange) {

        super(p_simTime, p_msg, p_senderX, p_senderY, p_dropped, p_senderRange);
    }

    /**
     * Singleton pattern : returns the unique instance of this class and sets
     * its content
     */
    static public MessageSentEvent getInstance(long p_simTime, Message p_msg,
                                               double p_senderX,
                                               double p_senderY,
                                               boolean p_dropped,
                                               double p_senderRange) {
        if (instance == null) {
            instance = new MessageSentEvent(p_simTime, p_msg, p_senderX,
                                            p_senderY, p_dropped, p_senderRange);
        }
        else {
            instance.setAll(p_simTime, p_msg, p_senderX, p_senderY, p_dropped,
                            p_senderRange);
        }
        return instance;
    }

    /**
     * @see AbstractLoggableEvent#parameters()
     * @see AbstractLoggableEvent#toString()
     */
    String[][] parameters() {
        String[][] params = parametersMsg(0);
        return params;
    }
}