/*
 * Created on Oct 12, 2005
 *
 */
package simjistwrapper.factories.jsnodefactory;

import simjistwrapper.exceptions.*;
import simjistwrapper.utils.realstruct.*;
import simjistwrapper.utils.simstruct.SimParamList;

/**
 * 
 * @author Clergue Jeremie
 */
public class JSNodeModel
{
	protected String positiontype;
	protected int posx;
	protected int posy;
	protected String radiomodel;
    protected double propagationlimit;
    protected double frequency;
    protected int bandwidth;
    protected double transmitpower;
    protected double antennagain;
    protected double sensitivity_mW;
    protected double threshold_mW;
    protected double fieldtemperature_K;
    protected double thermalfactor;
    protected double ambientnoise_mW;
    protected double SNRThreshold;
    protected String macmodel;
    protected String protocol;
    protected short protocolNbre;
    protected String packetlossin;
    protected String packetlossout;
    protected double probaPl;
    
    protected int id;
    
    public JSNodeModel(int id)
    {
        this.id = id;
        
        setDefaultValues();
    }
    
    /**
     * Assigns the default value to each one of the instance variables defined.
     * The default variable should be defined in the <code>parameter</code>
     * package.
     */
    private void setDefaultValues()
    {
    	positiontype = SimParamList.positiontypeDefault;
    	posx = -1; // not set
    	posy = -1; // not set
        radiomodel = SimParamList.radiomodelDefault;
        frequency = SimParamList.frequencyDefault;
        bandwidth = SimParamList.bandwidthDefault;
        transmitpower = SimParamList.transmitpowerDefault;
        antennagain = SimParamList.antennagainDefault;
        sensitivity_mW = SimParamList.sensitivity_mWDefault;
        threshold_mW = SimParamList.threshold_mWDefault;
        fieldtemperature_K = SimParamList.fieldtemperature_KDefault;
        thermalfactor = SimParamList.thermalfactorDefault;
        ambientnoise_mW = SimParamList.ambientnoise_mWDefault;
        SNRThreshold = SimParamList.SNRThresholdDefault;
        macmodel = SimParamList.macmodelDefault;
        protocol = SimParamList.protocolDefault;
        protocolNbre = SimParamList.protocolNbreDefault;
        packetlossin = SimParamList.packetlossinDefault;
        packetlossout = SimParamList.packetlossoutDefault;
        probaPl = SimParamList.probaPlDefault;
    }
    
    public void setParameter(IParameter param) throws SemanticException
    {
        if(param.getName().equals(SimParamList.radiomodel))
            radiomodel = ((StringParameter)param).getValue();
        else if(param.getName().equals(SimParamList.frequency))
            frequency = ((DoubleParameter)param).getValue();
        else if(param.getName().equals(SimParamList.bandwidth))
            bandwidth = ((IntParameter)param).getValue();
        else if(param.getName().equals(SimParamList.transmitpower))
            transmitpower = ((DoubleParameter)param).getValue();
        else if(param.getName().equals(SimParamList.antennagain))
            antennagain = ((DoubleParameter)param).getValue();
        else if(param.getName().equals(SimParamList.sensitivity_mW))
            sensitivity_mW = ((DoubleParameter)param).getValue();
        else if(param.getName().equals(SimParamList.threshold_mW))
            threshold_mW = ((DoubleParameter)param).getValue();
        else if(param.getName().equals(SimParamList.fieldtemperature_K))
            fieldtemperature_K = ((DoubleParameter)param).getValue();
        else if(param.getName().equals(SimParamList.thermalfactor))
            thermalfactor = ((DoubleParameter)param).getValue();
        else if(param.getName().equals(SimParamList.ambientnoise_mW))
            ambientnoise_mW = ((DoubleParameter)param).getValue();
        else if(param.getName().equals(SimParamList.SNRThreshold))
            SNRThreshold = ((DoubleParameter)param).getValue();
        else if(param.getName().equals(SimParamList.macmodel))
            macmodel = ((StringParameter)param).getValue();
        else if(param.getName().equals(SimParamList.packetlossin))
            packetlossin = ((StringParameter)param).getValue();
        else if(param.getName().equals(SimParamList.packetlossout))
            packetlossout = ((StringParameter)param).getValue();
        else if(param.getName().equals(SimParamList.probaPl))
            probaPl = ((DoubleParameter)param).getValue();
        else if(param.getName().equals(SimParamList.positiontype))
        	positiontype = ((StringParameter)param).getValue();
        else if(param.getName().equals(SimParamList.posx))
        	posx = ((IntParameter)param).getValue();
        else if(param.getName().equals(SimParamList.posy))
        	posy = ((IntParameter)param).getValue();
        else
        	throw new SemanticException("Parameter not present in the field model");
    }
    
    public void showAll()
    {
        System.out.println("MODEL #" + id);
        System.out.println("position type : " + positiontype);
        System.out.println("posx : " + posx);
        System.out.println("posy : " + posy);
        System.out.println("NODE MODEL LAYER");
        System.out.println("propagation limit : " + propagationlimit);
        System.out.println("\nRADIO LAYER");
        System.out.println("radio model : " + radiomodel);
        System.out.println("frequency : " + frequency);
        System.out.println("bandwidth : " + bandwidth);
        System.out.println("transmit power : " + transmitpower);
        System.out.println("antennagain : " + antennagain);
        System.out.println("sensitivity : " + sensitivity_mW);
        System.out.println("threshold : " + threshold_mW);
        System.out.println("field temperature : " + fieldtemperature_K);
        System.out.println("thermal factor : " + thermalfactor);
        System.out.println("ambient noise : " + ambientnoise_mW);
        System.out.println("SNRThreshold : " + SNRThreshold);
        System.out.println("\nMAC LAYER");
        System.out.println("mac model : " + macmodel);
        System.out.println("protocol : " + protocol);
        System.out.println("packet loss in : " + packetlossin);
        System.out.println("packet loss out : " + packetlossout);
        System.out.println("proba Pl : " + probaPl);
        System.out.println("\n\n");
    }
}
