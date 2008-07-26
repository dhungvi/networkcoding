/*
 * Created on Oct 12, 2005
 *
 */
package simjistwrapper.utils.realstruct;

/**
 * Represents a parameter describing a simulation. Some subclasses already
 * exist. Each subclass represent a particular type. This class is declared
 * <code>abstract</code> such that subclasses must be created if another type
 * appears. A parameter has three mandatory instance variable: the name which
 * should be defined in the <code>parameters.SimParamList</code> class, the
 * type which should be exactly the type of the parameter. The third parameter
 * is not defined in this superclass but should be implemented in the
 * subclasses. The third parameter is the value parameter. Of course, the type
 * of the value must be of type <code>type</code>.
 * 
 * @author Clergue Jeremie
 */
public abstract class Parameter implements IParameter
{
    /**
     * Name of the parameter.
     */
    public String name;
    
    /**
     * Type of the parameter.
     */
    public String type;

    /**
     * @return Returns the name of the parameter.
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return Returns the type of the parameter.
     */
    public String getType()
    {
        return type;
    }
}
