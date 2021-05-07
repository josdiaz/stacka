package org.tools.stacka.provider;

import java.io.File;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tools.stacka.StackTraceEntry;
import org.tools.stacka.StackTraceLine;
import org.tools.stacka.StackTraceProvider;

public class DefaultStackTraceProvider extends StackTraceProvider {
	private  static final Logger logger = LogManager.getLogger(DefaultStackTraceProvider.class);
	
	private StackTraceEntry stackTraceEntry = null;
	private boolean beginStackEntry = false;
	
	protected void clear() {
		super.clear();
		
		beginStackEntry = false;
	}
	
	@Override
	public void consumeLine(File dataFile, int lineNumber, String line) {
		StackTraceLine stackTraceLine = getStackTraceLine(line);
		
		if (stackTraceLine != null) {
			if (!beginStackEntry) {
				beginStackEntry = true;
				beginStackCount++;
				if (!autoDetect) {
					stackTraceEntry = new StackTraceEntry(dataFile);
					if (logger.isDebugEnabled()) {
						logger.debug("begin stacktrace at line " + lineNumber);
					}
				}
			}
			if (!autoDetect) {
				stackTraceEntry.insertLine(stackTraceLine);
			}
		} else if (beginStackEntry) {
			if (!autoDetect) {
				if (stackTraceEntry.isAllowed()) {
					if (stackTraceEntryList == null) {
						stackTraceEntryList = new ArrayList<>();
					}
					if (logger.isDebugEnabled() && stackTraceEntry.getLines().size() <= 0) {
						logger.debug("end stacktrace at line " + lineNumber);
					}
					stackTraceEntryList.add(stackTraceEntry);
				}
			}
			beginStackEntry = false;
			endStackCount++;
		}
	}
}
