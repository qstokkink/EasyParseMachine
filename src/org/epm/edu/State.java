package org.epm.edu;

import org.epm.edu.statechanges.IStateChange;

/**
 * A user made state for the parse machine 
 */
public interface State{

	/**
	 * Receive a character from the feed
	 * 
	 * @param c The read character (or -1 if EOF)
	 * @return The state to change the machine into
	 * 
	 */
	public IStateChange feed(int c);
	
	/**
	 * Get the name of the state
	 * 
	 * @return The name of this state
	 */
	public String getName();
	
	/**
	 * Reset this states internal state for processing
	 * a new set of potential input.
	 */
	public void reset();
	
	/**
	 * Copy this object
	 */
	public State copy();
	
}
