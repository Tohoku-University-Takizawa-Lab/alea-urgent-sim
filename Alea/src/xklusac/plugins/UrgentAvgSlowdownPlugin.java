/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xklusac.plugins;

import xklusac.environment.ComplexGridlet;

/**
 *
 * @author agung
 */
public class UrgentAvgSlowdownPlugin extends AverageSlowdownPlugin {

    @Override
    public void cumulate(ComplexGridlet gridletReceived) {
        if (gridletReceived.getPriority() == 999) {
            double finish_time = gridletReceived.getFinishTime();
            double cpu_time = gridletReceived.getActualCPUTime();
            double arrival = gridletReceived.getArrival_time();
            double response = Math.max(0.0, (finish_time - arrival));
            slowdown += Math.max(1.0, (response / Math.max(1.0, cpu_time)));
        }
    }
    
    
    
}
