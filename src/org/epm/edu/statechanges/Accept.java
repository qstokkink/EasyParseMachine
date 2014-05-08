package org.epm.edu.statechanges;

/**
 * Finalize our node, change state, we have not consumed
 */
public class Accept implements IStateChange{

	private final String content;
	
	public Accept(){
		this.content = null;
	}
	
	public Accept(String content){
		this.content = content;
	}
	
	public String getContent(){
		return content;
	}
	
}
