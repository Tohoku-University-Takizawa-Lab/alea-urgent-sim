package xklusac.environment;

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
		scheduler.cancelJob(gi.getGridlet(), ri.resource.getResourceID(), delay);
		
		// Put the preempted job on the head of queue
		ri.addGInfo(0, gi);
		
		gi.setSuspended(true);
		gi.addSwapDelay(delay);
		return delay;
	}
	
	public double swapin(GridletInfo gi, ResourceInfo ri) {
		double delay = delayGen.genSwapinTime();
		scheduler.submitJobWithDelay(gi.getGridlet(), ri.resource.getResourceID(), delay);
		gi.setSuspended(false);
		gi.addSwapDelay(delay);
		return delay;
	}
	
}
