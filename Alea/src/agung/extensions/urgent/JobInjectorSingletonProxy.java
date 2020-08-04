package agung.extensions.urgent;

import gridsim.GridSim;
import xklusac.environment.AleaConfiguration;

public class JobInjectorSingletonProxy implements JobInjector {
	
	private static JobInjector jobInjector;
	private SxAceJobUtil sxJobUtil;
	
	public JobInjectorSingletonProxy(AleaConfiguration aCfg) {
		init(aCfg);
	}
	
	public JobInjectorSingletonProxy(AleaConfiguration aCfg, SxAceJobUtil sxJobUtil) {
		this(aCfg);
		this.sxJobUtil = sxJobUtil;
	}

	public void init(AleaConfiguration aCfg) {
		int injectNum =  aCfg.getInt("inject_num");
    	int injectSeed = aCfg.getInt("inject_randseed");
		String injectorClass = aCfg.getString("inject_class");
		
    	if (injectorClass.equals("RandomBasedJobInjector")) {
            float injectProb =  (float) aCfg.getDouble("inject_prob");
        	jobInjector = RandomBasedJobInjector.getInstance();
        	((RandomBasedJobInjector) jobInjector).init(injectNum, injectProb, sxJobUtil, injectSeed);
    	}
    	else if (injectorClass.equals("MonthlyUrgentJobInjector")) {
    		jobInjector = MonthlyUrgentJobInjector.getInstance();
    		((MonthlyUrgentJobInjector) jobInjector).init(sxJobUtil, injectNum, injectSeed);
    	}
	}

	@Override
	public int injectJobs(GridSim gridsim, double arrivalTime, int ratingPE) {
		return jobInjector.injectJobs(gridsim, arrivalTime, ratingPE);
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

}
