package xklusac.environment;

import gridsim.Gridlet;

/**
 * Class for emulating job swapping.
 * @author agung
 *
 */
public class JobSwapper {
	
	// Time needed for swapping in (in seconds)
	public static final double DEF_SWAPOUT_DELAY = 1;
	public static final double DEF_SWAPIN_DELAY = 1;
	
	private Scheduler scheduler;
	private SwapTimeGen delayGen;
	
	public JobSwapper(Scheduler scheduler, SwapTimeGen gen) {
		this.scheduler = scheduler;
		this.delayGen = gen;
	}
	
	public double swapout(GridletInfo gi, ResourceInfo ri) {
		double delay = delayGen.genSwapoutTime();
		// Emulating the swapping time with GridSim delay
		// 1. Pause no delay
		// 2. Resume with delay of swap
		// 3. Cancel no delay
		//scheduler.pauseJob(gi.getGridlet(), ri.resource.getResourceID(), 0);
		//scheduler.resumeJob(gi.getGridlet(), ri.resource.getResourceID(), delay);
		scheduler.cancelJob(gi.getGridlet(), ri.resource.getResourceID(), delay);
		
		// Put the preempted job on the head of queue if it is canceled successfully
		if (gi.getGridlet().getGridletStatus() == Gridlet.CANCELED) {
			ri.addGInfo(0, gi);
		
			gi.setSuspended(true);
			gi.getGridlet().addTotalSwapDelay(delay);
			gi.getGridlet().addNumPreempted(1);
		}
		return delay;
	}
	
	public double swapin(GridletInfo gi, ResourceInfo ri) {
		double delay = delayGen.genSwapinTime();
		scheduler.submitJobWithDelay(gi.getGridlet(), ri.resource.getResourceID(), delay);
		gi.setSuspended(false);
		gi.getGridlet().addTotalSwapDelay(delay);
		return delay;
	}
	
}
