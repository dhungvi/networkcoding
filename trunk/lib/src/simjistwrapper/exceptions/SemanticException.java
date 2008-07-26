/*
 * Created on Oct 24, 2005
 *
 */
package simjistwrapper.exceptions;

public class SemanticException extends SimException
{   
    public SemanticException(String msg)
    {
    	super(msg);
    }
    
    public SemanticException(String msg, Throwable cause)
    {
    	super(msg, cause);
    }
}
