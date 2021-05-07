package org.tools.stacka;

public class StackTraceLine {
	
	public String getLine() {
		return line;
	}

	public void setLine(String line) {
		this.line = line;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	public String getMethodName() {
		return method;
	}

	public void setMethodName(String method) {
		this.method = method;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = normalizeClassName(className);
	}

	public String getSourceFile() {
		return sourceFile;
	}

	public void setSourceFile(String sourceFile) {
		this.sourceFile = sourceFile;
	}
	
	private String normalizeClassName(String className) {
		return className.replace("/", ".");
	}
	
	public boolean isEqual(StackTraceLine stackTraceLine) {
		return method.equals(stackTraceLine.getMethodName()) &&
			   className.equals(stackTraceLine.getClassName()) &&
			   sourceFile.equals(stackTraceLine.getSourceFile()) &&
			   lineNumber == stackTraceLine.getLineNumber();			  		
	}
	
	public boolean isAllowed() {
		Config config = Config.getInstance();
		boolean methodMatched = true, classMatched = true;
		
		if (config.getMethodPattern() != null) {
			methodMatched = config.getMethodPattern().matcher(method).matches();
		}
		if (config.getClassPattern() != null) {
			classMatched = config.getClassPattern().matcher(className).matches();				
		}
		return classMatched && methodMatched;
	}

	private String line = "";
	private int lineNumber = -1;
	private String method = "";
	private String className = "";
	private String sourceFile = "";
}
