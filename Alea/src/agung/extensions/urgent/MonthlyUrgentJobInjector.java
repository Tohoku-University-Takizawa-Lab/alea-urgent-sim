package agung.extensions.urgent;

import java.util.Random;

import alea.core.AleaSimTags;
import gridsim.GridSim;
import xklusac.environment.ComplexGridlet;

public class MonthlyUrgentJobInjector implements JobInjector {

	private static final int MONTH_DAYS = 30;
	
	private SxAceJobUtil sxJobUtil;
	private Random injectRand;
	private int lastMonthInjected;
	private int numInjects;
	private int currentNumInjects;
	
	public void init(SxAceJobUtil sxJobUtil, int numInjects, long randSeed) {
		if (randSeed > 0)
			this.injectRand = new Random(randSeed);
		else
			this.injectRand = new Random();
		this.sxJobUtil = sxJobUtil;
		this.lastMonthInjected = 0;
		this.numInjects = numInjects;
		this.currentNumInjects = 0;
	}

	@Override
	public int injectJobs(GridSim gridsim, double currentArrivalTime, int ratingPE) {
		int injected = 0;
		
		int currentDay = (int) Math.ceil(currentArrivalTime / (3600*24));
		//long currentDay = Math.round(currentArrivalTime / (3600*24));
		int currentMonth = (int) Math.ceil((double) currentDay / MONTH_DAYS);
		
		if (currentMonth > lastMonthInjected && currentNumInjects < numInjects) {
			//if (numInjectsNow < numInjects && injectRand.nextFloat() <= injectProb) {
			int relativeDay = currentDay % MONTH_DAYS;
			int randDay = injectRand.nextInt(MONTH_DAYS - relativeDay + 1) + relativeDay;
			double arrivalTime = (((currentMonth-1) * MONTH_DAYS) + randDay) * 3600 * 24;
			
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
					+ " at month-" + currentMonth + ", day-"+ relativeDay + " (" 
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
			lastMonthInjected = currentMonth;
			currentNumInjects++;
			injected++;
		}
		return injected;
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
		return numInjects;
	}
}
