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
public class NonUrgentAvgWaitTimePlugin extends AbstractPlugin implements Plugin {
    
	private double waitTime;
	private int numNonUrgentJobs;
	
	public NonUrgentAvgWaitTimePlugin() {
		this.waitTime = 0.0;
		this.numNonUrgentJobs = 0;
	}
	
    @Override
    public void cumulate(ComplexGridlet gridletReceived) {
        if (!UrgentGridletUtil.isUrgent(gridletReceived)) {
            double finish_time = gridletReceived.getFinishTime();
            double cpu_time = gridletReceived.getActualCPUTime();
            double arrival = gridletReceived.getArrival_time();
            double response = Math.max(0.0, (finish_time - arrival));
            waitTime += Math.max(0.0, (response - cpu_time));
            numNonUrgentJobs++;
        }
    }

	@Override
	public Double calculate(ResultCollector rc, SchedulerData sd) {
		return Math.round((waitTime / numNonUrgentJobs) * 100) / 100.0;
	}
    
}
