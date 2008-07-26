package ch.epfl.lsr.adhoc.real;

import ch.epfl.lsr.adhoc.runtime.FrancSystem;
import ch.epfl.lsr.adhoc.runtime.FrancThreadInterface;

public class FrancRealSystem extends FrancSystem {

	public FrancThreadInterface createPeerThread(Runnable target) {
		return new FrancRealThread(target);
	}
	
}
