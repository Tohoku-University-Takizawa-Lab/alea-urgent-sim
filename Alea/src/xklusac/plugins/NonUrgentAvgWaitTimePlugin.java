/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xklusac.plugins;

import xklusac.environment.ComplexGridlet;
import xklusac.environment.UrgentGridletUtil;

/**
 *
 * @author agung
 */
public class NonUrgentAvgWaitTimePlugin extends AverageWaitTimePlugin {
    
    @Override
    public void cumulate(ComplexGridlet gridletReceived) {
        if (!UrgentGridletUtil.isUrgent(gridletReceived)) {
            double finish_time = gridletReceived.getFinishTime();
            double cpu_time = gridletReceived.getActualCPUTime();
            double arrival = gridletReceived.getArrival_time();
            double response = Math.max(0.0, (finish_time - arrival));
            waitTime += Math.max(0.0, (response - cpu_time));
        }
    }
    
}