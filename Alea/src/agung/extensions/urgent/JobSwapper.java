package agung.extensions.urgent;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import gridsim.Gridlet;
import xklusac.environment.GridletInfo;
import xklusac.environment.ResourceInfo;
import xklusac.environment.Scheduler;

/**
 * Class for emulating job swapping.
 * @author agung
 *
 */
public class JobSwapper {
	
	public enum Result {
		SUCCESS(true), FAIL(false);
		
		private boolean success;
		private double delay;
		
		Result(boolean success) {
			this.success = success;
			this.delay = 0.0;
		}
		
		public boolean isSuccess() {
			return success;
		}
		
		public void setDelay(double delay) {
			this.delay = delay;
		}
		
		public double getDelay() {
			return delay;
		}
	}
	
	// Time needed for swapping in (in seconds)
	public static final double DEF_SWAPOUT_DELAY = 1;
	public static final double DEF_SWAPIN_DELAY = 1;
	
	private Scheduler scheduler;
	private SwapTimeGen delayGen;
	
	public JobSwapper(Scheduler scheduler, SwapTimeGen gen) {
		this.scheduler = scheduler;
		this.delayGen = gen;
	}
	
	public Result swapout(GridletInfo gi, ResourceInfo ri) {
		double delay = delayGen.genSwapoutTime(gi.getGridlet());
		// Emulating the swapping time with GridSim delay
		// 1. Pause no delay
		// 2. Resume with delay of swap
		// 3. Cancel no delay
		//scheduler.pauseJob(gi.getGridlet(), ri.resource.getResourceID(), 0);
		//scheduler.resumeJob(gi.getGridlet(), ri.resource.getResourceID(), delay);
		gi.setSuspended(true);
		//gi.getGridlet().setSuspended(true);
		
		scheduler.cancelJob(gi.getGridlet(), ri.resource.getResourceID(), delay);
		
		Result res = (gi.getGridlet().getGridletStatus() == Gridlet.CANCELED ? 
				Result.SUCCESS : Result.FAIL);
		//boolean swapped = (gi.getGridlet().getGridletStatus() == Gridlet.CANCELED);
		// Put the preempted job on the head of queue if it is canceled successfully
		if (res.isSuccess()) {
			res.setDelay(delay);
			//ri.removeGInfo(gi);
			//ri.addGInfo(0, gi); // Handle by the scheduling algorithm
			
			gi.getGridlet().addTotalSwapDelay(delay);
			gi.getGridlet().addNumPreempted(1);
			
			// Update the job length
			// Subtract the previous job runtime from the original job length
			gi.getGridlet().addTotalCPUTime(gi.getGridlet().getActualCPUTime());
			gi.getGridlet().setGridletLength(gi.getGridlet().getGridletLength() - 
					gi.getGridlet().getGridletFinishedSoFar());
			gi.getGridlet().setEstimatedLength(gi.getGridlet().getEstimatedLength() - 
					gi.getGridlet().getGridletFinishedSoFar());
			gi.getGridlet().setGridletFinishedSoFar(0.0);
			
			System.out.println("[JobSwapper] swapped out job-" + gi.getID() 
					+ ", RAM=" + gi.getRam() + ", PE=" + gi.getNumPE() 
					+ ", numNodes=" + gi.getNumNodes() + ", delay=" + delay);
		}
		else {
			// Finished before it is canceled.
			gi.setSuspended(false);
		}
		return res;
	}
	
	public double swapin(GridletInfo gi, ResourceInfo ri) {
		double delay = delayGen.genSwapinTime(gi.getGridlet());
		gi.setSuspended(false);
		scheduler.submitJobWithDelay(gi.getGridlet(), ri.resource.getResourceID(), delay);
		gi.getGridlet().addTotalSwapDelay(delay);
		return delay;
	}
	
	public void delayUrgentJob(GridletInfo gi, List<Result> swapResults) {
		 if (!swapResults.isEmpty()) {
			//double newLength = gi.getGridlet().getGridletLength() 
         	//		+ Collections.max(swapDelays);
         	//gi.getGridlet().setGridletLength(newLength);
         	//gi.setLength(gi.getGridlet().getGridletLength());
         	gi.setSubmissionDelay(Collections.max(swapResults, swapDelayComparator).delay);
         }
	}
	
	/**
     * UrgencyComparator<p>
     * Compares two SwapResults according to their delays.
     */
    public static Comparator<Result> swapDelayComparator = new Comparator<Result>() {
		@Override
		public int compare(Result o1, Result o2) {
	        if(o1.getDelay() > o2.getDelay()) 
	        	return 1;
	        else if(o1.getDelay() < o2.getDelay()) 
	        	return -1;
	        else
	        	return 0;
		}
	};
	
}
