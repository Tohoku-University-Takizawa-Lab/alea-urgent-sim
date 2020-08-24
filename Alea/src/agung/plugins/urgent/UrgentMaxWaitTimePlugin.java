package agung.plugins.urgent;

import agung.extensions.urgent.UrgentGridletUtil;
import xklusac.environment.ComplexGridlet;
import xklusac.environment.ResultCollector;
import xklusac.environment.SchedulerData;
import xklusac.plugins.AbstractPlugin;
import xklusac.plugins.Plugin;

/**
 *
 * @author agung
 */
public class UrgentMaxWaitTimePlugin extends AbstractPlugin implements Plugin {
    
	private double maxWaitTime;
	//private int numUrgentJobs;
	
	public UrgentMaxWaitTimePlugin() {
		super();
		this.maxWaitTime = 0.0;
		//this.numUrgentJobs = 0;
	}
	
    @Override
    public void cumulate(ComplexGridlet gridletReceived) {
        if (UrgentGridletUtil.isUrgent(gridletReceived)) {
            double finish_time = gridletReceived.getFinishTime();
            double cpu_time = gridletReceived.getActualCPUTime();
            double arrival = gridletReceived.getArrival_time();
            //double execStartTime = gridletReceived.getExecStartTime();
            double response = Math.max(0.0, (finish_time - arrival));
            maxWaitTime = Math.max(maxWaitTime, (response - cpu_time));
            //numUrgentJobs++;
        }
    }
	
	@Override
    public Double calculate(ResultCollector rc, SchedulerData sd) {
		return Math.round(maxWaitTime * 100) / 100.0;
    }
    
}
