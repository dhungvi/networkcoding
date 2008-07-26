/*
 * $Revision: 1.16 $
 * 
 * $Date: 2004/06/20 13:01:14 $
 *
 * Author: Boris Danev and Aurelien Frossard
 *
 * Copyright (C) 2003 EPFL - Swiss Federal Institute of Technology
 * All Rights Reserved.
 */
package ch.epfl.lsr.adhoc.simulator.util.xml;

/**
 * The class represents string constants for all XML tags
 * in the configuration file of the simulator
 * 
 * @version $Revision: 1.16 $ $Date: 2004/06/20 13:01:14 $
 * @author Author: Boris Danev and Aurelien Frossard
 */
public class SimXMLTags {
    public static final String CONFIGURATION_TAG = "Simulator";

    /** SimulationConfig tags */
    public static final String SIMULATION_CONFIG_TAG = "SimulationConfig";
    public static final String SERVER_TAG = "server";
    public static final String CLOCK_TAG = "clock";
    public static final String DURATION_TAG = "duration";
    public static final String IP_ATT = "ip";
    public static final String PORT_ATT = "port";
    public static final String QUEUE_ATT = "queue";
    public static final String TIMEOUT_ATT = "timeout";
    public static final String OUTPUT_DIR_TAG = "output-dir";

	/** DeamonsConfig tags */
	public static final String DEAMONS_CONFIG_TAG = "SimulationDaemons";
	public static final String DEAMONIP_TAG = "daemon";
	
    /** LayerConfig tags */
    public static final String LAYER_CONFIG_TAG = "SimulationLayer";
    public static final String NAME_TAG = "name";
    public static final String CLASS_TAG = "class";

    /** SimulationNetwork tags*/
    public static final String NETWORK_TAG = "SimulationNetwork";
    public static final String SIZE_ATT = "size";
    public static final String GLOBAL_CONFIG_TAG = "global-config";
    public static String CUSTOM_CONFIG_TAG = "custom-config";
    public static final String NODE_CONFIGIN_TAG = "node-configIn";
    public static final String NODE_CONFIGOUT_TAG = "node-configOut";
    public static final String NODE_ERRORLOG = "node-errorLog";
    public static final String NODE_SIMLOG = "node-simulationLog";
    public static final String NODE_SCHEDULE_TAG = "node-schedule";
	public static final String NODE_MOBILITYID_TAG = "node-mobilityID";
    public static final String NODE_TRANS_DEFAULTS_TAG =
        "node-transmissionDefaults";
    public static final String NODE_RANGE_ATT = "range";
    public static final String NODE_QUALITY_ATT = "quality";
    public static final String NODE_ON_TAG = "on";
    public static final String NODE_OFF_TAG = "off";
    public static final String NODE_ATTIME_ATT = "atTime";
    public static final String MOBILITYPATTERN_TAG = "mobility-pattern";
    public static final String MOBILITYPATTERN_TIME_ATT = "timeUnit";
    public static final String MODE_ATT = "mode";
    public static final String MODE_VALUE_ATT = "manual";
    public static final String TIME_UNIT_ATT = "timeUnit";
    public static final String ID_ATT = "id";
    public static final String SCALEBY_ATT = "scaleBy";
    public static final String AT_TIME_ATT = "atTime";

    /** Other XML tags */
    public static final String DATA_LINK_TAG = "DataLinkLayer";
    public static final String SERVER_PORT = "server_port";
	public static final String NODE_ID     = "nodeID";
	
    /** Prevents instantiation of this class */
    private SimXMLTags() {}
}