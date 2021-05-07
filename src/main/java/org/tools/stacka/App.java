package org.tools.stacka;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gnu.getopt.Getopt;

/**
 * Stacktrace Analyzer
 *
 */
public class App 
{
	private static final Logger loggerMsg = LogManager.getLogger(Constant.LOGGER_MESSAGES);
	
	private static void loadConfig(String configPath) throws StackAException {
		File configFile = new File(configPath);
		Properties configProp;
		FileInputStream configIn = null;
		
		if (!configFile.exists()) {
			throw new StackAException(Error.INVALID_ARG_CONFIG_FILE, configPath);
		}
		try {
			Config config = Config.getInstance();
			
			configIn = new FileInputStream(configFile);
			configProp = new Properties();
			configProp.load(configIn);
			config.getDataLogList().clear();
			for (Entry<Object, Object> entry : configProp.entrySet()) {
				String name, value;
				
				name = (String)entry.getKey();
				value = (String)entry.getValue();
				if (name.equalsIgnoreCase("methodFilter")) {
					config.setMethodFilter(value);
				} else if (name.equalsIgnoreCase("classFilter")) {
					config.setClassFilter(value);
				} else if (name.equalsIgnoreCase("filePattern")) {
					config.setFilePattern(value);
				} else if (name.startsWith("dataFile")) {
					config.getDataLogList().add(new File(value));			
				} else {
					loggerMsg.warn("config file " + configFile.getName() + " - unknown property " + name + ", ignoring...");
				}
			}
		} catch (FileNotFoundException e) {
			throw new StackAException(Error.INVALID_ARG_CONFIG_FILE, configPath);
		} catch (IOException e) {
			throw new StackAException(Error.READING_CONFIG_FILE, configPath, e.getMessage());
		} finally {
			IOUtils.closeQuietly(configIn);
		}
	}
		
    public static void main( String[] args )
    {
		try {
			Getopt options = new Getopt("stacka", args, "f:m:l:c:");
			Config config = Config.getInstance();
			int option;
			File dataLog = null;
			StackA stackA;
			
			if (new File(Constant.STACKA_CONFIG_FILE).exists()) {
				loadConfig(Constant.STACKA_CONFIG_FILE);
			}
			while ((option = options.getopt()) != -1) {
				switch (option) {
				case 'f':
					config.setFilePattern(options.getOptarg());
					break;
				case 'm':
					config.setMethodFilter(options.getOptarg());
					break;
				case 'l':
					config.setClassFilter(options.getOptarg());
					break;
				case 'c':
					loadConfig(options.getOptarg());
					break;
				}
			}
			if (config.getDataLogList().size() <= 0 && args.length <= options.getOptind()) {
				throw new StackAException(Error.MISSING_ARG_FILE_OR_DIR);
			}
			for (int i = options.getOptind(); i < args.length; i++) {
				dataLog = new File(args[i]);
				if (!dataLog.exists()) {
					throw new StackAException(Error.INVALID_ARG_FILE_OR_DIR, dataLog.getName());
				}
				if (i == options.getOptind()) {
					config.getDataLogList().clear();
				}
				config.getDataLogList().add(dataLog);
			}
			stackA = new StackA();
			stackA.analyze();
		} catch(StackAException e) {
			if (e.getCause() != null) {
				loggerMsg.error(e.getMessage(), e);
			} else {
				loggerMsg.error(e.getMessage());
			}
		}
    }
}
