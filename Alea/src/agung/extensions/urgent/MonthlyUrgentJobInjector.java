package agung.extensions.urgent;

import alea.core.AleaSimTags;
import gridsim.GridSim;
import xklusac.environment.ComplexGridlet;

public class MonthlyUrgentJobInjector implements JobInjector {

	private static final int MONTH_DAYS = 30;
	
	private SxAceJobUtil sxJobUtil;
	//private Random injectRand;
	private RNG injectRand;
	private int lastMonthInjected;
	//private int numInjects;
	//private int currentNumInjects;
	private double lastJobArrival;
	private int numInjectsPerMonth;
	
	public void init(SxAceJobUtil sxJobUtil, RNG rng) {
		init(sxJobUtil, rng, 1);
	}
	
	public void init(SxAceJobUtil sxJobUtil, RNG rng, int numInjectsPerMonth) {
		this.injectRand = rng;
		this.sxJobUtil = sxJobUtil;
		this.lastMonthInjected = 0;
		//this.numInjects = numInjects;
		//this.currentNumInjects = 0;
		this.numInjectsPerMonth = numInjectsPerMonth;
	}

	@Override
	public int injectJobs(GridSim gridsim, double currentArrivalTime, int ratingPE, int numJobs) {
		int injected = 0;
		
		//if (currentNumInjects < numInjects) {
			int currentDay = (int) Math.ceil(currentArrivalTime / (3600*24));
			//long currentDay = Math.round(currentArrivalTime / (3600*24));
			int currentMonth = (int) Math.ceil((double) currentDay / MONTH_DAYS);
			int relativeDay = currentDay % MONTH_DAYS;
			
			double arrivalTime = currentArrivalTime;
			//if (currentMonth > lastMonthInjected && currentNumInjects < numInjects) {
			if (currentMonth > lastMonthInjected) {
				//if (numInjectsNow < numInjects && injectRand.nextFloat() <= injectProb) {
				//int relativeDay = currentDay % MONTH_DAYS;
				//int randDay = injectRand.nextInt(MONTH_DAYS - relativeDay + 1) + relativeDay;
				//double arrivalTime = (((currentMonth-1) * MONTH_DAYS) + randDay) * 3600 * 24;
				for (int i = 0; i < numInjectsPerMonth; i++) {
					//arrivalTime = nextRandomArrivalFrom(currentMonth, relativeDay);
					generateSendJob(gridsim, ratingPE, currentMonth, relativeDay);
					injected++;
					//currentNumInjects++;
				}
				lastMonthInjected = currentMonth;
			}
			else if (numJobs > 1) {
				// Job traces finished but the allocated number of injections has not been reached.
				// So, let's put the remaining injections on the current month.
				//int numRemaining = numInjects - currentNumInjects;
				for (int i = 0; i < numJobs; i++) {
					//arrivalTime = nextRandomArrivalFrom(currentMonth, relativeDay);
					generateSendJob(gridsim, ratingPE, currentMonth, relativeDay);
					injected++;
					//currentNumInjects++;
				}
			}
			if (injected > 0)
				lastJobArrival = arrivalTime;
		//}
		return injected;
	}

	private void generateSendJob(GridSim gridsim, int ratingPE,
			int currentMonth, int relativeDay) {
		//int succeedInjected = 0;
		int arrivalDay = nextRandomDay(currentMonth, relativeDay);
		double arrivalTime = toArrival(currentMonth, arrivalDay);
		
		ComplexGridlet gl = sxJobUtil.generateUrgentJob(arrivalTime, ratingPE);
		
        // and set user id to the Scheduler entity - otherwise it would be returned to the JobLoader when completed.
        //System.out.println(id+" job has limit = "+(job_limit/3600.0)+" queue = "+queue);
        gl.setUserID(gridsim.getEntityId("Alea_3.0_scheduler"));
        
		//current_gl++;
		//if (gl == null) {
		//	super.sim_schedule(this.getEntityId(this.getEntityName()), 0.0, AleaSimTags.EVENT_WAKE);
		//}
		
		// to synchronize job arrival wrt. the data set.
		double delay = Math.max(0.0, (gl.getArrival_time() - gridsim.clock()));
		// some time is needed to transfer this job to the scheduler, i.e., delay should
		System.out.println("- Inject urgent job #"+ gl.getGridletID() 
				+ " at month-" + currentMonth + ", day-"+ arrivalDay + " (" 
				+ gl.getArrival_time() +")" + ", numPE = " + gl.getNumPE() 
				+ ", length = " + gl.getGridletLength());
		//last_delay = delay;
		gridsim.sim_schedule(gridsim.getEntityId("Alea_3.0_scheduler"), delay, AleaSimTags.GRIDLET_INFO, gl);
		
		//delay = Math.max(0.0, (gl.getArrival_time() - gridsim.clock()));
		//if (current_gl < total_jobs) {
			// use delay - next job will be loaded after the simulation time is equal to the
			// previous job arrival.
		//gridsim.sim_schedule(gridsim.getEntityId(gridsim.getEntityName()), delay, AleaSimTags.EVENT_WAKE);
		//}
		
		//succeedInjected++;
	}
	
	// Use singleton for the default access
	private static MonthlyUrgentJobInjector instance;
	
	public static MonthlyUrgentJobInjector getInstance() {
		if(instance == null){
            instance = new MonthlyUrgentJobInjector();
        }
        return instance;
	}

	@Override
	public int getTotalNumInjects() {
		return -1;
	}

	@Override
	public double getLastJobArrival() {
		return lastJobArrival;
	}
	
	private double nextRandomArrivalFrom(int currentMonth, int fromDay) {
		int randDay = injectRand.nextInt(MONTH_DAYS - fromDay + 1) + fromDay;
		double arrivalTime = (((currentMonth-1) * MONTH_DAYS) + randDay) * 3600 * 24;
		return arrivalTime;
	}
	
	private int nextRandomDay(int currentMonth, int fromDay) {
		return injectRand.nextInt(MONTH_DAYS - fromDay + 1) + fromDay;
	}
	
	private double toArrival(int month, int day) {
		return (((month-1) * MONTH_DAYS) + day) * 3600 * 24;
	}

	@Override
	public boolean isFinished() {
		// Always finished when called because it depends on job traces
		return true;
	}
}
