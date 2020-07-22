package agung.plugins.logger;

import java.io.File;

import xklusac.environment.FileUtil;

/**
 * Class LoggerFactory is used for creating Logger instances.
 * 
 * @author Mulya Agung
 */
public class InfoLoggerFactory {
	
	public static final String LOG_SUBDIR = "logger_outputs";
	
    /**
     * Create a Logger instance by using reflection,
     *
     * @param className
     *            the class name; it must be a valid class name on the classpath;
     *            the class must implement the JobResourceInfoLogger interface
     *
     * @return the instance of the logger, but not initialized yet
     *
     * @throws RuntimeException
     *             if the logger could not be properly created
     */
    public static JobResourceInfoLogger createLogger(String className) {
        try {
            // Make the instance of the plugin
            final Class<?> pluginClass = Class.forName(className);
            final Object result = pluginClass.newInstance();

            JobResourceInfoLogger logger = (JobResourceInfoLogger) result;
            return logger;
        } catch (Exception e) {
        	throw new RuntimeException("Could not create logger: " + className, e);
		}
    }
    
    public static String getLogPath() {
    	String logParentDir = System.getProperty("user.dir") + File.separator
				+ InfoLoggerFactory.LOG_SUBDIR; 
    	logParentDir = FileUtil.getPath(logParentDir);
    	File file = new File(logParentDir);
    	if (!file.exists())
    		file.mkdir();
    	return logParentDir;
    }
}
