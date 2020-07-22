package agung.plugins.logger;

import java.util.List;
import java.util.Map;

import xklusac.environment.ComplexGridlet;
import xklusac.environment.ResourceInfo;

public interface JobResourceInfoLogger {
	void init(Map<String, String> config);
	void logResources(double clock, List<ResourceInfo> resourceInfos);
	void logJob(double clock, ComplexGridlet complexGridlet);
	void close();
}
