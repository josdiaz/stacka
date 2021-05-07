package org.tools.stacka;

import java.text.MessageFormat;

public class StackAException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6492188353693843795L;
	private Error error;
	
	public StackAException(String msg, Throwable e) {
		super(msg, e);
	}
	
	public StackAException(String msg) {
		super(msg);
	}

	public StackAException(Error error, Throwable e, Object... args) {
		super(args != null && args.length > 0 ?
			MessageFormat.format(error.getMessage(), args) : error.getMessage(), e);
		
		this.error = error;
	}
	
	public StackAException(Error error, Object... args) {
		this(error, null, args);
	}
	
	public Error getError() {
		return error;
	}
}
