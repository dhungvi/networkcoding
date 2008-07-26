/*
 * $Revision: 1.4 $
 * 
 * $Date: 2004/06/05 17:14:19 $
 *
 * Author: Boris Danev and Aurelien Frossard
 *
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology
 * All Rights Reserved.
 */
package ch.epfl.lsr.adhoc.simulator.modules;

import java.util.Hashtable;

import ch.epfl.lsr.adhoc.simulator.config.AbstractConfig;

/**
 * This class represents the repository for the simulator.
 * It contains all application configuration files and serves
 * as a global storage for the simulator
 * 
 * @version $Revision: 1.4 $ $Date: 2004/06/05 17:14:19 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public class SimulatorContext {
    public static final String codeRevision =
        "$Revision: 1.4 $ $Date: 2004/06/05 17:14:19 $ Author: Boris Danev and Aurelien Frossard";

    /* Implementation details
     * --------------------------
     * This class is implemented as a singleton. Only one instance of this
     * class should exist in the simulator. 
     */

    /** Reference to the only instance of this class */
    private static final SimulatorContext INSTANCE = new SimulatorContext();

    /** Global storage repository for the context of the simulator */
    private Hashtable m_repository;

    /** Default private constructor */
    private SimulatorContext() {
        m_repository = new Hashtable();
    }

    /** Gets the only instance of this class */
    public static SimulatorContext getInstance() {
        return INSTANCE;
    }

    /** Puts an element in the repository */
    public void put(ContextKey p_key, AbstractConfig p_object) {
        m_repository.put(p_key, p_object);
    }

    /** 
     * Retrieves the element corresponding to a given key
     * @param p_key The key of the element to get from repository
     */
    public AbstractConfig get(ContextKey p_key) {
        return (AbstractConfig)m_repository.get(p_key);
    }

    /** Clears the Context */
    public void clear() {
        m_repository.clear();
    }
}
