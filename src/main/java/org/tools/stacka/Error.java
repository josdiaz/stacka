package org.tools.stacka;

public enum Error {
	
	
	// Config errors
	MISSING_ARG_FILE_OR_DIR(-1000, "missing argument file or directory"),
	INVALID_ARG_FILE_OR_DIR(-1001, "argument {0} is not a file or directory"),
	METHOD_FILTER_PATTERN(-1002, "invalid file pattern {0} {1}"),
	CLASS_FILTER_PATTERN(-1003, "invalid method pattern {0} {1}"),		
	INVALID_ARG_CONFIG_FILE(-1004, "argument {0} is not a valid config file"),
	READING_CONFIG_FILE(-1004, "error reading config file {0} {1}"),
	// Autodetect
	AUTODETECT_FORMAT_READING_FILE(-1100, "error doing autodetect stacktrace format, file ''{0}''"),
	AUTODETECT_INSTANCE_PROVIDER(-1101, "error creating provider instance {0}"),

	// Analyze
	ANALIZE_STACKTRACE_FILE(-1200, "error stacktraces, file ''{0}''"),
	;
	
    private final int code;
    private String message;
    
    Error(final int code, final String message) {
    	this.code = code;
    	this.message = message;
    }
    public int getCode() { return code; }
    public String getMessage() { return message; }
    
}

