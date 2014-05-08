package org.epm.edu.statechanges;

import java.util.Arrays;
import java.util.Collection;

/**
 * Ambiguous change state, we have not consumed
 */
public class Guess implements IStateChange{

	private final Collection<String> nextStates;
	
	public Guess(Collection<String> next){
		this.nextStates = next;
	}
	
	public Guess(String... next){
		this.nextStates = Arrays.asList(next);
	}
	
	public Collection<String> getNextStates(){
		return nextStates;
	}
	
}
