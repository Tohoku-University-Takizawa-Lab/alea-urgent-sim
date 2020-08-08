package agung.extensions.urgent;

import gridsim.GridSim;

public interface JobInjector {
	int injectJobs(GridSim gridsim, double arrivalTime, int ratingPE, int numJobs);
	int getTotalNumInjects();
	double getLastJobArrival();
	
	/**
	 * Check if the injector finished its jobs.
	 * Usefull for injecting regular jobs.
	 * @return boolean
	 */
	boolean isFinished();
}
