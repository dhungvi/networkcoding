/*
 * Created on Oct 12, 2005
 *
 */
package simjistwrapper.utils.realstruct;

/**
 * Represents a <code>double</code> parameter.
 * 
 * @author Clergue Jeremie
 *
 */
public class DoubleParameter extends Parameter
{
    /**
     * Value of the <code>double</code> parameter.
     */
    private double value;
    
    /**
     * Default constructor. The value of the <code>type</code> is hardcoded to
     * be "double".
     * 
     * @param name
     *            name of the parameter.
     * @param value
     *            value of the parameter having type <code>double</code>.
     */
    public DoubleParameter(String name, double value)
    {
        super();
        super.name = name;
        super.type = "double";
        this.value = value;
    }

    /**
     * @return Returns the value of the parameter.
     */
    public double getValue()
    {
        return value;
    }
}
