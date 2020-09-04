package agung.extensions.urgent;

import gridsim.GridSim;
import xklusac.environment.AleaConfiguration;

public class JobInjectorSingletonProxy implements JobInjector {
	
	private static JobInjector jobInjector;
	
	public JobInjectorSingletonProxy(AleaConfiguration aCfg) {
		init(aCfg, null);
	}
	
	public JobInjectorSingletonProxy(AleaConfiguration aCfg, SxAceJobUtil sxJobUtil) {
		init(aCfg, sxJobUtil);
	}

	public RNG initRNG(String name, long seed) {
		RNG rng = null;
		RNG.TYPE rngClass = RNG.TYPE.valueOf(name.toUpperCase());
		switch (rngClass) {
		case NORMAL:
			rng = new RNGNormal(seed);
			break;

		default:
			rng = new RNGUniform(seed);
			break;
		}
		return rng;
	}
	
	public void init(AleaConfiguration aCfg, SxAceJobUtil sxJobUtil) {
		//int injectNum =  aCfg.getInt("inject_num");
    	int injectSeed = aCfg.getInt("inject_randseed");
		String injectorClass = aCfg.getString("inject_class");
		//String rngName = aCfg.getString("rng");
		
		//RNG rng = initRNG(rngName, injectSeed);
		RNG rng = new RNGUniform(injectSeed);
		// Reset jobUtil
		sxJobUtil.reset();
		
    	if (injectorClass.equals("RandomBasedJobInjector") && sxJobUtil != null) {
    		
            float injectProb =  (float) aCfg.getDouble("inject_prob");
        	//jobInjector = RandomBasedJobInjector.getInstance();
            jobInjector = new RandomBasedJobInjector();
        	((RandomBasedJobInjector) jobInjector).init(injectProb, sxJobUtil, rng);
    	}
    	else if (injectorClass.equals("MonthlyUrgentJobInjector") && sxJobUtil != null) {
    		int injectNumPerMonth =  aCfg.getInt("inject_num_monthly");
    		//jobInjector = MonthlyUrgentJobInjector.getInstance();
    		jobInjector = new MonthlyUrgentJobInjector();
    		if (injectNumPerMonth > 1)
    			((MonthlyUrgentJobInjector) jobInjector).init(sxJobUtil, rng, injectNumPerMonth);
    		else
    			((MonthlyUrgentJobInjector) jobInjector).init(sxJobUtil, rng);
    	}
    	else if (injectorClass.equals("BusyHourUrgentJobInjector") && sxJobUtil != null) {
    		int injectHourDelta =  aCfg.getInt("inject_busy_hour_delta");
    		double busyThreshold =  aCfg.getDouble("inject_busy_hour_threshold");
    		jobInjector = new BusyHourUrgentJobInjector();
			((BusyHourUrgentJobInjector) jobInjector).init(sxJobUtil, rng,
					injectHourDelta, busyThreshold);
    	}
    	else {
    		throw new RuntimeException("JobInjector is not registered: " + injectorClass);
    	}
	}

	@Override
	public int injectJobs(GridSim gridsim, double arrivalTime, int ratingPE, int numJobs) {
		return jobInjector.injectJobs(gridsim, arrivalTime, ratingPE, numJobs);
	}
	
	@Override
	public int getTotalNumInjects() {
		return jobInjector.getTotalNumInjects();
	}
	
	
	public static JobInjector get() {
		if (jobInjector == null) {
    		throw new RuntimeException("JobInjector has been not initialized yet!");
    	}
		
        return jobInjector;
	}

	@Override
	public double getLastJobArrival() {
		return jobInjector.getLastJobArrival();
	}

	@Override
	public boolean isFinished() {
		return jobInjector.isFinished();
	}

}
