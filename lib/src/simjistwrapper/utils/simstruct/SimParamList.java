package simjistwrapper.utils.simstruct;

import java.util.ArrayList;
import jist.swans.Constants;
import jist.swans.misc.Util;

/**
 * Hold the constant information for the simulation.
 * <ul>
 * <li>Holds the tags that should be found into the XML file as
 * <code>public static final String</code> variables.</li>
 * <li>Holds the default values of the defined variables.Hence, serveral calls
 * are made to the <code>jist.swans.Constants</code> class.</li>
 * <li>Sorts the variables into <code>ArrayLists</code> depending on the type
 * the value is suppose to have</li>
 * <li>Hold the possible values of some variables if needed.</li>
 * </ul>
 * There are some conventions on the name of the variables.<br> If a variable
 * <code>public static final String plop</code> is now defined. Then, the
 * <code>ArrayLists</code> containing the possible values should be named
 * <code>plopRange</code>. The default value of the variabe <code>plop</code>
 * should be name <code>plopDefault</code>.<br> <br> If someone wants to
 * add a parameter here's a howto:<br>
 * <ol>
 * <li>WORK IN THIS FILE</li>
 * <ol>
 * <li>Modifiy the DTD of the file such that the file can be properly
 * parsed.</li>
 * <li>Add here the name of the parameter. The value of the parameter should
 * be exactly what is put in the XML file. The name of the variable should be
 * the same.</li>
 * <li>Add the parameter to the list to which it belongs depending on the
 * type of the parameter. (If the parameter has a double value, it should be
 * added to the doubleList).</li>
 * <li>If needed, create an ArrayList of name : name-of-the-parameterRange
 * containing all the possible values of the parameter. Or define another way to
 * describe a constraint ! :) And make sure the method
 * create-name-of-the-parameterRange is defined and called from the
 * createRange() method.</li>
 * <li>Define the default value of your new parameter in a variable called
 * name-of-the-parameterDefault</li>
 * </ol>
 * <li>WORK IN THE CHECKER</li> 
 * <ol>
 * <li>If a range for this variable is defined, the method check content should be modified such that the variable
 * name-of-the-parameterRange is accessed</li>
 * </ol>
 * <li>WORK IN THE NODE MODEL</li> 
 * <ol>
 * <li>The variable should be added with the proper format, or
 * String.</li>
 * <li>Set the variable its default value.</li>
 * <li>Add the corresponding line to assign the value if there's any in the XML file.</li>
 * </ol>
 * </ol>
 * 
 * @author Clergue Jeremie
 */
public class SimParamList
{
	/*
	 * FIRST PART : THE XML NODES
	 * 
	 * This first part is just a link between the internal programmation and the
	 * name of the XML node. The internal program should never call some XML
	 * node directly but should point to one of the variables defined here.
	 */
	/**
	 * Tag of the XML file
	 */
	public static final String NodeModels = "NodeModels";

	/**
	 * Tag of the XML file. This is an instance of a node model. For more
	 * details, see the DTD file
	 */
	public static final String NodeModel = "NodeModel";

	/**
	 * Tag of the XML file. Defines a structure dealing with the field layer.
	 */
	public static final String FieldLayer = "FieldLayer";
	
	public static final String CommonParameters = "CommonParameters";
	
	/**
	 * Section talking about mobility
	 * TODO redo the comment wrt the other ones.
	 */
	public static final String mobility = "mobility";

	/**
	 * Section talking about the position of the node. See DTD for more details.
	 */
	public static final String position = "Position";
	/**
	 * Tag of the XML file. Defines a structure dealing with the radio layer.
	 */
	public static final String RadioLayer = "RadioLayer";

	/**
	 * Tag of the XML file. Defines a structure dealing with the mac layer.
	 */
	public static final String MacLayer = "MacLayer";

	/**
	 * Tag of the XML file. Defines a structure dealing with the net layer.
	 */
	public static final String NetLayer = "NetLayer";

