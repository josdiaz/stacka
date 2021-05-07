package org.tools.stacka;

import java.io.File;
import java.util.List;

public abstract class StackTraceProvider {
	protected List<StackTraceEntry> stackTraceEntryList = null;
	protected boolean autoDetect;
	protected int beginStackCount = 0;
	protected int endStackCount = 0;
	protected String stackLineBeginWith = null;

	protected void clear() {
		if (stackTraceEntryList != null) {
			stackTraceEntryList.clear();
			stackTraceEntryList = null;
		}	
		beginStackCount = endStackCount = 0;
	}
	
	public void initAutoDetect() {
		clear();
		autoDetect = true;
	}
	
	public void initStackTrace() {
		clear();
		autoDetect = false;
	}
	
	public abstract void consumeLine(File dataFile, int lineNumber, String line);
	
	public List<StackTraceEntry> getStackTraces() {
		return stackTraceEntryList;
	}
	
	public boolean isProvided() {
		return beginStackCount > 0 && endStackCount > 0 && beginStackCount == endStackCount;
	}
	
	private boolean isClassName(String className) {
		if (className.length() <= 0) {
			return false;
		}		 
		for(int i = 0; i < className.length(); i++) {
			char c = className.charAt(i);
			
			if (!Character.isLetterOrDigit(c) && c != '.' && c != '_'  && c != '$' && c != '/') {
				return false;
			}
		}
		return true;
	}
	
	private boolean isMethodName(String methodName) {
		if (methodName.length() <= 0) {
			return false;
		}		 
		for(int i = 0; i < methodName.length(); i++) {
			char c = methodName.charAt(i);
			
			if (!Character.isLetterOrDigit(c) && c != '.' && c != '_' && c != '$') {
				return false;
			}
		}
		return true;
	}
	
	protected StackTraceLine getStackTraceLine(String line) {
		String tokens[];
		String classMethod, sourceLine;
		StackTraceLine stackTraceLine = new StackTraceLine();		
		int pos, pos2, pos3, pos4;
		String className, methodName;
		
		if (!line.endsWith(")")) {
			return null;
		}
		if (stackLineBeginWith != null) {
			pos = line.indexOf(stackLineBeginWith);
			if (pos < 0) {
				return null;
			}
			line = line.substring(pos + stackLineBeginWith.length());
		} else {
			for (pos = 0; pos < line.length(); pos++) {
				char c = line.charAt(pos);
			
				if (Character.isAlphabetic(c)) {
					break;
				}
			}
			if (pos == 0) {
				return null;
			}
			line = line.substring(pos);
		}
		pos2 = line.indexOf("(");
		if (pos2 <= pos) {
			return null;
		}	
		classMethod = line.substring(0, pos2).trim();
		pos4 = classMethod.lastIndexOf('.');
		if (pos4 <= 0) {
			return null;
		}
		methodName = classMethod.substring(pos4 + 1);
		className = classMethod.substring(0, pos4);
		if (!isClassName(className) || !isMethodName(methodName)) {
			return null;
		}
		stackTraceLine.setClassName(className);
		stackTraceLine.setMethodName(methodName);
		stackTraceLine.setLine(line);		
		
		pos3 = line.indexOf(")", pos2);
		if (pos3 > pos2) {
			sourceLine = line.substring(pos2 + 1, pos3);
			tokens = sourceLine.split(":");
			if (tokens.length == 2) {
				StringBuffer lineNumber = new StringBuffer();

				stackTraceLine.setSourceFile(tokens[0]);
				for (int i = 0; i < tokens[1].length(); i++) {
					char c = tokens[1].charAt(i);

					if (c < '0' || c > '9') {
						break;
					} else {
						lineNumber.append(c);
					}
				}
				stackTraceLine.setLineNumber(lineNumber.length() > 0 ? Integer.parseInt(lineNumber.toString()) : -1);
			}
		}
		return stackTraceLine;
	}
}
