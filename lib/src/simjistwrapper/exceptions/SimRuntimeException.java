package simjistwrapper.exceptions;

public class SimRuntimeException extends SimException
{
	public SimRuntimeException(String msg)
	{
		super(msg);
	}
	
	public SimRuntimeException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}
