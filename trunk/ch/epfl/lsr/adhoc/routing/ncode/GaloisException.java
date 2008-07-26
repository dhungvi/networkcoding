package ch.epfl.lsr.adhoc.routing.ncode;

//package GF;

public class GaloisException extends Exception {
	private String outstr;
	private int code;

	public static final int POLYNOMIAL = 0;
	public static final int DIVIDE = 1;
	public static final int MODULOPOLY = 2;
	public static final int PRIMITIVE = 3;
	public static final int PARSE = 4;
	public static final int FIELDCONV = 5;
	public static final int GENERAL = 6;
	public static final int CODE = 7;

	public GaloisException(String newstr, int newcode) {
		outstr = newstr;
		code = newcode;
	}

	public String toString() {
		return "GaloisException: " + outstr;
	}
}
