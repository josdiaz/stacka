package org.tools.stacka;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Config {
	private ArrayList<File> dataLogList = new ArrayList<>();
	private String filePattern = null;
	private Pattern methodPattern = null;
	private Pattern classPattern = null;
	private static Config config = null;
	
	public static Config getInstance() {
		if (config == null) {
			config = new Config();
		}
		return config;
	}
	
	public List<File> getDataLogList() {
		return dataLogList;
	}

	public String getFilePattern() {
		return filePattern;
	}

	public void setFilePattern(String filePattern) {
		this.filePattern = filePattern;
	}

	public Pattern getMethodPattern() {
		
		return methodPattern;
	}

	public void setMethodFilter(String methodFilter) throws StackAException {
		try {
			this.methodPattern = Pattern.compile(methodFilter);
		} catch(PatternSyntaxException e) {
			throw new StackAException(Error.METHOD_FILTER_PATTERN, methodFilter, e.getMessage());			
		}
	}

	public Pattern getClassPattern() {
		return classPattern;
	}

	public void setClassFilter(String classFilter) throws StackAException {
		try {
			this.classPattern = Pattern.compile(classFilter);
		} catch(PatternSyntaxException e) {
			throw new StackAException(Error.CLASS_FILTER_PATTERN, classFilter, e.getMessage());			
		}
	}	
}
