/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package agung.algorithms.urgent;

import java.util.Date;

import agung.extensions.urgent.UrgentGridletUtil;

import java.util.Collections;

import xklusac.algorithms.queue_based.SJF;
import xklusac.environment.GridletInfo;
import xklusac.environment.Scheduler;

/**
 * Class UJF<p>(Urgent Job First)
 * Extends SJF (Shortest Job First) algorithm.
 * @author      Agung
 */
public class UJF extends SJF {

    public UJF(Scheduler scheduler) {
        super(scheduler);
    }

    @Override
    public void addNewJob(GridletInfo gi) {
        double runtime1 = new Date().getTime();
        Scheduler.queue.addLast(gi);
        Collections.sort(Scheduler.queue, UrgentGridletUtil.urgencyComparator);
        Scheduler.runtime += (new Date().getTime() - runtime1);
        //System.out.println("New job has been received by SJF");
        /*
        for (int i = 0; i < scheduler.queue.size(); i++)
          System.out.print(Scheduler.queue.get(i).getUrgency() + ",");
        System.out.println();
        */
    }

}
