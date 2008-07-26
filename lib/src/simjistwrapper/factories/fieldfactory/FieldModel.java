package simjistwrapper.factories.fieldfactory;

import simjistwrapper.exceptions.*;
import simjistwrapper.utils.realstruct.*;
import simjistwrapper.utils.simstruct.SimParamList;

public class FieldModel
{
	protected int fieldlength;
	protected int fieldwidth;
	protected String spatial;
	protected String fading;
	protected String pathloss;
	protected double propagationlimit;
	protected String mobilitymodel;
	protected double fixedradius;
	protected double randomradius;
	protected double pausetime;
	protected int minspeed;
	protected int maxspeed;
	protected int precision;
	
	public FieldModel()
	{
		setDefaultValues();
	}
	
	private void setDefaultValues()
	{
		fieldlength = SimParamList.fieldlengthDefault;
		fieldwidth = SimParamList.fieldwidthDefault;
		spatial = SimParamList.spatialDefault;
        fading = SimParamList.fadingDefault;
        pathloss = SimParamList.pathlossDefault;
        propagationlimit = SimParamList.propagationlimitDefault;
        mobilitymodel = SimParamList.mobilitymodelDefault;
        fixedradius = SimParamList.fixedradiusDefault;
        randomradius = SimParamList.randomradiusDefault;
        pausetime = SimParamList.pausetimeDefault;
        minspeed = SimParamList.minspeedDefault;
        maxspeed = SimParamList.maxspeedDefault;
        precision = SimParamList.precisionDefault;
	}
	
	public void setParameter(IParameter param) throws SemanticException
    {
        if(param.getName().equals(SimParamList.fieldlength))
            fieldlength = ((IntParameter)param).getValue();
        else if(param.getName().equals(SimParamList.fieldwidth))
            fieldwidth = ((IntParameter)param).getValue();
        else if(param.getName().equals(SimParamList.spatial))
            spatial = ((StringParameter)param).getValue();
        else if(param.getName().equals(SimParamList.fading))
            fading = ((StringParameter)param).getValue();
        else if(param.getName().equals(SimParamList.pathloss))
            pathloss = ((StringParameter)param).getValue();
        else if(param.getName().equals(SimParamList.propagationlimit))
            propagationlimit = ((DoubleParameter)param).getValue();
        else if(param.getName().equals(SimParamList.mobilitymodel))
        	mobilitymodel = ((StringParameter)param).getValue();
        else if(param.getName().equals(SimParamList.fixedradius))
            fixedradius = ((DoubleParameter)param).getValue();
        else if(param.getName().equals(SimParamList.randomradius))
            randomradius = ((DoubleParameter)param).getValue();
        else if(param.getName().equals(SimParamList.pausetime))
            pausetime = ((DoubleParameter)param).getValue();
        else if(param.getName().equals(SimParamList.minspeed))
            minspeed = ((IntParameter)param).getValue();
        else if(param.getName().equals(SimParamList.maxspeed))
            maxspeed = ((IntParameter)param).getValue();
        else if(param.getName().equals(SimParamList.precision))
            precision = ((IntParameter)param).getValue();
        else
        	throw new SemanticException(
					"Parameter not present in the field model");
    }
	
	public void showAll()
	{
		System.out.println("FIELD PROPERTIES: ");
		System.out.println("DIMENSIONS OF THE FIELD:");
		System.out.println("length : " + fieldlength);
		System.out.println("width : " + fieldwidth);
		System.out.println("PROPERTIES :");
		System.out.println("spatial : " + spatial);
		System.out.println("fading : " + fading);
		System.out.println("pathloss : " + pathloss);
		System.out.println("propagation limit : " + propagationlimit);
		System.out.println("MOBILITY PROPERTIES : ");
		System.out.println("mobility model : " + mobilitymodel);
		System.out.println("fixedradius : " + fixedradius);
		System.out.println("randomradius : " + randomradius);
		System.out.println("pausetime : " + pausetime);
		System.out.println("minspeed : " + minspeed);
		System.out.println("maxspeed : " + maxspeed);
		System.out.println("precision : " + precision);
	}
}
