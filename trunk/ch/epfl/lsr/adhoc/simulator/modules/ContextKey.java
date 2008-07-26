/*
 * $Revision: 1.7 $
 * 
 * $Date: 2004/06/05 17:14:18 $
 *
 * Author: Boris Danev and Aurelien Frossard
 *
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology
 * All Rights Reserved.
 */
package ch.epfl.lsr.adhoc.simulator.modules;

/**
 * Represents the keys for each application config object in the
 * context of the simulator.
 * @see ch.epfl.lsr.adhoc.simulator.modules.SimulatorContext
 * 
 * @version $Revision: 1.7 $ $Date: 2004/06/05 17:14:18 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public class ContextKey {
    public static final String codeRevision =
        "$Revision: 1.7 $ $Date: 2004/06/05 17:14:18 $ Author: Boris Danev and Aurelien Frossard";

    /** 
     * Key for the Simulation config file
     * @see ch.epfl.lsr.adhoc.simulator.config.SimulationConfig
     */
    public static final ContextKey SIMULATION_KEY =
        new ContextKey("ch.epfl.lsr.adhoc.simulator.SIMULATION");

	/** 
	 * Key for the SimulationDeamons config file
	 * @see ch.epfl.lsr.adhoc.simulator.config.DeamonsConfig
	 */
	public static final ContextKey DEAMONS_KEY =
		new ContextKey("ch.epfl.lsr.adhoc.simulator.DEAMONS");

    /** 
     * Key for the LAYER config file
     * @see ch.epfl.lsr.adhoc.simulator.config.LayerConfig
     */
    public static final ContextKey LAYER_KEY =
        new ContextKey("ch.epfl.lsr.adhoc.simulator.LAYER");

    /** 
     * Key for the NETWORK config file
     * @see ch.epfl.lsr.adhoc.simulator.config.NetworkConfig
     */
    public static final ContextKey NETWORK_KEY =
        new ContextKey("ch.epfl.lsr.adhoc.simulator.NETWORK");

    /** Private constructor for this enumeration class */
    private ContextKey(String p_key) {
        m_key = p_key;
    }

    /** Contains the key for each particulat object */
    private String m_key;
}
