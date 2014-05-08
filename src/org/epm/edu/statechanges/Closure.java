package org.epm.edu.statechanges;

/**
 * Finalize our node, change state, we have consumed
 */
public class Closure implements IStateChange{

	private final String content;
	
	public Closure(){
		this.content = null;
	}
	
	public Closure(String content){
		this.content = content;
	}
	
	public String getContent(){
		return content;
	}
}
