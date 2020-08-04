/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xklusac.environment;

import agung.extensions.urgent.JobInjectorSingletonProxy;
import alea.core.AleaSimTags;
import eduni.simjava.Sim_event;
import gridsim.GridSimTags;

/**
 * Class SWFLoader
 * <p>
 * Loads jobs dynamically over time from the file. Then sends these gridlets to
 * the scheduler. SWF stands for Standard Workloads Format (SWF). In
 *
 * @author Mulya Agung
 */
public class SWFLoaderWithInjects extends SWFLoader {

	protected int numInjectsTotal;
	private int numGridletReads;
	private double currentArrival;
	
	/**
	 * Creates a new instance of JobLoader
	 */
	public SWFLoaderWithInjects(String name, double baudRate, int total_jobs, String data_set, int maxPE,
			int minPErating, int maxPErating) throws Exception {
		super(name, baudRate, total_jobs, data_set, maxPE, minPErating, maxPErating);
		this.numInjectsTotal = 0;
		this.numGridletReads = 0;
	}
	
	/**
	 * Reads jobs from data_set file and sends them to the Scheduler entity
	 * dynamically over time, while injecting artificial jobs.
	 */
	@Override
	public void body() {
		super.gridSimHold(10.0); // hold by 10 second

		while (current_gl < total_jobs) {

			Sim_event ev = new Sim_event();
			sim_get_next(ev);

			if (ev.get_tag() == AleaSimTags.EVENT_WAKE) {
				ComplexGridlet gl = null;
				// Stop reading jobs from the file when the total number of jobs in the file have been reached
				boolean fileFinished = (numGridletReads >= (total_jobs - JobInjectorSingletonProxy.get().getTotalNumInjects()));
				if (!fileFinished) {
					gl = readGridlet(current_gl);
					current_gl++;
					numGridletReads++;
				}
					
				//if (gl == null && current_gl < total_jobs) {
				if (gl == null && current_gl < total_jobs && !fileFinished) {
					super.sim_schedule(this.getEntityId(this.getEntityName()), 0.0, AleaSimTags.EVENT_WAKE);
					continue;
				} else if (gl == null && current_gl >= total_jobs) {
					continue;
				}
				
				if (gl != null) {
					currentArrival = gl.getArrival_time();
					// to synchronize job arrival wrt. the data set.
					double delay = Math.max(0.0, (gl.getArrival_time() - super.clock()));
					// some time is needed to transfer this job to the scheduler, i.e., delay should
					// be delay = delay - transfer_time. Fix this in the future.
					// System.out.println("Sending: "+gl.getGridletID());
					last_delay = delay;
					super.sim_schedule(this.getEntityId("Alea_3.0_scheduler"), delay, AleaSimTags.GRIDLET_INFO, gl);
				}
				else {
					// File finished but there are still pending injections
					// Advance the current arrival time for one month
					currentArrival += (3600*24*30);
				}
				
				// Do injections
				int numInjected = JobInjectorSingletonProxy.get().injectJobs(
						this, currentArrival, maxPErating);
				current_gl += numInjected;
				numUrgentJobs += numInjected;
				numInjectsTotal += numInjected;
				
				double delay = Math.max(0.0, (currentArrival - super.clock()));
				if (current_gl < total_jobs) {
					// use delay - next job will be loaded after the simulation time is equal to the
					// previous job arrival.
					super.sim_schedule(this.getEntityId(this.getEntityName()), delay, AleaSimTags.EVENT_WAKE);
				}

				//continue;
			}
			
			
		}
		System.out.println(
				"Shuting down - last gridlet = " + current_gl + " of " + total_jobs + ", readGridlets = " 
				+ (current_gl - numInjectsTotal) + ", urgent = " + numUrgentJobs + ", injected = " + numInjectsTotal);
		super.sim_schedule(this.getEntityId("Alea_3.0_scheduler"), Math.round(last_delay + 2),
				AleaSimTags.SUBMISSION_DONE, new Integer(current_gl));
		Sim_event ev = new Sim_event();
		sim_get_next(ev);

		if (ev.get_tag() == GridSimTags.END_OF_SIMULATION) {
			System.out.println(
					"Shuting down the " + data_set + "_PWALoader... with: " + fail + " failed or skipped jobs");
		}
		shutdownUserEntity();
		super.terminateIOEntities();

	}
	
}
