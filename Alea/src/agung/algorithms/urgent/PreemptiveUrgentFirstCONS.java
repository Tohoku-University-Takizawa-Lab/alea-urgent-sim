/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package agung.algorithms.urgent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import agung.extensions.urgent.JobSwapper;
import agung.extensions.urgent.JobSwapper.Result;
import agung.extensions.urgent.UrgentGridletUtil;
import xklusac.environment.GridletInfo;
import xklusac.environment.ResourceInfo;
import xklusac.environment.Scheduler;

/**
 * Class PreemptiveUrgentFirstCONS<p>
 * UrgentFirstJob with backfilling and preemption
 * Implements UrgentFirstCONS.
 *
 * @author Agung
 */
public class PreemptiveUrgentFirstCONS extends UrgentFirstCONS {

	private JobSwapper jobSwapper;
	
    public PreemptiveUrgentFirstCONS(Scheduler scheduler, JobSwapper jobSwapper) {
        super(scheduler);
        this.jobSwapper = jobSwapper;
    }

    @Override
    public int selectJob() {
        int scheduled = 0;
        for (int j = 0; j < Scheduler.resourceInfoList.size(); j++) {
            ResourceInfo ri = (ResourceInfo) Scheduler.resourceInfoList.get(j);
            if (ri.resSchedule.size() > 0) {
                GridletInfo gi = (GridletInfo) ri.resSchedule.get(0);
                if (ri.canExecuteNow(gi)) {
                    ri.removeFirstGI();
                    ri.addGInfoInExec(gi);

                    // set the resource ID for this gridletInfo (this is the final scheduling decision)
                    gi.setResourceID(ri.resource.getResourceID());
                    // tell the user where to send which gridlet

                    //scheduler.submitJob(gi.getGridlet(), ri.resource.getResourceID());
                    // Add swapping in delay
                    if (gi.isSuspended()) {
                    	jobSwapper.swapin(gi, ri);
                    }
                    else {
                    	//scheduler.submitJob(gi.getGridlet(), r_cand.resource.getResourceID());
                    	scheduler.submitJobWithDelay(gi.getGridlet(), ri.resource.getResourceID(), gi.getSubmissionDelay());
                    }

                    ri.is_ready = true;
                    //scheduler.sim_schedule(GridSim.getEntityId("Alea_3.0_scheduler"), 0.0, AleaSimTags.GRIDLET_SENT, gi);
                    scheduled++;
                    return scheduled;
                }
                else if (UrgentGridletUtil.isUrgent(gi)) {
                    // Assumed to be suitable because it is verified when it is added
                    
                    // Preempt regular jobs
                    List<GridletInfo> runInfos = ri.resInExec;

                    // Prioritize jobs with longer time-to-finish to be preempted 
                    Collections.sort(runInfos, UrgentGridletUtil.finishSoFarComparator);

                    ArrayList<Result> swapResults = new ArrayList<Result>();
                    int toFree = gi.getNumPE() - ri.getNumFreePE();
                    int visit = 0;
                    while (toFree > 0 && visit < runInfos.size()) {
                        GridletInfo info = runInfos.get(visit);

                        // Preempt only regular jobs
                        if (!UrgentGridletUtil.isUrgent(info)) {
	                        System.out.format("- Preempting job %s (PE=%d,Mem=%d) for urgent_job=%d (PE:%d), toFree=%d\n",
	                                info.getID(), info.getNumPE(), info.getRam(), gi.getID(), gi.getNumPE(), toFree);
	
	                        //boolean swapped = jobSwapper.swapout(info, ri);
	                        Result swapResult = jobSwapper.swapout(info, ri);
	                        
	                        if (swapResult.isSuccess()) {
	                        	ri.lowerResInExec(info);
	                        	ri.removeGInfo(info);
		                        // Put the preempted job into the earliest queue of regular jobs
		                        int actual_idx = 0;
		                        if (ri.resSchedule.size() > 0) {
			                        while (actual_idx < ri.resSchedule.size() 
			                        		&& UrgentGridletUtil.isUrgent(ri.resSchedule.get(actual_idx))) {
			                            actual_idx++;
			                        }
		                        }
		                        ri.addGInfo(actual_idx, info);
		                        System.out.format("- Put back preempted job %d to slot %d of resource %d:%s.\n",
		                                info.getID(), actual_idx, ri.resource.getResourceID(), ri.resource.getResourceName());
		                        swapResults.add(swapResult);
	                        }
	
	                        toFree -= info.getNumPE();
                        }
                        visit++;
                     }
                     jobSwapper.delayUrgentJob(gi, swapResults);
                     
                     //Scheduler.updateResourceInfos(GridSim.clock());
                     // Now urgent job is ready to submit
                     //ri.addGInfoInExec(gi);
                     //gi.setResourceID(ri.resource.getResourceID());
                     //scheduler.submitJob(gi.getGridlet(), gi.getResourceID());

                     //ri.is_ready = true;
                     //scheduled++;
                     return scheduled;
                }
            }
        }
        return scheduled;
    }

}
