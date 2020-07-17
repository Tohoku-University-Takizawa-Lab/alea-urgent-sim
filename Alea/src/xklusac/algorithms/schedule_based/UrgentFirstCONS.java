/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xklusac.algorithms.schedule_based;

import java.util.Date;
import gridsim.GridSim;
import java.util.ArrayList;
import java.util.Collections;
import xklusac.environment.ExperimentSetup;
import xklusac.environment.GridletInfo;
import xklusac.environment.ResourceInfo;
import xklusac.environment.Scheduler;
import xklusac.environment.UrgentGridletUtil;
import xklusac.extensions.EndTimeComparator;
import xklusac.extensions.SchedulingEvent;

/**
 * Class UrgentFirstCONS<p>
 * Implements CONS (Conservative Backfilling).
 *
 * @author Agung
 */
public class UrgentFirstCONS extends CONS {

    public UrgentFirstCONS(Scheduler scheduler) {
        super(scheduler);
    }

    @Override
    public void addNewJob(GridletInfo gi) {
        int index = 0;
        int resIndex = -2;
        int gIndex = -1;
        double current_time = GridSim.clock();
        double runtime1 = new Date().getTime();
        double best_start_time = Double.MAX_VALUE;
        boolean accept = true;
        boolean ok = false;
        boolean okh = false;

        // select schedule with earliest suitable gap
        for (int i = 0; i < Scheduler.resourceInfoList.size(); i++) {
            ResourceInfo ri = (ResourceInfo) Scheduler.resourceInfoList.get(i);

            boolean evaluate = false;

            // continue when not suitable.
            if (!Scheduler.isSuitableThenUpdate(ri, gi, current_time)) {
                continue;
            }
            ok = true;
            // if the gap is found - use it
            if (ri.findHoleForGridlet(gi)) {
                okh = true;
                index = ri.resSchedule.indexOf(gi);
                evaluate = true;

                // exists previous assignement
                if (resIndex >= 0) {
                    ResourceInfo rPrev = (ResourceInfo) Scheduler.resourceInfoList.get(resIndex);
                    boolean odstr = rPrev.removeGInfo(gi);
                }
            } else {
                evaluate = false;
            }
            // if the move was made - evaluate this solution
            if (evaluate) {

                Scheduler.updateResourceInfos(current_time);
                double start_time_new = gi.getExpectedStartTime();

                // test the new assignement
                if (start_time_new >= best_start_time && accept == false) {
                    //bad move
                    ri.removeGInfo(gi);
                    ResourceInfo rPrev = (ResourceInfo) Scheduler.resourceInfoList.get(resIndex);
                    rPrev.addGInfo(gIndex, gi);

                } else {
                    // good move
                    accept = false;
                    best_start_time = start_time_new;
                    resIndex = i;
                    gIndex = index;
                    gi.setResourceID(ri.resource.getResourceID());
                }
            }
        }
        Scheduler.runtime += (new Date().getTime() - runtime1);
        if (!Scheduler.isExecutable(gi)) {
            System.out.println(gi.getID() + " is not executable - danger!!! ok=" + ok + " hole=" + okh);
        }
        ResourceInfo ri = (ResourceInfo) Scheduler.resourceInfoList.get(resIndex);
        
        int gi_index = ri.resSchedule.indexOf(gi);
        if (UrgentGridletUtil.isUrgent(gi) && gi_index > 0) {
              // Put urgent jobs to the head of queue
              ri.removeGInfo(gi);
              // Put later than existing urgent jobs
              int actual_idx = 0;
              while (UrgentGridletUtil.isUrgent(ri.resSchedule.get(actual_idx))) {
                  actual_idx++;
              }
              ri.addGInfo(actual_idx, gi);
              System.out.format("- Moved urgent job %d to slot %d of resource %d:%s.\n",
                      gi.getID(), actual_idx, ri.resource.getResourceID(), ri.resource.getResourceName());
        }
        else {
            // mark job as backfilled if it is not at the end of schedule
            //int gi_index = ri.resSchedule.indexOf(gi);
            if (gi_index != (ri.resSchedule.size() - 1)) {
                ExperimentSetup.backfilled++;
            }
        }
        // updates resource info's internal values (IMPORTANT! because of next use of this policy)
        ri.forceUpdate(GridSim.clock());

        // different backfill computation
        double g_start = gi.getExpectedFinishTime();
        for (int b = 0; b < ri.resSchedule.size(); b++) {
            GridletInfo gs = ri.resSchedule.get(b);
            if (gs.getExpectedStartTime() > g_start) {
                ExperimentSetup.backfilled_cons++;
                break;
            }
        }

        //System.out.println(gi.getID()+": New job has been received by CONS");
        if (ExperimentSetup.visualize_schedule) {
            anim = ExperimentSetup.schedule_windows.get(0);
            ArrayList[] schedules = new ArrayList[Scheduler.resourceInfoList.size()];
            int cpu_shift = 0;
            for (int i = 0; i < Scheduler.resourceInfoList.size(); i++) {
                ResourceInfo r = (ResourceInfo) Scheduler.resourceInfoList.get(i);
                
                ArrayList<SchedulingEvent> job_schedule = new ArrayList();
                for (int s = 0; s < r.resSchedule.size(); s++) {
                    GridletInfo ginf = r.resSchedule.get(s);
                    SchedulingEvent job_start = new SchedulingEvent(Math.round(ginf.getExpectedStartTime()), cpu_shift, ginf, true);
                    job_schedule.add(job_start);
                    job_schedule.add(new SchedulingEvent(Math.round(ginf.getExpectedFinishTime()), cpu_shift, ginf, false, job_start));
                }
                //sort all scheduling events by their time
                Collections.sort(job_schedule, new EndTimeComparator());
                schedules[i] = job_schedule;
                
                cpu_shift += r.getNumRunningPE();
            }
            anim.reDrawSchedule(schedules, Scheduler.resourceInfoList.size(), scheduler.cl_names, scheduler.cl_CPUs);
            try {
                Thread.sleep(ExperimentSetup.schedule_repaint_delay);
            } catch (InterruptedException e) {
            }

        }
    }

}
