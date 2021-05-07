package org.tools.stacka;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tools.stacka.provider.DefaultStackTraceProvider;
import org.tools.stacka.provider.TDumpWASProvider;

public class StackA {
	
	public static final String DEFAULT_LOG_FILE_PATTERN = "^.*$";
	
	private static List<Class<?>> stackTraceProviderList;
	private  static final Logger logger = LogManager.getLogger(StackA.class);
	private  static final Logger loggerMsg = LogManager.getLogger(Constant.LOGGER_MESSAGES);
	static {
		stackTraceProviderList = new ArrayList<Class<?>>();
		stackTraceProviderList.add(TDumpWASProvider.class);
		stackTraceProviderList.add(DefaultStackTraceProvider.class);
	}
	
	private StackTraceProvider autoDetectFormat(File dataFile) throws StackAException {
		
		BufferedReader reader = null;
		
		try  {			
			String line;
			List<StackTraceProvider> providerList = new ArrayList<StackTraceProvider>();
			StackTraceProvider providerDetected = null;
			reader = new BufferedReader(new FileReader(dataFile));
			
			for (Class<?> provider_ : stackTraceProviderList) {
				StackTraceProvider provider = (StackTraceProvider) provider_.newInstance();

				provider.initAutoDetect();
				providerList.add(provider);
			}

			for (int lineNumber = 1; (line = reader.readLine()) != null; lineNumber++) {
				for (StackTraceProvider provider : providerList) {
					provider.consumeLine(dataFile, lineNumber, line);
				}
			}
			for (StackTraceProvider provider : providerList) {
				if (provider.isProvided()) {
					providerDetected = provider;
					providerDetected.clear();
					break;
				}
				provider.clear();
			}		
			return providerDetected;

		} catch(IOException e) {
			throw new StackAException(Error.AUTODETECT_FORMAT_READING_FILE, e, dataFile);
		} catch (Exception e) {
			throw new StackAException(Error.AUTODETECT_INSTANCE_PROVIDER, e);
		} finally {
			if (reader != null) {
				IOUtils.closeQuietly(reader);
			}
		}
	}
	
	private List<StackTraceEntry> analyzeFileWithProvider(File dataFile, StackTraceProvider provider) throws StackAException {
		BufferedReader reader = null;
		
		try  {			
			String line;
			reader = new BufferedReader(new FileReader(dataFile));
			
			provider.initStackTrace();
			for (int lineNumber = 1; (line = reader.readLine()) != null; lineNumber++) {
				provider.consumeLine(dataFile, lineNumber, line);
			}
			return provider.getStackTraces();
		} catch(IOException e) {
			throw new StackAException(Error.ANALIZE_STACKTRACE_FILE, e, dataFile);
		} finally {
			if (reader != null) {
				IOUtils.closeQuietly(reader);
			}
		}
	}

	private List<StackTraceEntry> analyzeFile(File dataFile) throws StackAException {
		StackTraceProvider provider = autoDetectFormat(dataFile);	
		
		if (provider == null) {
			loggerMsg.info("no provider was found it, ignoring file " + dataFile);
			return null;
		}
		if (logger.isDebugEnabled()) {
			logger.debug("provider was found " + provider.getClass().getName() + ", " + dataFile);
		}
		return analyzeFileWithProvider(dataFile, provider);
	}
	
	private List<StackTraceEntry> analyzeDir(File dataLogDir) throws StackAException {
		List<StackTraceEntry> stackTraceEntryList = new ArrayList<>();
		String[] fileList;
		Config config = Config.getInstance();

		Pattern pattern = Pattern
				.compile(config.getFilePattern() != null ? config.getFilePattern() : DEFAULT_LOG_FILE_PATTERN);

		fileList = dataLogDir.list((dir, name) -> pattern.matcher(name).matches());
		for(String file_ : fileList) {
			File file = new File(dataLogDir, file_);
			List<StackTraceEntry> stackTraceEntryList_;

			if (!file.isFile()) {
				continue;
			}
			loggerMsg.info("analizying file " + file.getName());

			stackTraceEntryList_ = analyzeFile(file);
			if (stackTraceEntryList_ != null) {
				stackTraceEntryList.addAll(stackTraceEntryList_);
			}
		}
		return stackTraceEntryList;
	}
	
	private StackStadistic fillStadistics(List<StackTraceEntry> stackTraceList) {
		StackStadistic stadistics = new StackStadistic();

		for (StackTraceEntry stackTraceEntry : stackTraceList) {
			stadistics.visitStackTrace(stackTraceEntry);
		}
		return stadistics;
	}
	
