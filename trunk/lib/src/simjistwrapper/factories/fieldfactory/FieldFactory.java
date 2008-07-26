package simjistwrapper.factories.fieldfactory;

import org.w3c.dom.*;

import jist.swans.field.*;
import jist.swans.misc.Location;
import simjistwrapper.exceptions.*;
import simjistwrapper.factories.jsnodefactory.JSNodeChecker;
import simjistwrapper.utils.realstruct.IParameter;
import simjistwrapper.utils.simstruct.SimParamList;

public class FieldFactory
{
	private static Field field = null;

	private static Location.Location2D bounds = null;

	private static Document document;

	public static void init(Document locDoc) throws SemanticException
	{
		document = locDoc;
		FieldChecker.init();
		createField();
	}

	private static void createField() throws SemanticException
	{
		Element element = document.getDocumentElement();
		NodeList fieldLayers = element
				.getElementsByTagName(SimParamList.FieldLayer);
		
		createField(createFieldModel(fieldLayers.item(0)));
	}

	private static void createField(FieldModel fieldModel) throws SemanticException
	{
		bounds = new Location.Location2D(
				fieldModel.fieldlength, fieldModel.fieldwidth);
		// user should have the choice for the spatial stuff.. or delete it in the model.
		// TODO number of division should be set in the XML !
		Spatial spatial = null;
		if(fieldModel.spatial.equals("linearlist"))
			spatial = new Spatial.LinearList(bounds);
		else if(fieldModel.spatial.equals("grid"))
			spatial = new Spatial.Grid(bounds, 10);
		else if(fieldModel.spatial.equals("hiergrid"))
			spatial = new Spatial.HierGrid(bounds, 10);

		Fading fading = null;
		if (fieldModel.fading.equals("none"))
			fading = new Fading.None();
		else if (fieldModel.fading.equals("rayleigh"))
			fading = new Fading.Rayleigh();
		// TODO parameter needed
		//		else if(fieldModel.equals("rician"))
		//			fading = new Fading.Rician();

		else
			System.err.println("this should not happen (fading)");

		PathLoss pathloss = null;
		if (fieldModel.pathloss.equals("freespace"))
			pathloss = new PathLoss.FreeSpace();
		else if (fieldModel.pathloss.equals("tworay"))
			pathloss = new PathLoss.TwoRay();
		else
			System.err.println("this should not happen (pathloss)");
		double propagationLimit = fieldModel.propagationlimit;

		Mobility mobility = null;
		if (fieldModel.mobilitymodel.equals("static"))
			mobility = new Mobility.Static();
		else if (fieldModel.mobilitymodel.equals("randomwalk"))
			mobility = new Mobility.RandomWalk(bounds, fieldModel.fixedradius,
					fieldModel.randomradius, (long) fieldModel.pausetime);
		else if (fieldModel.mobilitymodel.equals("randomwaypoint"))
			mobility = new Mobility.RandomWaypoint(bounds,
					(long) fieldModel.pausetime, fieldModel.precision,
					fieldModel.minspeed, fieldModel.maxspeed);
		else
			throw new SemanticException("An error occured in the FieldFactory");

		// FIELD CREATION AT LAST : 
		field = new Field(spatial, fading, pathloss, mobility, propagationLimit);
	}

	public static FieldModel createFieldModel(Node XMLNode)
			throws SemanticException
	{
		FieldModel fieldModel = new FieldModel();

		// First, the attributes of the field
		fieldModel.setParameter(FieldChecker.check(SimParamList.fieldlength,
				((Element) XMLNode).getAttribute(SimParamList.fieldlength)));
		fieldModel.setParameter(FieldChecker.check(SimParamList.fieldwidth,
				((Element) XMLNode).getAttribute(SimParamList.fieldwidth)));

		// Second the nodes
		NodeList parameters = XMLNode.getChildNodes();
		for (int i = 0; i < parameters.getLength(); i++)
		{
			Node XMLparam = parameters.item(i);
			//if (XMLparam.getFirstChild() != null)
				if (XMLparam.getNodeName().equals(SimParamList.mobility))
				{
					FieldChecker.checkMobilityParams(XMLparam);
					String mobModel = ((Element)XMLparam).getAttribute("mobilitymodel");
					System.out.println("mobModel : " + mobModel);
					fieldModel.mobilitymodel =  mobModel;
					NodeList mobilityParams = XMLparam.getChildNodes();
					for (int j = 0; j < mobilityParams.getLength(); j++)
					{
						Node mobilityParam = mobilityParams.item(j);
						fieldModel.setParameter(FieldChecker.check(
								mobilityParam.getNodeName(), mobilityParam
										.getFirstChild().getNodeValue()));
					}
				} else
					fieldModel.setParameter(FieldChecker.check(XMLparam
							.getNodeName(), XMLparam.getFirstChild()
							.getNodeValue()));
		}

		fieldModel.showAll();
		return fieldModel;
	}

	public static Field getField() throws SimRuntimeException
	{
		if (field != null)
			return field;
		else
			throw new SimRuntimeException("Run createField() first !");
	}

	public static Location.Location2D getBounds() throws SimRuntimeException
	{
		if (bounds != null)
			return bounds;
		else
			throw new SimRuntimeException("Run createField() first !");
	}
}
