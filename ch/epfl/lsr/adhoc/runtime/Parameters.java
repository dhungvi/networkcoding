package ch.epfl.lsr.adhoc.runtime;

import java.util.Hashtable;
import java.util.Enumeration;

/**
 * This class represents a set of parameters identified by
 * a {@code String} of caracters. This class provides several
 * methods to retrieve {@code String}, {@code boolean}, {@code short},
 * {@code int}, {@code long} or {@code double} parameters.
 *
 * It is initially intended for storing {@code AsynchronousLayers} 
 * and {@code Services} parameters read from the XML config file.
 *
 * @author David Cavin, LSR
 * @see ch.epfl.lsr.adhoc.communicationslayer.AsynchronousLayer
 */

public class Parameters {

	// Internal representation of the parameters,
	private Hashtable params;

	/**
	 * Constructs a new empty Parameter object.
	 */
	public Parameters() {
		params = new Hashtable();
	}

	/**
	 * Adds a new parameter {@code (name.value)} to this set of parameters.
	 *
	 * @param name The name of the new parameter.
	 * @param value The value associated with the new parameter name
	 */
	protected void addParameter(String name, String value) {
		params.put(name,value);
	}

	/**
	 * Checks whether this set of parameters is empty.
	 *
	 * @return {@code false} if this set of parameters is empty, 
	 *         {@code false} otherwise.
	 */
	public boolean hasParameters() {
		return !params.isEmpty();
	}

	/**
	 * Returns the number of parameters currently defined in this set of parameters.
	 *
	 * @return The current number of parameters in this set of parameters.
	 */
	public int size() {
		return params.size();
	}

	/**
	 * Returns an array of all the parameters names currently defined
	 * in this set of parameters.
	 *
	 * @return An array containing all the paramters names defined in 
	 *         this set of parameters.
	 */
	public String[] names() {
		return (String[])params.keySet().toArray(new String[size()]);
	}

	/**
	 * Returns the value of the parameter {@code name}.
	 *
	 * @param key The name of an existing parameter.
	 * @return The {@code String} associated with the parameter's name.
	 * @throws ParamDoesNotExistException If no paramter {@code name} is 
	 *                                    defined in this set of parameters.
	 */
	protected String getValue(String key) throws ParamDoesNotExistException {
		String value = (String)params.get(key);
		if(value == null)
			throw new ParamDoesNotExistException("Parameter \""+key+"\" does not exist.");
		return value;
	}

	/**
	 * Return a {@code boolean} representation of the value associated 
	 * with the parameter {@code name}. If the conversion fails, a
	 * {@code RuntimeException} is raised.
	 *
	 * @param name The parameter's name.
	 * @return A {@code boolean} representation of the parameter {@code name}. 
	 * @throws ParamDoesNotExistException If no paramter {@code name} is 
	 *                                    defined in this set of parameters.
	 */
	public boolean getBoolean(String name) throws ParamDoesNotExistException {
		return Boolean.valueOf(getValue(name)).booleanValue();			
	}

	/**
	 * Return a {@code boolean} representation of the value associated 
	 * with the parameter {@code name} or {@code defaultValue} if the 
	 * parameter does not exist. If the conversion fails, a
	 * {@code RuntimeException} is raised.
	 *
	 * @param name The parameter's name.
	 * @param defaultValue A default value returned if parameter {@code name}
	 *                     does not exist.
	 * @return A {@code boolean} representation of the parameter {@code name}. 
	 *         defined in this set of parameters.
	 */
	public boolean getBoolean(String name, boolean defaultValue) {
		try {
			return getBoolean(name);
		} catch (ParamDoesNotExistException pdnee) {
			return defaultValue;
		} 
	}

	/**
	 * Return a {@code short} representation of the value associated 
	 * with the parameter {@code name}. If the conversion fails, a
	 * {@code RuntimeException} is raised.
	 *
	 * @param name The parameter's name.
	 * @return A {@code short} representation of the parameter {@code name}. 
	 * @throws ParamDoesNotExistException If no paramter {@code name} is 
	 *                                    defined in this set of parameters.
	 */
	public short getShort(String name) throws ParamDoesNotExistException {
		return Short.parseShort(getValue(name));			
	}

	/**
	 * Return a {@code short} representation of the value associated 
	 * with the parameter {@code name} or {@code defaultValue} if the 
	 * parameter does not exist. If the conversion fails, a
	 * {@code RuntimeException} is raised.
	 *
	 * @param name The parameter's name.
	 * @param defaultValue A default value returned if parameter {@code name}
	 *                     does not exist.
	 * @return A {@code short} representation of the parameter {@code name}. 
	 *         defined in this set of parameters.
	 */
	public short getShort(String name, short defaultValue) {
		try {
			return getShort(name);
		} catch (ParamDoesNotExistException pdnee) {
			return defaultValue;
		} 
	}

