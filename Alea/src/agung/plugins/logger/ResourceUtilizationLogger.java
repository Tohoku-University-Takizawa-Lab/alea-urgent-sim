package agung.plugins.logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import xklusac.environment.ComplexGridlet;
import xklusac.environment.ResourceInfo;

public class ResourceUtilizationLogger implements JobResourceInfoLogger {
	
	private FileWriter outputWriter;
	
	@Override
	public void init(Map<String, String> config) {
		String logParentDir = InfoLoggerFactory.getLogPath();
		String filename = (String) config.get("outputFilename");
		File outputFile = new File(logParentDir, filename);
		try {
			outputWriter = new FileWriter(outputFile);
			// Print header
			outputWriter.append("time,resourceId,numBusyPE,numFreePE\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void logResources(double clock, List<ResourceInfo> infos) {
		try {
			for (ResourceInfo ri: infos) {
				long hour = Math.round(clock / 3600);
				outputWriter.append(hour + "," + ri.resource.getResourceID() 
					+ "," + ri.resource.getNumBusyPE()
					+ "," + ri.resource.getNumFreePE() + "\n");
				//System.out.println("[ResourceUtilizationLogger] log " + ri.resource.getResourceName());
			}
			outputWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void logJob(double clock, ComplexGridlet gl) {
		// TODO Auto-generated method stub
		// Do nothing
	}

	@Override
	public void close() {
		try {
			outputWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	

}
