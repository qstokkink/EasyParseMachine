package org.epm.edu.statechanges;

/**
 * Change state, we have not consumed
 */
public class Goto implements IStateChange{

	private final String nextState;
	
	public Goto(String next){
		this.nextState = next;
	}
	
	public String getNextState(){
		return nextState;
	}
	
}
