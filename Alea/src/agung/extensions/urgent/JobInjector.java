package agung.extensions.urgent;

import gridsim.GridSim;

public interface JobInjector {
	int injectJobs(GridSim gridsim, double arrivalTime);
}
