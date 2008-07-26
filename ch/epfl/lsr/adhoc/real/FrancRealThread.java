package ch.epfl.lsr.adhoc.real;

import ch.epfl.lsr.adhoc.runtime.FrancThreadInterface;

public class FrancRealThread extends Thread 
	implements FrancThreadInterface {
	

	public FrancRealThread() {
		this(null);
	}

	public FrancRealThread(Runnable target) {
		super(target);
	}
}
