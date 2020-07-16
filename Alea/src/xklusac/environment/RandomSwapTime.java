package xklusac.environment;

import java.util.Random;

public class RandomSwapTime implements SwapTimeGen {

	// Time needed for swapping in (in seconds)
	private double inRangeMin;
	private double inRangeMax;
	private double outRangeMin;
	private double outRangeMax;
	private Random random;

	public RandomSwapTime(double inRangeMin, double inRangeMax,
			double outRangeMin, double outRangeMax, long seed) {
		this.inRangeMax = inRangeMax;
		this.inRangeMin = inRangeMin;
		this.outRangeMax = outRangeMax;
		this.outRangeMin = outRangeMin;
		
		if (seed > 0)
			this.random = new Random(seed);
		else
			this.random = new Random();
	}
	
	@Override
	public double genSwapinTime() {
		return inRangeMin + (inRangeMax - inRangeMin) * random.nextDouble();
	}

	@Override
	public double genSwapoutTime() {
		return outRangeMin + (outRangeMax - outRangeMin) * random.nextDouble();
	}

}
