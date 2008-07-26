/*
 * Created on Oct 12, 2005
 *
 */
package simjistwrapper.utils.realstruct;

/**
 * Represents an <code>int</code> parameter.
 * 
 * @author Clergue Jeremie
 *
 */
public class IntParameter extends Parameter
{
    /**
     * Value of the <code>int</code> parameter.
     */
    private int value;
    
    /**
     * Default constructor. The value of the <code>type</code> is hardcoded to
     * be "int".
     * 
     * @param name
     *            name of the parameter.
     * @param value
     *            value of the parameter having type <code>int</code>.
     */
    public IntParameter(String name, int value)
    {
        super();
        super.name = name;
        super.type = "int";
        this.value = value;
    }

    /**
     * @return Returns the value of the parameter.
     */
    public int getValue()
    {
        return value;
    }
}
