package org.epm.edu.statechanges;

public class UnknownStateChangeException extends RuntimeException{

	private static final long serialVersionUID = 1L;

	public UnknownStateChangeException(String msg){
		super(msg);
	}
}
