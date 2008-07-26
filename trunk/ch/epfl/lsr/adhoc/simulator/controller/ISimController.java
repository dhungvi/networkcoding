/*
 * $Revision: 1.8 $
 * 
 * $Date: 2004/06/20 13:01:13 $
 * 
 * Author: Boris Danev and Aurelien Frossard
 *
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology
 * All Rights Reserved.
 */

package ch.epfl.lsr.adhoc.simulator.controller;

/**
 * Interface for the Controller of the simulation
 * 
 * @version $Revision: 1.8 $ $Date: 2004/06/20 13:01:13 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public interface ISimController {

    public static final String codeRevision =
        "$Revision: 1.8 $ $Date: 2004/06/20 13:01:13 $ Author: Boris Danev and Aurelien Frossard";

    /** 
     * Starts the controller thread 
     * @param nodeID contains the identificator of the node 
     */
    public void startup(int nodeID);

    /** Nicely terminates the controller */
    public void terminate();

    /** This method is used to notify the controller to update its state
     *  N.B! This method should be synchronized when implemented */
    public void update();
}