	/*
	 * SECOND PART : THE VARIABLES
	 * 
	 * This second part defines the name of the variable (same concept as above)
	 * and also defines the default value each one of the variable should have.
	 * It is the same concept, only this file can call some Jist constants
	 * (Constants.*).
	 * This section gives first the parameter and then its default value.
	 */
	
	/**
	 * Argument of the field layer creation representing the length of the field
	 * to be created. This value should always be a positive number.
	 * WARNING : IT'S AN ARGUMENT, NOT ANOTHER NODE !
	 */
	public static final String fieldlength = "fieldlength";

	public static final int fieldlengthDefault = 1000;

	/**
	 * Argument of the field layer creation representing the width of the field
	 * to be created. This value should always be a positive number.
	 * WARNING : IT'S AN ARGUMENT, NOT ANOTHER NODE !
	 */
	public static final String fieldwidth = "fieldwidth";

	public static final int fieldwidthDefault = 1000;

	/**
	 * Defines the spatial argument of the method
	 * <code>jist.swans.field.Field</code>. The acceptable values are the
	 * following:<br>
	 * <ul>
	 * <li>grid</li>
	 * <li>hiergrid</li>
	 * <li>linearlist</li>
	 * <li>spatialtransmitvisitor</li>
	 * <li>spatialvisitor</li>
	 * <li>tiledwraparound</li>
	 * </ul>
	 */
	public static final String spatial = "spatial";

	/**
	 * Default value of the spatial parameter.
	 */
	public static final String spatialDefault = "linearlist";

	/**
	 * Defines the fading argument of the method
	 * <code>jist.swans.field.Field</code>. The acceptable values are the
	 * following:<br>
	 * <ul>
	 * <li>node</li>
	 * <li>rayleigh</li>
	 * <li>rician</li>
	 * </ul>
	 */
	public static final String fading = "fading";

	/**
	 * Default value of the fading parameter.
	 */
	public static final String fadingDefault = "none";

	/**
	 * Defines the pathloss argument of the method
	 * <code>jist.swans.field.Field</code>. The acceptable values are the
	 * following: <br>
	 * <ul>
	 * <li>freespace</li>
	 * <li>tworay</li>
	 * </ul>
	 */
	public static final String pathloss = "pathloss";

	/**
	 * Default value of the path loss parameter.
	 */
	public static final String pathlossDefault = "freespace";

	/**
	 * Defines the propagation limit argument of the method
	 * <code>jist.swans.field.Field</code>.<br>
	 * TODO for the moment, all the values are accepted !
	 */
	public static final String propagationlimit = "propagationlimit";

	public static final double propagationlimitDefault = Constants.PROPAGATION_LIMIT_DEFAULT;

	/**
	 * Defines the random walk mobility model.
	 */
	public static final String mobilitymodel = "mobilitymodel";

	public static final String mobilitymodelDefault = "static";

	public static final String randomradius = "randomradius";

	public static final double randomradiusDefault = 60;

	public static final String fixedradius = "fixedradius";

	public static final double fixedradiusDefault = 60;

	public static final String pausetime = "pausetime";

	public static final double pausetimeDefault = 500 * Constants.MILLI_SECOND;

	public static final String minspeed = "minspeed";

	public static final int minspeedDefault = 0;

	public static final String maxspeed = "maxspeed";

	public static final int maxspeedDefault = 50;

	public static final String precision = "precision";

	public static final int precisionDefault = 100;

	public static final String positiontype = "positiontype";
	public static final String positiontypeDefault = "random";
	
	/**
	 * WATCH OUT ! NO DEFAULT VALUE HERE: 
	 * 
	 * if(positiontype == random)
	 * {
	 *    no need to define posx & posy
	 * } else if(positiontype == deterministic)
	 * {
	 *    that's the purpose of deterministic, the values should be set
	 *    or at least one (the other will be set randomly)
	 * }
	 * 
	 * Hence, in all cases, no need to give a default value
	 */
	public static final String posx = "posx";
	public static final String posy = "posy";
	