	/**
	 * Return a {@code short} representation of the value associated 
	 * with the parameter {@code name}. If the conversion fails, a
	 * {@code RuntimeException} is raised.
	 *
	 * @param name The parameter's name.
	 * @return A {@code short} representation of the parameter {@code name}. 
	 * @throws ParamDoesNotExistException If no paramter {@code name} is 
	 *                                    defined in this set of parameters.
	 */
	public int getInt(String name) throws ParamDoesNotExistException {
		return Integer.parseInt(getValue(name));			
	}

	/**
	 * Return a {@code int} representation of the value associated 
	 * with the parameter {@code name} or {@code defaultValue} if the 
	 * parameter does not exist. If the conversion fails, a
	 * {@code RuntimeException} is raised.
	 *
	 * @param name The parameter's name.
	 * @param defaultValue A default value returned if parameter {@code name}
	 *                     does not exist.
	 * @return A {@code int} representation of the parameter {@code name}. 
	 *         defined in this set of parameters.
	 */
	public int getInt(String name, int defaultValue) {
		try {
			return getInt(name);
		} catch (ParamDoesNotExistException pdnee) {
			return defaultValue;
		} 
	}

	/**
	 * Return a {@code long} representation of the value associated 
	 * with the parameter {@code name}. If the conversion fails, a
	 * {@code RuntimeException} is raised.
	 *
	 * @param name The parameter's name.
	 * @return A {@code long} representation of the parameter {@code name}. 
	 * @throws ParamDoesNotExistException If no paramter {@code name} is 
	 *                                    defined in this set of parameters.
	 */
	public long getLong(String name) throws ParamDoesNotExistException {
		return Long.parseLong(getValue(name));			
	}

	/**
	 * Return a {@code long} representation of the value associated 
	 * with the parameter {@code name} or {@code defaultValue} if the 
	 * parameter does not exist. If the conversion fails, a
	 * {@code RuntimeException} is raised.
	 *
	 * @param name The parameter's name.
	 * @param defaultValue A default value returned if parameter {@code name}
	 *                     does not exist.
	 * @return A {@code long} representation of the parameter {@code name}. 
	 *         defined in this set of parameters.
	 */
	public long getLong(String name, long defaultValue) {
		try {
			return getLong(name);
		} catch (ParamDoesNotExistException pdnee) {
			return defaultValue;
		} 
	}

	/**
	 * Return a {@code double} representation of the value associated 
	 * with the parameter {@code name}. If the conversion fails, a
	 * {@code RuntimeException} is raised.
	 *
	 * @param name The parameter's name.
	 * @return A {@code double} representation of the parameter {@code name}. 
	 * @throws ParamDoesNotExistException If no paramter {@code name} is 
	 *                                    defined in this set of parameters.
	 */
	public double getDouble(String name) throws ParamDoesNotExistException {
		return Double.parseDouble(getValue(name));			
	}
	
	/**
	 * Return a {@code double} representation of the value associated 
	 * with the parameter {@code name} or {@code defaultValue} if the 
	 * parameter does not exist. If the conversion fails, a
	 * {@code RuntimeException} is raised.
	 *
	 * @param name The parameter's name.
	 * @param defaultValue A default value returned if parameter {@code name}
	 *                     does not exist.
	 * @return A {@code double} representation of the parameter {@code name}. 
	 *         defined in this set of parameters.
	 */
	public double getDouble(String name, double defaultValue) {
		try {
			return getDouble(name);
		} catch (ParamDoesNotExistException pdnee) {
			return defaultValue;
		} 
	}

	/**
	 * Return a {@code String} representation of the value associated 
	 * with the parameter {@code name}. If the conversion fails, a
	 * {@code RuntimeException} is raised.
	 *
	 * @param name The parameter's name.
	 * @return A {@code String} representation of the parameter {@code name}. 
	 * @throws ParamDoesNotExistException If no paramter {@code name} is 
	 *                                    defined in this set of parameters.
	 */
	public String getString(String name) throws ParamDoesNotExistException {
		return getValue(name);
	}

	/**
	 * Return a {@code String} representation of the value associated 
	 * with the parameter {@code name} or {@code defaultValue} if the 
	 * parameter does not exist. If the conversion fails, a
	 * {@code RuntimeException} is raised.
	 *
	 * @param name The parameter's name.
	 * @param defaultValue A default value returned if parameter {@code name}
	 *                     does not exist.
	 * @return A {@code String} representation of the parameter {@code name}. 
	 *         defined in this set of parameters.
	 */
	public String getString(String name, String defaultValue) {
		try {
			return getString(name);
		} catch (ParamDoesNotExistException pdnee) {
			return defaultValue;
		} 
	}

	/**
	 * {@inheritDoc} 
	 */
	public String toString() {
		String res ="{";
		Enumeration keys = params.keys();
		while(keys.hasMoreElements()) {
			Object key = keys.nextElement();
			res+="("+key+","+params.get(key)+(keys.hasMoreElements()?");":")");
		}
		res+="}";
		return res;
	}

}
