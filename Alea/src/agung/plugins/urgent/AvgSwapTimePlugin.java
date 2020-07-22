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
 * A plugin for calculating the average swap time of preempted jobs.
 * @author agung
 */
public class AvgSwapTimePlugin extends AbstractPlugin implements Plugin {
    
	private double swapTime;
	private int numPreempted;
	
    @Override
    public void cumulate(ComplexGridlet gridletReceived) {
    	swapTime += gridletReceived.getTotalSwapDelay();
    	numPreempted += gridletReceived.getNumPreempted();
    }

	@Override
	public Double calculate(ResultCollector rc, SchedulerData sd) {
		return Math.round((swapTime / numPreempted) * 100) / 100.0;
	}
    
}
