package simjistwrapper.factories.fieldfactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import java.util.ArrayList;
import simjistwrapper.exceptions.*;
import simjistwrapper.utils.realstruct.*;
import simjistwrapper.utils.simstruct.SimParamList;

public class FieldChecker
{
	private static ArrayList randomWalkList;

	private static ArrayList randomWaypointList;

	public static void init() throws SemanticException
	{
		createLists();
	}
	
	public static IParameter check(String name, String value)
			throws SemanticException
	{
		IParameter param = null;
		if (SimParamList.StringList.contains(name))
		{
			param = new StringParameter(name, value);
			checkStringContent((StringParameter)param);
		}
		else if (SimParamList.intList.contains(name))
			param = new IntParameter(name, Integer.valueOf(value).intValue());
		else if (SimParamList.doubleList.contains(name))
			param = new DoubleParameter(name, Double.valueOf(value)
					.doubleValue());
		else
			throw new SemanticException("Unknown parameter type");
		return param;
	}

	public static void checkMobilityParams(Node XMLnode)
			throws SemanticException
	{
		String mobModel = ((Element) XMLnode)
				.getAttribute(SimParamList.mobilitymodel);
		NodeList mobParams = XMLnode.getChildNodes();
		for (int i = 0; i < mobParams.getLength(); i++)
		{
			Node mobParam = mobParams.item(i);
			if (mobModel.equals("static") && mobParams.getLength() > 0)
				throw new SemanticException(
						"If static mobility is chosen, no parameter should be inserted");
			else if (mobModel.equals("randomwalk")
					&& !randomWalkList.contains(mobParam.getNodeName()))
				throw new SemanticException(
						"A parameter is not consistent with the mobility model");
			else if (mobModel.equals("randomwaypoint")
					&& !randomWaypointList.contains(mobParam.getNodeName()))
				throw new SemanticException(
						"A parameter is not consistent with the mobility model");
		}
	}

	private static void createLists()
	{
		createParamListRandomWalk();
		createParamListRandomWaypoint();
	}

	private static void createParamListRandomWalk()
	{
		randomWalkList = new ArrayList();
		randomWalkList.add(SimParamList.fixedradius);
		randomWalkList.add(SimParamList.randomradius);
		randomWalkList.add(SimParamList.pausetime);
	}

	private static void createParamListRandomWaypoint()
	{
		randomWaypointList = new ArrayList();
		randomWaypointList.add(SimParamList.minspeed);
		randomWaypointList.add(SimParamList.maxspeed);
		randomWaypointList.add(SimParamList.pausetime);
		randomWaypointList.add(SimParamList.precision);
	}
	
	private static void checkStringContent(StringParameter param)
			throws SemanticException
	{
		if (param.name.equals("sptatial")
				&& !SimParamList.spatialRange.contains(param.getValue()))
			throw new SemanticException("spatial value not in range");
		else if (param.name.equals("pathloss")
				&& !SimParamList.pathlossRange.contains(param.getValue()))
			throw new SemanticException("spatial value not in range");
		else if (param.name.equals("fading")
				&& !SimParamList.fadingRange.contains(param.getValue()))
			throw new SemanticException("spatial value not in range");
		else
			throw new SemanticException("unknown parameter name");
	}
}
