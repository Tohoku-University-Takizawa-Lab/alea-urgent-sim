package agung.extensions.urgent;

import java.util.Random;

import alea.core.AleaSimTags;
import gridsim.GridSim;
import xklusac.environment.ComplexGridlet;

public class RandomBasedJobInjector implements JobInjector {

	private SxAceJobUtil sxJobUtil;
	private int numInjects;
	private int numInjectsNow = 0;
	private float injectProb;
	private Random injectRand;
	
	
	public void init(int numInjects, float injectProb, SxAceJobUtil sxJobUtil, long randSeed) {
		this.numInjects = numInjects;
		this.injectProb = injectProb;
		if (randSeed > 0)
			this.injectRand = new Random(randSeed);
		else
			this.injectRand = new Random();
		this.sxJobUtil = sxJobUtil;
	}


	@Override
	public int injectJobs(GridSim gridsim, double arrivalTime) {
		int injected = 0;
		if (numInjectsNow < numInjects && injectRand.nextFloat() <= injectProb) {
			
			ComplexGridlet gl = sxJobUtil.generateUrgentJob(arrivalTime);
			
	        // and set user id to the Scheduler entity - otherwise it would be returned to the JobLoader when completed.
	        //System.out.println(id+" job has limit = "+(job_limit/3600.0)+" queue = "+queue);
	        gl.setUserID(gridsim.getEntityId("Alea_3.0_scheduler"));
	        
			//current_gl++;
			numInjectsNow++;
			//if (gl == null) {
			//	super.sim_schedule(this.getEntityId(this.getEntityName()), 0.0, AleaSimTags.EVENT_WAKE);
			//}
			
			// to synchronize job arrival wrt. the data set.
			double delay = Math.max(0.0, (gl.getArrival_time() - gridsim.clock()));
			// some time is needed to transfer this job to the scheduler, i.e., delay should
			//System.out.println("- Inject urgent job: "+ gl.getGridletID());
			//last_delay = delay;
			gridsim.sim_schedule(gridsim.getEntityId("Alea_3.0_scheduler"), delay, AleaSimTags.GRIDLET_INFO, gl);

			//delay = Math.max(0.0, (gl.getArrival_time() - super.clock()));
			//if (current_gl < total_jobs) {
				// use delay - next job will be loaded after the simulation time is equal to the
				// previous job arrival.
			//	super.sim_schedule(this.getEntityId(this.getEntityName()), delay, AleaSimTags.EVENT_WAKE);
			//}
			injected++;
		}
		return injected;
	}

	// Use singleton for the default access
	private static RandomBasedJobInjector instance;
	
	public static RandomBasedJobInjector getInstance() {
		if(instance == null){
            instance = new RandomBasedJobInjector();
        }
        return instance;
	}
}
