package org.tools.stacka.provider;


import java.io.File;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tools.stacka.StackTraceEntry;
import org.tools.stacka.StackTraceLine;
import org.tools.stacka.StackTraceProvider;

public class TDumpWASProvider extends StackTraceProvider {
	
	private  static final Logger logger = LogManager.getLogger(TDumpWASProvider.class);
	private boolean beginStackEntry = false;
	private StackTraceEntry stackTraceEntry = null;
	
	public TDumpWASProvider() {
		super();
		
		stackLineBeginWith = " at ";
	}
	
	protected void clear() {
		super.clear();
		
		beginStackEntry = false;
	}
	
	public void consumeLine(File dataFile, int lineNumber, String line) {		
		if (line.endsWith("Java callstack:")) {
			beginStackEntry = true;
			if (logger.isDebugEnabled()) {
				logger.debug("Detected begin of the stacktrace at line " + lineNumber);
			}
			beginStackCount++;
			if (!autoDetect) {
				stackTraceEntry = new StackTraceEntry(dataFile);
			}
		} else if (beginStackEntry && line.endsWith("Native callstack:")) {
			if (logger.isDebugEnabled()) {
				logger.debug("Detected end of the stacktrace at line " + lineNumber);
			}
			endStackCount++;
			beginStackEntry = false;			
			if (!autoDetect && stackTraceEntry != null) {
				if (logger.isDebugEnabled()) {
					logger.debug(System.lineSeparator() + stackTraceEntry.getDump());
				}
				if (stackTraceEntry.isAllowed()) {
					if (stackTraceEntryList == null) {
						stackTraceEntryList = new ArrayList<>();
					}
					if (logger.isDebugEnabled()) {
						logger.debug("end stacktrace at line " + lineNumber);
					}
					stackTraceEntryList.add(stackTraceEntry);
				}
				stackTraceEntry = null;
			}
		} else if (!autoDetect && beginStackEntry && stackTraceEntry != null) {
			StackTraceLine stackTraceLine = getStackTraceLine(line);

			if (stackTraceLine != null) {				
				if (logger.isDebugEnabled() && stackTraceEntry.getLines().size() <= 0) {
					logger.debug("begin stacktrace at line " + lineNumber);
				}
				stackTraceEntry.insertLine(stackTraceLine);
			}
		}
	}

}