	/**
	 * no default value
	 */
	public static final String id = "id";
	
	
	/**
	 * Defines the radio model. Depending on the value of this parameter, different constructor 
	 * of the package <code>jist.swans.radio</code> are going to be used. <br>
	 * Hence, only the following values will be accepted: <br>
	 * <ul>
	 * <li>additive</li>
	 * <li>independant</li>
	 * </ul>
	 */
	public static final String radiomodel = "radiomodel";

	/**
	 * Default value of the radio model.
	 */
	public static final String radiomodelDefault = "additive";

	/**
	 * Defines the frequency argument for the method
	 * <code>public static RadioInfo.RadioInfoShared createShared</code>. <br>
	 * TODO for the moment, all the values are accepted !
	 */
	public static final String frequency = "frequency";

	/**
	 * Default value of the frequency parameter.
	 */
	public static final double frequencyDefault = Constants.FREQUENCY_DEFAULT;

	/**
	 * Defines the bandwidth argument for the method
	 * <code>public static RadioInfo.RadioInfoShared createShared</code>. <br>
	 * TODO for the moment, all the values are accepted !
	 */
	public static final String bandwidth = "bandwidth";

	/**
	 * Default value of the bandwith parameter.
	 */
	public static final int bandwidthDefault = Constants.BANDWIDTH_DEFAULT;

	/**
	 * Defines the transmit power argument for the method
	 * <code>public static RadioInfo.RadioInfoShared createShared</code>. <br>
	 * TODO for the moment, all the values are accepted !
	 */
	public static final String transmitpower = "transmitpower";

	/**
	 * Default value of the transmit power parameter.
	 */
	public static final double transmitpowerDefault = Constants.TRANSMIT_DEFAULT;

	/**
	 * Defines the gain argument for the method
	 * <code>public static RadioInfo.RadioInfoShared createShared</code>. <br>
	 * TODO for the moment, all the values are accepted !
	 */
	public static final String antennagain = "antennagain";

	/**
	 * Default value of the gain parameter.
	 */
	public static final double antennagainDefault = Constants.GAIN_DEFAULT;

	/**
	 * Defines the sensitivity argument for the method
	 * <code>public static RadioInfo.RadioInfoShared createShared</code>. <br>
	 * TODO for the moment, all the values are accepted !
	 */
	public static final String sensitivity_mW = "sensitivity_mW";

	/**
	 * Default value of the sensitivity parameter.
	 */
	public static final double sensitivity_mWDefault = Util
			.fromDB(Constants.SENSITIVITY_DEFAULT);

	/**
	 * Defines the threshold argument for the method
	 * <code>public static RadioInfo.RadioInfoShared createShared</code>. <br>
	 * TODO for the moment, all the values are accepted !
	 */
	public static final String threshold_mW = "threshold_mW";

	/**
	 * Default value of the threshold parameter.
	 */
	public static final double threshold_mWDefault = Util
			.fromDB(Constants.THRESHOLD_DEFAULT);

	/**
	 * Defines the field temperature argument for the method
	 * <code>public static RadioInfo.RadioInfoShared createShared</code>. <br>
	 * TODO for the moment, all the values are accepted !
	 */
	public static final String fieldtemperature_K = "fieldtemperature_K";

	/**
	 * Default value of the fiedl temperature parameter.
	 */
	public static final double fieldtemperature_KDefault = Constants.TEMPERATURE_DEFAULT;

	/**
	 * Defines the thermal factor argument for the method
	 * <code>public static RadioInfo.RadioInfoShared createShared</code>. <br>
	 * TODO for the moment, all the values are accepted !
	 */
	public static final String thermalfactor = "thermalfactor";

	/**
	 * Default value of the thermal factor parameter.
	 */
	public static final double thermalfactorDefault = Constants.TEMPERATURE_FACTOR_DEFAULT;

	/**
	 * Defines the ambient noise argument for the method
	 * <code>public static RadioInfo.RadioInfoShared createShared</code>. <br>
	 * TODO for the moment, all the values are accepted !
	 */
	public static final String ambientnoise_mW = "ambientnoise_mW";

