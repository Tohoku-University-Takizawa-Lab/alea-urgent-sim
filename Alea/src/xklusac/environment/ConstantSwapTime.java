package xklusac.environment;

public class ConstantSwapTime implements SwapTimeGen {

	// Time needed for swapping in (in seconds)
	private double swapinTime;
	private double swapoutTime;

	public ConstantSwapTime(double swapinTime, double swapoutTime) {
		this.swapinTime = swapinTime;
		this.swapoutTime = swapoutTime;
	}
	
	@Override
	public double genSwapinTime() {
		return swapinTime;
	}

	@Override
	public double genSwapoutTime() {
		return swapoutTime;
	}

}
