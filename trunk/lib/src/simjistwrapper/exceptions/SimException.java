package simjistwrapper.exceptions;

public class SimException extends Exception
{
	public SimException(String msg)
	{
		super(msg);
	}
	
	public SimException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}
