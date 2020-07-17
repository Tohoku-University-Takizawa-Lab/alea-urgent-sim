/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xklusac.algorithms.queue_based;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import xklusac.environment.GridletInfo;
import xklusac.environment.JobSwapper;
import xklusac.environment.ResourceInfo;
import xklusac.environment.Scheduler;
import xklusac.environment.UrgentGridletUtil;

/**
 * Class PreemptiveUJFF<p>
 * Implements UJF (Urgent Job First) algorithm.
 * @author       Agung
 */
public class PreemptiveUJF extends UJF {

	private JobSwapper jobSwapper;
	
    public PreemptiveUJF(Scheduler scheduler, JobSwapper jobSwapper) {
        super(scheduler);
        this.jobSwapper = jobSwapper;
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
                
                // Add swapping in delay
                if (gi.isSuspended()) {
                	jobSwapper.swapin(gi, r_cand);
                }
                else {
                	scheduler.submitJob(gi.getGridlet(), r_cand.resource.getResourceID());
                }
                
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
                    
                    int toFree = gi.getNumPE() - targetRi.getNumFreePE();
                    int visit = 0;
                    while (toFree > 0 && visit < runInfos.size()) {
                        GridletInfo info = runInfos.get(visit);
                        
                        //System.out.format("- Preempting job %s (Mem=%d) for urgent_job=%d (PE:%d), toFree=%d\n",
                        //       info.getID(), info.getRam(), gi.getID(), gi.getNumPE(), toFree);
                        System.out.format("- Preempting job %s (PE=%d,Mem=%d) for urgent_job=%d (PE:%d), toFree=%d\n",
                                info.getID(), info.getNumPE(), info.getRam(), gi.getID(), gi.getNumPE(), toFree);
                        
                        //targetRi.resInExec.remove(info);
                        //targetRi.lowerResInExec(info);
                        //scheduler.cancelJob(info.getGridlet(), targetRi.resource.getResourceID(), 0);
                        //jobSwapper.swapout(info, targetRi);
                        
                        boolean swapped = jobSwapper.swapout(info, targetRi);
                        
                        if (swapped) {
                        	targetRi.removeGInfo(info);
	                        // Put the preempted job into the earliest queue of regular jobs
	                        int actual_idx = 0;
	                        while (UrgentGridletUtil.isUrgent(targetRi.resSchedule.get(actual_idx))) {
	                            actual_idx++;
	                        }
	                        targetRi.addGInfo(actual_idx, info);
	                        System.out.format("- Put back preempted job %d to slot %d of resource %d:%s.\n",
	                                info.getID(), actual_idx, targetRi.resource.getResourceID(), targetRi.resource.getResourceName());
                        }
                        
                        // Resubmit to the head of queue
                        //targetRi.addLastGInfo(info);
                        //targetRi.addGInfo(0, info);
                        
                        toFree -= info.getNumPE();
                        visit++;
                        
                    }
                    // Now urgent job is ready to submit
                    //targetRi.addGInfoInExec(gi);
                    //gi.setResourceID(targetRi.resource.getResourceID());
                    //scheduler.submitJob(gi.getGridlet(), gi.getResourceID());
                    
                    //targetRi.is_ready = true;
                    //scheduled++;
                }
                return scheduled;
            }
        }

        return scheduled;
    }
    
}
