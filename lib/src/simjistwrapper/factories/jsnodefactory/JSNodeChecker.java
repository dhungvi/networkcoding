package simjistwrapper.factories.jsnodefactory;

import org.w3c.dom.Node;
import org.w3c.dom.Element;
import jist.swans.misc.Location;
import simjistwrapper.exceptions.*;
import simjistwrapper.utils.realstruct.*;
import simjistwrapper.utils.simstruct.*;

/**
 * Provides methods for checking if the parameters of an XML file fits some
 * expectations. <br>
 * The checks are made in two steps. First the format is checked, if this test
 * passes, an object <code>Parameter</code> is created. Depending on the type,
 * it can be a <code>StringParameter</code> or a <code>IntParameter</code>
 * or a <code>DoubleParameter<code>. Then, the content is checked. This
 * is done with the range defined in the <code>parameters.SimParamList</code> file.
 * 
 * @author Clergue Jeremie
 */
public class JSNodeChecker
{
	/**
	 * Takes the name and the value of a parameter. Then, two tests are made.
	 * One checks the format calling the <code>checkFormat</code> method. The
	 * second checks the correctness of the value.
	 * 
	 * @param name
	 *            the name of the parameter
	 * @param value
	 *            the value of the parameter
	 * @return a <code>parameter</code> object. Depending on the type of the
	 *         object, it can be a <code>StringParameter</code> or a
	 *         <code>IntParameter</code> or a <code>DoubleParameter<code>.
	 */
	public static IParameter check(String name, String value)
			throws SemanticException
	{
		IParameter parameter = checkFormat(name, value);
		IParameter param = checkContent(parameter);
		return param;
	}
	
	public static void checkPosition(Node XMLnode) throws SemanticException
	{
		if (((Element) XMLnode).getAttribute(SimParamList.positiontype).equals(
				"random")
				&& XMLnode.getChildNodes().getLength() > 0)
			throw new SemanticException(
					"if random position type is chosen, then no position should be set");
		else if (((Element) XMLnode).getAttribute(SimParamList.positiontype)
				.equals("deterministic")
				&& XMLnode.getChildNodes().getLength() != 2)
			throw new SemanticException(
					"if deterministic position type is chosen, then both positions must be set");
	}
	
	public static void checkPositionWithinBounds(Location position,
			Location bounds) throws SemanticException
	{
		if (!position.inside(bounds))
			throw new SemanticException(
					"The location is not in the bounds of the defined filed");
	}

	/**
	 * Checks the format of a parameter and creates a corresponding
	 * <code>Parameter</code>. This method checks to what list the parameter
	 * belongs to from the list given in <code>parameters.SimParamList</code>.
	 * Depending on this, the proper <code>Parameter</code> object is created.
	 * If the format does not fit the expectation, an error is raised.<br>
	 * TODO For the moment, no error is raised ! <br>
	 * If a String is expected, it must start with a letter, otherwise, the
	 * String will be rejected. <br>
	 * 
	 * @param name
	 *            the name of the parameter to be checked
	 * @param value
	 *            the value of the parameter to be checked
	 * @return a <code>parameter</code> object. Depending on the type of the
	 *         object, it can be a <code>StringParameter</code> or a
	 *         <code>IntParameter</code> or a <code>DoubleParameter<code>.
	 */
	private static IParameter checkFormat(String name, String value)
			throws SemanticException
	{
		IParameter param = null;
		if (SimParamList.StringList.contains(name))
			param = new StringParameter(name, value);
		else if (SimParamList.intList.contains(name))
		{
			param = new IntParameter(name, Integer.valueOf(value).intValue());
			if ((param.getName().equals(SimParamList.posx) || param.getName()
					.equals(SimParamList.posy))
					&& ((IntParameter) param).getValue() < 0)
				throw new SemanticException("Positions must be positive values");
		}
		else if (SimParamList.doubleList.contains(name))
			param = new DoubleParameter(name, Double.valueOf(value)
					.doubleValue());
		else
			throw new SemanticException("Unknown parameter type");
		return param;
	}

