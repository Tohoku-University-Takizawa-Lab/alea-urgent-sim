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

	public void init(AleaConfiguration aCfg, SxAceJobUtil sxJobUtil) {
		int injectNum =  aCfg.getInt("inject_num");
    	int injectSeed = aCfg.getInt("inject_randseed");
		String injectorClass = aCfg.getString("inject_class");
		
    	if (injectorClass.equals("RandomBasedJobInjector") && sxJobUtil != null) {
            float injectProb =  (float) aCfg.getDouble("inject_prob");
        	jobInjector = RandomBasedJobInjector.getInstance();
        	((RandomBasedJobInjector) jobInjector).init(injectNum, injectProb, sxJobUtil, injectSeed);
    	}
    	else if (injectorClass.equals("MonthlyUrgentJobInjector") && sxJobUtil != null) {
    		jobInjector = MonthlyUrgentJobInjector.getInstance();
    		((MonthlyUrgentJobInjector) jobInjector).init(sxJobUtil, injectNum, injectSeed);
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

}
