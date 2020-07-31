/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agung.plugins.urgent;

import xklusac.environment.ComplexGridlet;
import xklusac.environment.ResultCollector;
import xklusac.environment.SchedulerData;
import xklusac.plugins.AbstractPlugin;
import xklusac.plugins.Plugin;

/**
 *
 * @author agung
 */
public class TotalPreemptionPlugin extends AbstractPlugin implements Plugin {

	private int numPreemptions;
	private int numPreemptedJobs;
	
	
    public TotalPreemptionPlugin() {
		super();
		this.numPreemptedJobs = 0;
		this.numPreemptions = 0;
	}

	@Override
    public void cumulate(ComplexGridlet gridletReceived) {
        if (gridletReceived.getNumPreempted() > 0) {
            numPreemptions += gridletReceived.getNumPreempted();
            numPreemptedJobs++;
        }
    }
    
    @Override
    public Double calculate(ResultCollector rc, SchedulerData sd) {
       return (double) numPreemptions;
    }
    
}
