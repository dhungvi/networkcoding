/*
 * $Revision: 1.8 $
 * 
 * $Date: 2004/06/05 17:14:21 $
 *
 * Author: Boris Danev and Aurelien Frossard
 *
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology
 * All Rights Reserved.
 */
package ch.epfl.lsr.adhoc.simulator.controller;

import ch.epfl.lsr.adhoc.simulator.util.logging.SimLogger;

/**
 * This class represents the clock of the simulation
 * It provides a reliable tick for the simulation
 *   
 *  
 * @version $Revision: 1.8 $ $Date: 2004/06/05 17:14:21 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public class SimClock implements ISimClock {

    public static final String codeRevision =
        "$Revision: 1.8 $ $Date: 2004/06/05 17:14:21 $ Author: Boris Danev and Aurelien Frossard";

    /*
     * To ensure a hign level of accuracy, it gets the current time when it 
     * starts running, then issues a tick every 1000 
     * milliseconds that pass, even if the notification does not occur 
     * every 1000 milliseconds exactly. Thus the notification that a time 
     * period has passed won't be very accurate, but the notification that 
     * some number of time periods have passed will be--in other words, 
     * the clock is accurate over long stretches. Given the vissitudes of 
     * thread management, this is about the best that can be expected.
     */

    /** Private logger for the simulation clock */
    private static final SimLogger logger = new SimLogger(SimClock.class);

    /** 
     * Indicates the threshold for sleeping or not.
     * @see method run() 
     */
    private static int SLEEP_THRESHOLD = 5;

    /** Indicates the default speed of the clock in milliseconds. */
    private static int DEFAULT_SPEED = 1000;

    /** Stores reference to the controller of the simulation. */
    private ISimController m_controller;

    /** Clock speed. */
    private long m_tickInterval;

    /** Indicates if the clock is running or not. */
    private boolean m_running;

    /** The thread in which the clock is running */
    private Thread m_clockThread;

    /** 
     * Default constructor.
     * @param p_controller which controller to notify for a time change
     */
    public SimClock(ISimController p_controller) {
        m_controller = p_controller;
        m_running = false;
        m_tickInterval = DEFAULT_SPEED;
    }

    /** Starts the clock. */
    public void startup() {
        m_clockThread = new Thread(this);
        m_clockThread.start();
    }

    /** Stops definitely the clock */
    public synchronized void terminate() {
        m_running = false;
    }

    public synchronized void setSpeed(long p_tickInterval) {
        m_tickInterval = p_tickInterval;
    }

    /** Method implements the clock ticking */
    public void run() {
        long wakeUpTime = 0; // when to wake up next
        long sleepTime = 0; // how long to sleep

        if (!m_running) {
            m_running = true;
            wakeUpTime = System.currentTimeMillis();
            //Mesurements show that this is faster then: new Date().getTime();
            while (m_running) {
                /* sleep until duration has passed */
                wakeUpTime += m_tickInterval;
                sleepTime = wakeUpTime - System.currentTimeMillis();
                //Mesurements show that this is faster then: new Date().getTime();
                try {
                    if (sleepTime > SLEEP_THRESHOLD)
                        Thread.sleep(sleepTime);
                }
                catch (InterruptedException e) {
                    logger.debug(null, e);
                }
                /* Call the controller to update its state */
                m_controller.update();
            }
        }
    }

    /** Overrides the default toString method */
    public String toString() {
        return getClass().getName();
    }
}
