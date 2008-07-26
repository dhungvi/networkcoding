package ch.epfl.lsr.adhoc.runtime;

public interface FrancThreadInterface {

	void start();
	public void setDaemon(boolean on);
	public boolean isAlive();
	public void interrupt();
}
