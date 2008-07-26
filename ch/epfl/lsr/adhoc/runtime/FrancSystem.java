package ch.epfl.lsr.adhoc.runtime;

public abstract class FrancSystem {
	
	private static FrancSystem system;

	public static FrancSystem instance() {
		if(system == null)
			throw new NullPointerException("FRANC System not initialized.");
		else
			return system;
	}

	public FrancSystem() {
		init();
	}

	private void init() {
		if(system != null)
			throw new RuntimeException("FRANC System already initialized.");
		else
			system = this;
	}

	public abstract FrancThreadInterface createPeerThread(Runnable target);

}
