package org.tools.stacka;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

public class StackStadistic {
	private Hashtable<String, Integer> lineCounter = new Hashtable<String, Integer>();
	
	private String getLineId(StackTraceLine stackTraceLine) {
		if (stackTraceLine.getClassName() != null && stackTraceLine.getMethodName() != null) {
			return stackTraceLine.getClassName() + "." + stackTraceLine.getMethodName();
		}
		return stackTraceLine.getLine();
	}
	public void visitStackTrace(StackTraceEntry stackTraceEntry) {
		List<StackTraceLine> stackTraceLineList = stackTraceEntry.getLines();
		HashSet<String> linesVisited = new HashSet<>();
		
		for (StackTraceLine stackTraceLine : stackTraceLineList) {				
			String lineId;
			Integer value;

			if (!stackTraceLine.isAllowed()) {
				continue;
			}			
			lineId = getLineId(stackTraceLine);
			if (linesVisited.contains(lineId)) {
				continue;
			}
			value = lineCounter.get(lineId);
			if (value != null) {
				lineCounter.put(lineId, value.intValue() + 1);
			} else {
				lineCounter.put(lineId, 1);
			}
			linesVisited.add(lineId);
		}
	}
	
	 public List<Entry<String, Integer>> getLines() {
			List<Entry<String, Integer>> list = new LinkedList<>(lineCounter.entrySet());

			list.sort((line1, line2) -> line1.getValue() > line2.getValue() ? -1
					: line1.getValue() < line2.getValue() ? 1 : 0);

			return list;
	 }
}