	private void mergeStacks(List<StackTraceEntry> stackTraceList) {
		for (int i = 0; i < stackTraceList.size(); i++) {
			StackTraceEntry stackTraceEntry = stackTraceList.get(i);
			int n = i + 1;
			int count = 1;
			
			while (n < stackTraceList.size()) {
				StackTraceEntry stackTraceEntry2 = stackTraceList.get(n);
				
				if (stackTraceEntry.isEqual(stackTraceEntry2)) {
					stackTraceEntry.getDataFileList().addAll(stackTraceEntry2.getDataFileList());
					stackTraceList.remove(n);
					count++;
				} else {
					n++;
				}
			}
			stackTraceEntry.setCount(count);
		}
		
		Collections.sort(stackTraceList, (stackentry1, stackentry2) ->
		        		 stackentry1.getCount() > stackentry2.getCount() ? -1 : 
		        			 (stackentry1.getCount()  < stackentry2.getCount() ) ? 1 : 0		    
		);
		
	}
	/*
	 *  TODO: make diff stacktraces compare
	private void diffStacks(List<StackTraceEntry> stackTraceList) {
		
		Collections.sort(stackTraceList, new Comparator<StackTraceEntry>() {
		    
		    @Override
			public int compare(StackTraceEntry stackentry1, StackTraceEntry stackentry2) {
		    	Patch<String> diff = DiffUtils.diff(Arrays.asList(stackentry1.getDump().split(System.lineSeparator())),
		    										 Arrays.asList(stackentry2.getDump().split(System.lineSeparator())));
		        if (diff.getDeltas().size() <= 5) {
		        	return 0;
		        }
		        if (diff.getDeltas().size() > 5) {
		        	return -1;
		        }
		        return 0;
		    }
		});
	}
	*/
	private void dumpStadistics(List<StackTraceEntry> stackTraceList) {
		StackStadistic stadistics = fillStadistics(stackTraceList);
		List<Entry<String, Integer>> lineCounterList = stadistics.getLines();
		
		if (lineCounterList.size() <= 0) {
			return;
		}
		loggerMsg.info("=======================================================================");
		loggerMsg.info("= list classname/method or line repeated in the stacktraces");
		loggerMsg.info("=======================================================================");
		loggerMsg.info("= count pattern");
		loggerMsg.info("=======================================================================");
		for(Entry<String, Integer> lineCount : lineCounterList) {
			loggerMsg.info(MessageFormat.format("{0,number,integer} {1}", lineCount.getValue(), lineCount.getKey()));
		}
		loggerMsg.info("=======================================================================" +
						System.lineSeparator());		
	}
	
	private void dumpStackTraces(List<StackTraceEntry> stackTraceList) {
		loggerMsg.info("=======================================================================");
		loggerMsg.info("= stacktraces count = " + stackTraceList.size());
		mergeStacks(stackTraceList);		
		loggerMsg.info("= stacktraces (merged) count = " + stackTraceList.size());
		loggerMsg.info("= showing stacktraces was merged...");
		loggerMsg.info("=======================================================================");

		for (StackTraceEntry stackTraceEntry : stackTraceList) {
			HashSet<File> dataFileList = stackTraceEntry.getDataFileList();
			StringBuilder stackTraceInfo = new StringBuilder();
			Iterator<File> dataFileEntry = dataFileList.iterator();
			
			stackTraceInfo.append("[");
			for (int i = 0; i < dataFileList.size(); i++) {		
				stackTraceInfo.append(dataFileEntry.next().getName());
				if (i < dataFileList.size() - 1) {
					stackTraceInfo.append(" | ");
				}
			}
			stackTraceInfo.append("] matches ");
			stackTraceInfo.append(stackTraceEntry.getCount());
			stackTraceInfo.append(System.lineSeparator());
			stackTraceInfo.append(stackTraceEntry.getDump());
			loggerMsg.info(stackTraceInfo.toString());
		}
	}
	
	private void doAnalyze(List<StackTraceEntry> stackTraceList) {
		dumpStadistics(stackTraceList);
		dumpStackTraces(stackTraceList);	
	}
	
	public void analyze() throws StackAException {
		ArrayList<StackTraceEntry> stackTraceList = new ArrayList<>();
		Config config = Config.getInstance();
		List<File> dataLogList = config.getDataLogList();
		
		for (File dataLog : dataLogList) {
			List<StackTraceEntry> stackTraceDataLog;
			
			if (dataLog.isDirectory()) {
				stackTraceDataLog = analyzeDir(dataLog);
			} else {
				stackTraceDataLog = analyzeFile(dataLog);
			}
			if (stackTraceDataLog != null) {
				stackTraceList.addAll(stackTraceDataLog);
			}
		}
		if (stackTraceList.size() > 0) {
			doAnalyze(stackTraceList);
		} else {
			loggerMsg.info("not stacktraces was found it....exiting");
		}
	}
}
