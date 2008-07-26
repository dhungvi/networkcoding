package simjistwrapper.exceptions;

import org.xml.sax.*;

public class ParsingException extends Exception
{
	public ParsingException(String msg)
	{
		super(msg);
		System.out.println(msg);
		System.exit(-1);
	}
	
	public ParsingException(String msg, Throwable cause)
	{
		super(msg, cause);
		System.out.println(msg);
		System.exit(-1);
	}
}
