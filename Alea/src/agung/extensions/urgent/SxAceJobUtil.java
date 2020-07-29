package agung.extensions.urgent;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import xklusac.environment.ComplexGridlet;

public class SxAceJobUtil {
	
	private static final int URGENT_ID_OFFSET = 1000000;
	private static final String URGENT_USER = "thanos";
	private static final String URGENT_QUEUE = "urgentQ";
	
	
	private Random random;
	private long jobLenMin;
	private long jobLenMax;
	private int numNodesMin;
	private int numNodesMax;
	
	private AtomicInteger currentId;
	
	public SxAceJobUtil(long jobLenMin, long jobLenMax, int numNodesMin, int numNodesMax, long randSeed) {
		this.jobLenMin = jobLenMin;
		this.jobLenMax = jobLenMax;
		this.numNodesMin = numNodesMin;
		this.numNodesMax = numNodesMax;
		
		this.currentId = new AtomicInteger(0);
		
		if (randSeed > 0)
			random = new Random(randSeed);
		else
			random = new Random();
	}


	public ComplexGridlet generateUrgentJob(double arrival) {
		
		long jobLen = genRandomJobLength();
		// User accurately estimate the job runtime, so the job limit = jobLen
		double deadline = jobLen * 2;
		int numNodes = genRandomNumNodes();
		long ramUsage = 0;
		int ppn = 1;
		
		ComplexGridlet gl = new ComplexGridlet(URGENT_ID_OFFSET + currentId.getAndIncrement(), URGENT_USER,
				jobLen, (double) jobLen, (double) jobLen, 10, 10, "Linux", "Risc arch.", arrival, deadline, 1,
				numNodes, 0.0, URGENT_QUEUE, "", 0.0, ramUsage, numNodes, ppn, UrgentGridletUtil.DEFAULT_URGENCY);
		
		return gl;
	}
	
	
	public long genRandomJobLength() {
		//return jobLenMin + (jobLenMax - jobLenMin) * random.nextLong();
		return (random.nextLong() % (jobLenMax - jobLenMin)) + jobLenMin;
	}
	
	public int genRandomNumNodes() {
		return random.nextInt(numNodesMax + 1 - numNodesMin) + numNodesMin;
	}
	
}