	/**
	 * Default value of the ambient noise parameter.
	 */
	public static final double ambientnoise_mWDefault = Constants.AMBIENT_NOISE_DEFAULT;

	/**
	 * Defines the SNR threshold argument for the method
	 * <code>public static RadioInfo.RadioInfoShared createShared</code>.
	 * <br>
	 * TODO for the moment, all the values are accepted !
	 */
	public static final String SNRThreshold = "SNRThreshold";

	/**
	 * Default value of the SNR threshold parameter.
	 */
	public static final double SNRThresholdDefault = Constants.SNR_THRESHOLD_DEFAULT;

	/**
	 * Defines the mac model. Depending on the value of this parameter,
	 * different constructors of the package <code>jist.swans.mac</code> are
	 * going to be used. <br>
	 * Hence, only the following values will be accepted: <br>
	 * <ul>
	 * <li>dumb</li>
	 * <li>802_11</li>
	 * </ul>
	 */
	public static final String macmodel = "macmodel";

	/**
	 * Default value of the mac model.
	 */
	public static final String macmodelDefault = "802_11";

	/**
	 * This tag should no more appear in the XML file. The only supported
	 * protocol is the UDP protocol
	 */
	public static final String protocol = "protocol";

	/**
	 * Default value of the protocol parameter. <br>
	 * TODO Fix this protocol stuff !!
	 */
	public static final String protocolDefault = "udp"; // !! strange !?

	/**
	 * Defines the packet loss in argument of the
	 * <code>jist.swans.net.NetIp</code> constructor method. The acceptable
	 * values are the following: <br>
	 * <ul>
	 * <li>zero</li>
	 * <li>uniform</li>
	 * </ul>
	 */
	public static final String packetlossin = "packetlossin";

	/**
	 * Default value of the packet loss in parameter.
	 */
	public static final String packetlossinDefault = "zero";

	/**
	 * Defines the packet loss out argument of the
	 * <code>jist.swans.net.NetIp</code> constructor method. The acceptable
	 * values are the following: <br>
	 * <ul>
	 * <li>zero</li>
	 * <li>uniform</li>
	 * </ul>
	 */
	public static final String packetlossout = "packetlossout";

	/**
	 * Default value of the packet loss out parameter.
	 */
	public static final String packetlossoutDefault = "zero";

	/**
	 * Defines the prob argument of the <code>jist.swans.net.PacketLoss.Uniform</code> constructor method. <br>
	 * TODO does not work at all for the moment !! <br>.
	 * TODO XML structure to be adapted .
	 * TODO should not be set if Zero is chosen for the packet loss in / out is set.
	 */
	public static final String probaPl = "probaPl";

	/**
	 * Default value of the prob parameter. <br>
	 * TODO detail the comments here !
	 */
	public static final double probaPlDefault = 0.0;

	/**
	 * Tag of the XML file. Defines a structure dealing with the orders of the
	 * models.
	 */
	public static final String Orders = "Orders";

	public static final String Order = "Order";

	public static final String orderedmodel = "orderedmodel";

	public static final String qtity = "qtity";

	// Real parameter default values

	/**
	 * Default value of the protocol number. <br>
	 * TODO Fix this protocol stuff !!
	 */
	public static final short protocolNbreDefault = Constants.NET_PROTOCOL_UDP;

	//	 List to know the type of a parameter
	/**
	 * List of the parameter of type <code>java.lang.String</code>.
	 */
	public static ArrayList StringList;

	/**
	 * List of the parameter of type int.
	 */
	public static ArrayList intList;

	/**
	 * List of the parameter of type double.
	 */
	public static ArrayList doubleList;

	// Lists contaning the possible values of a given parameter
	/**
	 * List containing all the possible values for the spatial parameter.
	 */
	public static ArrayList spatialRange;

	/**
	 * List containing all the possible values for the fading parameter.
	 */
	public static ArrayList fadingRange;

