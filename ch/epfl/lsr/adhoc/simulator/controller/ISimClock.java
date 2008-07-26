/*
 * $Revision: 1.6 $
 * 
 * $Date: 2004/06/05 17:14:21 $
 * 
 * Author: Boris Danev and Aurelien Frossard
 *
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology
 * All Rights Reserved.
 */
package ch.epfl.lsr.adhoc.simulator.controller;

/**
 * Interface  for the clock of the simulation
 * 
 * @version $Revision: 1.6 $ $Date: 2004/06/05 17:14:21 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public interface ISimClock extends Runnable {

    public static final String codeRevision =
        "$Revision: 1.6 $ $Date: 2004/06/05 17:14:21 $ Author: Boris Danev and Aurelien Frossard";

    /** Starts the clock. */
    public void startup();

    /** Terminates the clock definitely */
    public void terminate();

    /** Sets the interval in milliseconds between two ticks of the clock */
    public void setSpeed(long p_tickInterval);
}
