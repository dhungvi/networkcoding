/*
 * Created on Oct 12, 2005
 *
 */
package simjistwrapper.utils.realstruct;

/**
 * Represents a <code>String</code> parameter.
 * 
 * @author Clergue Jeremie
 *
 */
public class StringParameter extends Parameter
{
    /**
     * Value of the <code>String</code> parameter.
     */
    private String value;
    
    /**
     * Default constructor. The value of the <code>type</code> is hardcoded to
     * be "String".
     * 
     * @param name
     *            name of the parameter.
     * @param value
     *            value of the parameter having type <code>String</code>.
     */
    public StringParameter(String name, String value)
    {
        super();
        super.name = name;
        super.type = "String";
        this.value = value;
    }

    /**
     * @return Returns the value of the parameter.
     */
    public String getValue()
    {
        return value;
    }
}
