package agung.plugins.logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import xklusac.environment.ComplexGridlet;
import xklusac.environment.ResourceInfo;

public class ResourceQueueUtilLogger implements JobResourceInfoLogger {
	
	private FileWriter outputWriter;
	
	@Override
	public void init(Map<String, String> config) {
		String logParentDir = InfoLoggerFactory.getLogPath();
		String filename = (String) config.get("outputFilename");
		File outputFile = new File(logParentDir, filename);
		try {
			outputWriter = new FileWriter(outputFile);
			// Print header
			outputWriter.append("hour,numBusyPE,numFreePE,utilization,queueSize\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void logResources(double clock, List<ResourceInfo> infos, long queueSize) {
		long hour = Math.round(clock / 3600);
		
		long numFree = 0;
		//long numTotal = 0;
		long numBusy = 0;
		for (ResourceInfo ri: infos) {
			numFree += ri.getNumFreePE();
			numBusy += ri.getNumBusyPE();
			//numTotal += ri.resource.getNumPE();
			//ri.resource.getNumPE();
		}
		//long numBusy = numTotal - numFree;
		//double utilization = numBusy / (double) numTotal;
		if ((numBusy + numFree) > 0) {		// This is a workaround when GridSim return 0 for both counters
			double utilization = numBusy / (double) (numBusy + numFree);
			
			try {
				outputWriter.append(hour + "," + numBusy + "," + numFree + "," 
						+ String.format("%.2f", utilization) + "," + queueSize + "\n");
				outputWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
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
