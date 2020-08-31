package agung.extensions.urgent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.atomic.AtomicInteger;

import xklusac.environment.ComplexGridlet;

public class SxAceJobUtil {
	
	public static final int URGENT_ID_OFFSET = 1000000;
	private static final String URGENT_USER = "thanos";
	private static final String URGENT_QUEUE = "urgentQ";
	
	// Job limit as specified in the SX-ACE manual: https://www.ss.cc.tohoku.ac.jp/super/index.html
	private static final long JOB_LIMIT_DEF = 7 * 3600 * 24;			// 1 week
	private static final long JOB_LIMIT_MAX = JOB_LIMIT_DEF * 4;		// 1 month
	
	private Random random;
	private long jobLenMin;
	private long jobLenMax;
	private int numNodesMin;
	private int numNodesMax;
	private Map<Integer, Long> nodeLengthMap;
	private boolean usePredefinedJobLengths;
	
	private AtomicInteger currentId;
	
	public SxAceJobUtil(long jobLenMin, long jobLenMax, int numNodesMin, int numNodesMax, long randSeed) {
		this(jobLenMin, jobLenMax, numNodesMin, numNodesMax, randSeed, false);
	}
	
	public SxAceJobUtil(long jobLenMin, long jobLenMax, int numNodesMin, int numNodesMax, long randSeed,
			boolean useJobLenMap) {
		this.jobLenMin = jobLenMin;
		this.jobLenMax = jobLenMax;
		this.numNodesMin = numNodesMin;
		this.numNodesMax = numNodesMax;
		
		this.currentId = new AtomicInteger(0);
		
		if (randSeed > 0)
			random = new Random(randSeed);
		else
			random = new Random();
		this.usePredefinedJobLengths = useJobLenMap;
		nodeLengthMap = new LinkedHashMap<>();
	}

	public void setLengthNodeMap(Map<Integer, Long> lengthNodeMap) {
		this.nodeLengthMap = lengthNodeMap;
	}
	
	public void setLengthNodeMapStr(String str) {
		String[] keyValuepairs = str.split(",");
		for (String pair: keyValuepairs) {
			String[] entry = pair.split("=");
			nodeLengthMap.put(Integer.parseInt(entry[0]), Long.parseLong(entry[1]));
		}
	}

	public ComplexGridlet generateUrgentJobRandomLen(double arrival, int ratingPE) {
		
		long randLen = genRandomJobLength();
		long jobLen = randLen * ratingPE;
		// User accurately estimate the job runtime, so the job limit = jobLen
		//double deadline = jobLen * 2;
		int numNodes = genRandomNumNodes();
		long ramUsage = 0;
		int ppn = 1;
		long deadline = (long) (randLen * UrgentGridletUtil.defaultDeadlineRatio);
		
		ComplexGridlet gl = new ComplexGridlet(URGENT_ID_OFFSET + currentId.getAndIncrement(), URGENT_USER,
				JOB_LIMIT_DEF, (double) jobLen, (double) jobLen, 10, 10, "Linux", "Risc arch.", arrival, deadline,
				1, numNodes, 0.0, URGENT_QUEUE, "", 0.0, ramUsage, numNodes, ppn, UrgentGridletUtil.DEFAULT_URGENCY);
		
		return gl;
	}
	
	public ComplexGridlet generateUrgentJob(double arrival, int ratingPE) {
		if (!usePredefinedJobLengths)
			return generateUrgentJobRandomLen(arrival, ratingPE);
		else
			return generateUrgentJobMap(arrival, ratingPE);
	}
	
	
	public long genRandomJobLength() {
		//return jobLenMin + (jobLenMax - jobLenMin) * random.nextLong();
		return (random.nextLong() % (jobLenMax - jobLenMin)) + jobLenMin;
	}
	
	public int genRandomNumNodes() {
		return random.nextInt(numNodesMax + 1 - numNodesMin) + numNodesMin;
	}
	
	public ComplexGridlet generateUrgentJobMap(double arrival, int ratingPE) {
		ComplexGridlet gl = null;
		if (!nodeLengthMap.isEmpty()) {
			List<Integer> keys = new ArrayList<>(nodeLengthMap.keySet());
			int randIdx = random.nextInt(keys.size());
			int numNodes = keys.get(randIdx);
			long randLen = nodeLengthMap.get(numNodes);
			long jobLen = randLen * ratingPE;
			
			long ramUsage = 0;
			int ppn = 1;
			long deadline = (long) (randLen * UrgentGridletUtil.defaultDeadlineRatio);
			
			gl = new ComplexGridlet(URGENT_ID_OFFSET + currentId.getAndIncrement(), URGENT_USER,
					JOB_LIMIT_DEF, (double) jobLen, (double) jobLen, 10, 10, "Linux", "Risc arch.", arrival, deadline,
					1, numNodes, 0.0, URGENT_QUEUE, "", 0.0, ramUsage, numNodes, ppn, UrgentGridletUtil.DEFAULT_URGENCY);
			
		}
		else {
			System.err.println("* ERR: map for generating jobs is empty!");
		}
		return gl;
	}
	
}
