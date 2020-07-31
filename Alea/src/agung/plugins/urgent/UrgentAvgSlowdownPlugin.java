/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
public class UrgentAvgSlowdownPlugin extends AbstractPlugin implements Plugin {

	private double slowdown;
	private int numUrgentJobs;
	
	
    public UrgentAvgSlowdownPlugin() {
		super();
		this.slowdown = 0.0;
		this.numUrgentJobs = 0;
	}

	@Override
    public void cumulate(ComplexGridlet gridletReceived) {
        if (UrgentGridletUtil.isUrgent(gridletReceived)) {
            double finish_time = gridletReceived.getFinishTime();
            double cpu_time = gridletReceived.getActualCPUTime();
            double arrival = gridletReceived.getArrival_time();
            double response = Math.max(0.0, (finish_time - arrival));
            slowdown += Math.max(1.0, (response / Math.max(1.0, cpu_time)));
            numUrgentJobs++;
        }
    }
    
    @Override
    public Double calculate(ResultCollector rc, SchedulerData sd) {
        double avgSlowdown = slowdown / numUrgentJobs;
        return avgSlowdown;
    }
    
}