	/**
	 * List containing all the possible values for the path loss parameter.
	 */
	public static ArrayList pathlossRange;

	/**
	 * List containing all the possible values for the radio model.
	 */
	public static ArrayList radioModelRange;

	/**
	 * List containing all the possible values for the mac model.
	 */
	public static ArrayList macModelRange;

	/**
	 * List containing all the possible values for the packet loss model.
	 */
	public static ArrayList packetLossRange;
	
	public static ArrayList mobilitymodelRange;

	public static boolean isFirst = true;

	/**
	 * Initialize the lists. This method calls the <code>createLists()</code>
	 * method, and the <code>createRange()</code> method.
	 */
	public static void init()
	{
		if (isFirst)
		{
			createLists();
			createRange();
			isFirst = false;
		}
	}

	/**
	 * Creates three lists: <code>StringList</code>, <code>intList</code>
	 * and <code>doubleList</code>. This method calls the three corresponding
	 * method: <code>createStringList()</code>, <code>createIntList()</code>
	 * and <code>createDoubleList()</code>.
	 */
	private static void createLists()
	{
		StringList = createStringList();
		intList = createIntList();
		doubleList = createDoubleList();
	}

	/**
	 * Create the <code>StringList</code>. This list contains all the
	 * varialbes having the type <code>java.lang.String</code>. This will be
	 * used by the <code>Checker</code> when it will check the type of a
	 * variable.
	 * 
	 * @return the <code>ArrayList</code> containing all the variables of type
	 *         <code>java.lang.String</code>.
	 */
	private static ArrayList createStringList()
	{
		ArrayList stringTab = new ArrayList();
		stringTab.add(SimParamList.radiomodel);
		stringTab.add(SimParamList.macmodel);
		stringTab.add(SimParamList.packetlossin);
		stringTab.add(SimParamList.packetlossout);
		stringTab.add(SimParamList.spatial);
		stringTab.add(SimParamList.fading);
		stringTab.add(SimParamList.pathloss);
		stringTab.add(SimParamList.positiontype);
		
		return stringTab;
	}

	/**
	 * Create the <code>intList</code>. This list contains all the varialbes
	 * having the type int. This will be used by the <code>Checker</code> when
	 * it will check the type of a variable.
	 * 
	 * @return the <code>ArrayList</code> containing all the variables of type
	 *         int.
	 */
	private static ArrayList createIntList()
	{
		ArrayList intTab = new ArrayList();
		intTab.add(SimParamList.fieldlength);
		intTab.add(SimParamList.fieldwidth);
		intTab.add(SimParamList.minspeed);
		intTab.add(SimParamList.maxspeed);
		intTab.add(SimParamList.precision);
		intTab.add(SimParamList.bandwidth);
		intTab.add(SimParamList.posx);
		intTab.add(SimParamList.posy);
		
		return intTab;
	}

	/**
	 * Create the <code>doubleList</code>. This list contains all the varialbes
	 * having the type double. This will be used by the <code>Checker</code> when
	 * it will check the type of a variable.
	 * 
	 * @return the <code>ArrayList</code> containing all the variables of type
	 *         double.
	 */
	private static ArrayList createDoubleList()
	{
		ArrayList doubleTab = new ArrayList();
		doubleTab.add(SimParamList.propagationlimit);
		doubleTab.add(SimParamList.frequency);
		doubleTab.add(SimParamList.transmitpower);
		doubleTab.add(SimParamList.antennagain);
		doubleTab.add(SimParamList.sensitivity_mW);
		doubleTab.add(SimParamList.threshold_mW);
		doubleTab.add(SimParamList.fieldtemperature_K);
		doubleTab.add(SimParamList.thermalfactor);
		doubleTab.add(SimParamList.ambientnoise_mW);
		doubleTab.add(SimParamList.SNRThreshold);
		doubleTab.add(SimParamList.probaPl);
		doubleTab.add(SimParamList.fixedradius);
		doubleTab.add(SimParamList.randomradius);
		doubleTab.add(SimParamList.pausetime);

		return doubleTab;
	}

