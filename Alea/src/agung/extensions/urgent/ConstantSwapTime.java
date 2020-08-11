package agung.extensions.urgent;

import xklusac.environment.ComplexGridlet;

public class ConstantSwapTime implements SwapTimeGen {

	// Time needed for swapping in (in seconds)
	private double swapinTime;
	private double swapoutTime;

	public ConstantSwapTime(double swapinTime, double swapoutTime) {
		this.swapinTime = swapinTime;
		this.swapoutTime = swapoutTime;
	}
	
	@Override
	public double genSwapinTime(ComplexGridlet gl) {
		return swapinTime;
	}

	@Override
	public double genSwapoutTime(ComplexGridlet gl) {
		return swapoutTime;
	}

}
