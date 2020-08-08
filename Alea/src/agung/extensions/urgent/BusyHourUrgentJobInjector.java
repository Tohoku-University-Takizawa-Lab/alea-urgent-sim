package agung.extensions.urgent;

import alea.core.AleaSimTags;
import gridsim.GridSim;
import xklusac.environment.ComplexGridlet;
import xklusac.environment.Scheduler;

public class BusyHourUrgentJobInjector implements JobInjector {

	private SxAceJobUtil sxJobUtil;
	//private Random injectRand;
	private RNG injectRand;
	private int lastHourInjected;
	//private int numInjects;
	//private int currentNumInjects;
	private double lastJobArrival;
	private int hourDelta;
	private double busyThreshold;
	
	public void init(SxAceJobUtil sxJobUtil, RNG rng, double busyThreshold) {
		// Every day as default
		init(sxJobUtil, rng, 24, busyThreshold);
	}
	
	public void init(SxAceJobUtil sxJobUtil, RNG rng, int hourDelta, double busyThreshold) {
		this.injectRand = rng;
		this.sxJobUtil = sxJobUtil;
		this.lastHourInjected = 0;
		//this.numInjects = numInjects;
		//this.currentNumInjects = 0;
		this.hourDelta = hourDelta;
		this.busyThreshold = busyThreshold;
	}

	@Override
	public int injectJobs(GridSim gridsim, double currentArrivalTime, int ratingPE, int numJobs) {
		int injected = 0;
		//int currentDay = (int) Math.ceil(currentArrivalTime / (3600*24));
		//int currentDay = (int) Math.floor(currentArrivalTime / (3600*24));
		int currentHour = (int) Math.floor(currentArrivalTime / 3600);
		int relativeMin = (int) Math.ceil(currentHour % 60);
		//int relativeDay = currentDay % MONTH_DAYS;
		
		//double arrivalTime = currentArrivalTime;
		//if (currentMonth > lastMonthInjected && currentNumInjects < numInjects) {
		//double currentUtil = Scheduler.getCPUUtilization();
		//if ( (currentHour - lastHourInjected) >= hourDelta && currentUtil >= busyThreshold) {
		if ( (currentHour - lastHourInjected) >= hourDelta && Scheduler.getCPUUtilization() >= busyThreshold) {
			//if (numInjectsNow < numInjects && injectRand.nextFloat() <= injectProb) {
			//int relativeDay = currentDay % MONTH_DAYS;
			//int randDay = injectRand.nextInt(MONTH_DAYS - relativeDay + 1) + relativeDay;
			//double arrivalTime = (((currentMonth-1) * MONTH_DAYS) + randDay) * 3600 * 24;
			//for (int i = 0; i < numInjectsPerDay; i++) {
				//arrivalTime = nextRandomArrivalFrom(currentMonth, relativeDay);
			//System.out.println("-> [BusyHour] CPU utilization = " + currentUtil);
			generateSendJob(gridsim, ratingPE, currentHour, relativeMin);
			injected++;
				//currentNumInjects++;
			//}
			lastHourInjected = currentHour;
		}
		//if (injected > 0)
		//	lastJobArrival = currentArrivalTime;
		return injected;
	}

	private void generateSendJob(GridSim gridsim, int ratingPE,
			int currentHour, int relativeMin) {
		//int succeedInjected = 0;
		int arrivalMin = nextRandomMin(currentHour, relativeMin);
		double arrivalTime = toArrival(currentHour, arrivalMin);
		
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
		System.out.println("-> [BusyHour] Inject urgent job #"+ gl.getGridletID() 
				+ " hour-" + currentHour + ", min-"+ arrivalMin + " (" 
				+ gl.getArrival_time() +")" + ", numPE = " + gl.getNumPE() 
				+ ", length = " + gl.getGridletLength());
		//last_delay = delay;
		gridsim.sim_schedule(gridsim.getEntityId("Alea_3.0_scheduler"), delay, AleaSimTags.GRIDLET_INFO, gl);
		
		lastJobArrival = gl.getArrival_time();
		//delay = Math.max(0.0, (gl.getArrival_time() - gridsim.clock()));
		//if (current_gl < total_jobs) {
			// use delay - next job will be loaded after the simulation time is equal to the
			// previous job arrival.
		//gridsim.sim_schedule(gridsim.getEntityId(gridsim.getEntityName()), delay, AleaSimTags.EVENT_WAKE);
		//}
		
		//succeedInjected++;
	}
	
	// Use singleton for the default access
	private static BusyHourUrgentJobInjector instance;
	
	public static BusyHourUrgentJobInjector getInstance() {
		if(instance == null){
            instance = new BusyHourUrgentJobInjector();
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
	
	private int nextRandomMin(int currentHour, int fromMinute) {
		return injectRand.nextInt(60 - fromMinute + 1) + fromMinute;
	}
	
	private double toArrival(int hour, int min) {
		return ((hour * 60) + min) * 60;
	}

	@Override
	public boolean isFinished() {
		// Always finished when called because it depends on job traces
		return true;
	}
}