	/**
	 * Fills the different <code>ArrayList</code> containing the allowed value
	 * for the parameters. The name of the <code>ArrayList</code>s are called
	 * as follows: name-of-the-parameterRange. For the moment, this method calls
	 * <code>createSpatialRange()</code>, <code>createFadingRange</code> 
	 * <code>createPathlossRange</code>,
	 * <code>createRadioModelRange</code>, <code>createMacModelRange</code>
	 * and the <code>createPacketLossRange</code> methods.
	 */
	private static void createRange()
	{
		spatialRange = createSpatialRange();
		fadingRange = createFadingRange();
		pathlossRange = createPathlossRange();
		radioModelRange = createRadioModelRange();
		macModelRange = createMacModelRange();
		packetLossRange = createPacketLossRange();
		mobilitymodelRange = createMobilityModelRange();
	}

	/**
	 * Fills an <code>ArrayList</code> with the allowed values for the spatial
	 * parameter.
	 * 
	 * @return An <code>ArrayList</code> with the allowed values for the
	 *         spatial parameter.
	 */
	private static ArrayList createSpatialRange()
	{
		ArrayList spatialRange = new ArrayList();
		spatialRange.add("grid");
		spatialRange.add("hiergrid");
		spatialRange.add("linearlist");
		/*
		spatialRange.add("spatialtransmitvisitor");
		spatialRange.add("spatialvisitor");
		spatialRange.add("tiledwraparound");
		*/
		return spatialRange;
	}

	/**
	 * Fills an <code>ArrayList</code> with the allowed values for the radio
	 * model.
	 * 
	 * @return An <code>ArrayList</code> with the allowed values for the radio
	 *         model.
	 */
	private static ArrayList createRadioModelRange()
	{
		ArrayList radioRange = new ArrayList();
		radioRange.add("additive");
		radioRange.add("independant");
		return radioRange;
	}

	/**
	 * Fills an <code>ArrayList</code> with the allowed values for the fading
	 * parameter.
	 * 
	 * @return An <code>ArrayList</code> with the allowed values for the
	 *         fading parameter.
	 */
	private static ArrayList createFadingRange()
	{
		ArrayList fadingRange = new ArrayList();
		fadingRange.add("none");
		fadingRange.add("rayleigh");
		//fadingRange.add("rician");
		return fadingRange;
	}

	/**
	 * Fills an <code>ArrayList</code> with the allowed values for the
	 * pathloss parameter.
	 * 
	 * @return An <code>ArrayList</code> with the allowed values for the
	 *         pathloss parameter.
	 */
	private static ArrayList createPathlossRange()
	{
		ArrayList pathlossRange = new ArrayList();
		pathlossRange.add("freespace");
		pathlossRange.add("tworay");
		return pathlossRange;
	}

	/**
	 * Fills an <code>ArrayList</code> with the allowed values for the mac
	 * model.
	 * 
	 * @return An <code>ArrayList</code> with the allowed values for the mac
	 *         model.
	 */
	private static ArrayList createMacModelRange()
	{
		ArrayList macRange = new ArrayList();
		macRange.add("dumb");
		macRange.add("802_11");
		return macRange;
	}

	/**
	 * Fills an <code>ArrayList</code> with the allowed values for the packet
	 * loss parameter.
	 * 
	 * @return An <code>ArrayList</code> with the allowed values for the
	 *         packet loss parameter.
	 */
	private static ArrayList createPacketLossRange()
	{
		ArrayList plRange = new ArrayList();
		plRange.add("zero");
		plRange.add("uniform");
		return plRange;
	}
	
	private static ArrayList createMobilityModelRange()
	{
		ArrayList mobModelRange = new ArrayList();
		mobModelRange.add("static");
		mobModelRange.add("randomwalk");
		mobModelRange.add("randomwaypoint");
		
		return mobModelRange;
	}
}
