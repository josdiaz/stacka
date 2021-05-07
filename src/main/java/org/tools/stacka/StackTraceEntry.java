package org.tools.stacka;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class StackTraceEntry {

	private String dump;
	private int count;
	private List<StackTraceLine> stackTraceLineList = new ArrayList<>();
	private HashSet<File> dataFileList = new HashSet<>();
	
	public StackTraceEntry(File dataFile) {
		dataFileList.add(dataFile);
	}
	
	public String getDump() {
		if (dump == null) {
			StringBuilder dumpBuilder = new StringBuilder();
			
			for (StackTraceLine stackTraceLine: stackTraceLineList) {
				dumpBuilder.append(stackTraceLine.getLine());
				dumpBuilder.append(System.lineSeparator());
			}
			dump = dumpBuilder.toString();
		}
		return dump;
	}

	public void setDump(String dump) {
		this.dump = dump;
	}
	

	public boolean isAllowed() {
		Config config = Config.getInstance();
		
		if (config.getMethodPattern() == null &&
			config.getClassPattern() == null) {
			return true;
		}
		for (StackTraceLine stackTraceLine : stackTraceLineList) {
			if (stackTraceLine.isAllowed()) {
				return true;
			}			
		}
		return false;
	}

	
	public boolean isEqual(StackTraceEntry stackTraceEntry) {
		List<StackTraceLine> stackTraceLine2List = stackTraceEntry.getLines();
		Iterator<StackTraceLine> it, it2;
		
		if (stackTraceLine2List.size() != stackTraceLineList.size()) {
			return false;
		}
		it = stackTraceLineList.iterator();
		it2 = stackTraceLine2List.iterator();
		while(it.hasNext()) {
			StackTraceLine stackTraceLine, stackTraceLine2;
			
			stackTraceLine = it.next();
			stackTraceLine2 = it2.next();
			if (!stackTraceLine.isEqual(stackTraceLine2)) {
				return false;
			}
		}		
		return true;
	}
	
	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
	
	public void insertLine(StackTraceLine stackTraceLine) {
		stackTraceLineList.add(stackTraceLine);
	}
	
	public List<StackTraceLine> getLines() {
		return stackTraceLineList;
	}

	public HashSet<File> getDataFileList() {
		return dataFileList;
	}
}
