package agung.plugins.logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import agung.extensions.urgent.UrgentGridletUtil;
import xklusac.environment.ComplexGridlet;
import xklusac.environment.ResourceInfo;

public class UrgentJobsLogger implements JobResourceInfoLogger {

	protected FileWriter outputWriter;
	
	@Override
	public void init(Map<String, String> config) {
		String logParentDir = InfoLoggerFactory.getLogPath();
		String filename = (String) config.get("outputFilename");
		File outputFile = new File(logParentDir, filename);
		try {
			outputWriter = new FileWriter(outputFile);
			// Print header
			outputWriter.append("job_id,numPE,length_t,deadline,arrival_t,start_t,finish_t,arrival_late,finish_late,assoc_preempts\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void logResources(double clock, List<ResourceInfo> resourceInfos, long queueSize) {
		// Do nothing

	}

	@Override
	public void logJob(double clock, ComplexGridlet gl) {
		if (UrgentGridletUtil.isUrgent(gl)) {
			double arrival_late = (gl.getExecStartTime() - gl.getArrival_time() + gl.getOriginalLength()) / gl.getOriginalLength();
			double complete_late = (gl.getFinishTime()-gl.getDue_date() + gl.getOriginalLength()) / gl.getOriginalLength();
			
			try {
				outputWriter.append(gl.getGridletID() + "," + gl.getNumPE()
						+ "," + gl.getOriginalLength() + "," + gl.getDue_date()
						+ "," + gl.getArrival_time() + "," + gl.getExecStartTime() 
						+ "," + gl.getFinishTime() + "," + String.format("%.4f", arrival_late) 
						+ "," + String.format("%.4f", complete_late) 
						+ "," + gl.getNumAssocPreempts() + "\n");
				outputWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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
