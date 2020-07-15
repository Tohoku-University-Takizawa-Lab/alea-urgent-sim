/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xklusac.algorithms.queue_based;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import xklusac.environment.GridletInfo;
import xklusac.environment.ResourceInfo;
import xklusac.environment.Scheduler;
import xklusac.environment.UrgentGridletUtil;

/**
 * Class PreemptiveUJFF<p>
 * Implements UJF (Urgent Job First) algorithm.
 * @author       Agung
 */
public class PreemptiveUJF extends UJF {

    public PreemptiveUJF(Scheduler scheduler) {
        super(scheduler);
    }

    @Override
    public int selectJob() {
        //System.out.println("Selecting job by SJF...");
        int scheduled = 0;
        ResourceInfo r_cand = null;
        for (int i = 0; i < Scheduler.queue.size(); i++) {
            GridletInfo gi = (GridletInfo) Scheduler.queue.get(i);
            
            ArrayList<ResourceInfo> suitableRes = new ArrayList<>();
            
            for (int j = 0; j < Scheduler.resourceInfoList.size(); j++) {
                ResourceInfo ri = (ResourceInfo) Scheduler.resourceInfoList.get(j);

                if (Scheduler.isSuitable(ri, gi)) {
                    suitableRes.add(ri);
                    
                    if (ri.canExecuteNow(gi)) {
                        r_cand = ri;
                        break;
                    }
                }
            }
            if (r_cand != null) {
                gi = (GridletInfo) Scheduler.queue.remove(i);
                //System.err.println(gi.getID()+" PEs size = "+gi.PEs.size());
                r_cand.addGInfoInExec(gi);
                // set the resource ID for this gridletInfo (this is the final scheduling decision)
                gi.setResourceID(r_cand.resource.getResourceID());
                scheduler.submitJob(gi.getGridlet(), r_cand.resource.getResourceID());
                r_cand.is_ready = true;
                //scheduler.sim_schedule(GridSim.getEntityId("Alea_3.0_scheduler"), 0.0, AleaSimTags.GRIDLET_SENT, gi);
                scheduled++;
                r_cand = null;
                i--;
                return scheduled;
            } else {
                // Cannot find free resources
                
                // Preempt regular jobs
                if (UrgentGridletUtil.isUrgent(gi)) {
                    // Select machines with most idle PEs.
                    Collections.sort(suitableRes, UrgentGridletUtil.numFreePEComparator);
                    
                    ResourceInfo targetRi = suitableRes.get(0);
                    //List<GridletInfo> schedInfos = targetRi.resSchedule;
                    List<GridletInfo> runInfos = targetRi.resInExec;
                    
                    // Prioritize jobs with longer time-to-finish to be preempted 
                    Collections.sort(runInfos, UrgentGridletUtil.finishSoFarComparator);
                    
                    int toFree = gi.getNumPE();
                    int visit = 0;
                    while (toFree > 0 && visit < runInfos.size()) {
                        GridletInfo info = runInfos.get(visit);
                        
                        //targetRi.resInExec.remove(info);
                        targetRi.lowerResInExec(info);
                        scheduler.cancelJob(info.getGridlet(), targetRi.resource.getResourceID(), 0);
                        
                        // Resubmit to scheduling queue
                        targetRi.addLastGInfo(info);
                        
                        toFree -= targetRi.getNumFreePE();
                        visit++;
                        System.out.format("- Job %s is preempted, toFree=%d, candidates=%d, urgent_job=%d\n",
                                info.getID(), toFree, runInfos.size(), gi.getID());
                    }
                    // Now urgent job is ready to submit
                    targetRi.addGInfoInExec(gi);
                    gi.setResourceID(targetRi.resource.getResourceID());
                    scheduler.submitJob(gi.getGridlet(), gi.getResourceID());
                    
                    targetRi.is_ready = true;
                    scheduled++;
                }
                return scheduled;
            }
        }

        return scheduled;
    }
    
}