	/**
	 * This method should check if the Strings put into the XML file are strings and not
	 * numbers. But the argument for the macmodel is 802_11 which is a number, and thus
	 * should not be rejected !! <br> 
	 * TODO fix that ! <br> 
	 * 
	 * @param value
	 * @return
	 */
	private static String checkString(String value) throws SemanticException
	{

		if (!isNumber(value.charAt(0)))
			return value;
		else
			throw new SemanticException(
					"A String begins with a number. This is not a proper String.");
	}

	/**
	 * Checks if the parameter given in arguement hold a valid value. <br>
	 * This method simply call three sub-method depending on the
	 * <code>type</code> value of the parameter.<br>
	 * An error is raised if the value does not fit to the expectations.<br>
	 * TODO what if the type is neither String, int nor double ?
	 * 
	 * @param param
	 *            the parameter to be checked
	 * @return the parameter given in argument.
	 */
	private static IParameter checkContent(IParameter param)
			throws SemanticException
	{
		if (param.getType().equals("String"))
			checkContent(param.getName(), ((StringParameter) param).getValue());
		else if (param.getType().equals("int"))
			checkContent(param.getName(), ((IntParameter) param).getValue());
		else if (param.getType().equals("double"))
			checkContent(param.getName(), ((DoubleParameter) param).getValue());
		else
			throw new SemanticException("the type of the parameter is unknown");
			
		return param;
	}

	/**
	 * Checks the content of a parameter when the
	 * <code>type</type> of the parameter is <code>String</code>. This method
	 * checks if the value is in the range defined in the <code>SimParamList
	 * </code>. <br>
	 * If a parameter is to be added, a line should be added here : <br>
	 * Let us call the new variable <code>plop</code>, then the new line to be
	 *  added to this method should be as follows:<br>
	 * <pre>
	 * if(name.equals(SimParamList.plop) &amp;&amp; !SimParamList.plopRange.contains(value))
	 *     System.out.println(&quot; !!! error with the plop&quot;);
	 * </pre>
	 * 
	 * TODO try to do it automatically, such that no line should be added if 
	 * someone adds a parameter. (List of all the parameters in the SimParamList class ?)
	 * 
	 * @param name the name of the parameter to be checked
	 * @param value the value of the parameter to be checked
	 */
	private static void checkContent(String name, String value)
			throws SemanticException
	{
		if (name.equals(SimParamList.radiomodel)
				&& !SimParamList.radioModelRange.contains(value))
			throw new SemanticException(name);
		else if (name.equals(SimParamList.macmodel)
				&& !SimParamList.macModelRange.contains(value))
			throw new SemanticException(name);
		else if (name.equals(SimParamList.packetlossin)
				&& !SimParamList.packetLossRange.contains(value))
			throw new SemanticException(name);
		else if (name.equals(SimParamList.packetlossout)
				&& !SimParamList.packetLossRange.contains(value))
			throw new SemanticException(name);
		else if (name.equals(SimParamList.spatial)
				&& !SimParamList.spatialRange.contains(value))
			throw new SemanticException(name);
		else if (name.equals(SimParamList.fading)
				&& !SimParamList.fadingRange.contains(value))
			throw new SemanticException(name);
		else if (name.equals(SimParamList.pathloss)
				&& !SimParamList.pathlossRange.contains(value))
			throw new SemanticException(name);
	}

	/**
	 * This method has the same role as the
	 * <code>checkContent(java.lang.String, java.lang.String)</code> method,
	 * but when the parameter has an int value. <br>
	 * For the moment, the method is empty.<br>
	 * TODO check the range if needed
	 * 
	 * @param name
	 *            the name of the parameter to be checked
	 * @param value
	 *            the value of the parameter to be checked
	 */
	private static void checkContent(String name, int value)
	{
	}

	/**
	 * This method has the same role as the
	 * <code>checkContent(java.lang.String, java.lang.String)</code> method,
	 * but when the parameter has an int value. <br>
	 * For the moment, the method is empty. <br>
	 * TODO check the range if needed
	 * 
	 * @param name
	 *            the name of the parameter to be checked
	 * @param value
	 *            the value of the parameter to be checked
	 */
	private static void checkContent(String name, double value)
	{
	}

	private static boolean isNumber(char letter)
	{
		int intLetter = (int) letter;
		if (intLetter >= 48 && intLetter <= 57)
			return true;
		else
			return false;
	}
}
