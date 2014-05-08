package org.epm.edu.statechanges;

import java.util.Arrays;
import java.util.Collection;

/**
 * Ambiguous change state, we have consumed
 */
public class Split implements IStateChange{

	private final Collection<String> nextStates;
	
	public Split(Collection<String> next){
		this.nextStates = next;
	}
	
	public Split(String... next){
		this.nextStates = Arrays.asList(next);
	}
	
	public Collection<String> getNextStates(){
		return nextStates;
	}
	
}
