/*
 * $Revision: 1.4 $
 * 
 * $Date: 2004/06/05 17:14:17 $
 *
 * Author: Boris Danev and Aurelien Frossard
 *
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology
 * All Rights Reserved.
 */
package ch.epfl.lsr.adhoc.simulator.events;

/**
 * This event is sent by the server to check node's connection
 * 
 * @version $Revision: 1.4 $ $Date: 2004/06/05 17:14:17 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public class CheckConnectionEvent implements java.io.Serializable {
    public static final String codeRevision =
        "$Revision: 1.4 $ $Date: 2004/06/05 17:14:17 $ Author: Boris Danev and Aurelien Frossard";

    /** 
     * Default constructor
     */
    public CheckConnectionEvent() {}
}
