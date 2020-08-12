package agung.extensions.urgent;

import xklusac.environment.ComplexGridlet;

public class JobSizeSwapTime implements SwapTimeGen {

	private final static double MB_UNIT = 1024 * 1024;
			
	// Time needed (in s) for swapping job per size unit
	private double inDelayPerUnit;
	private double outDelayPerUnit;

	public JobSizeSwapTime(double inDelayPerUnit, double outDelayPerUnit) {
		this.inDelayPerUnit = inDelayPerUnit;
		this.outDelayPerUnit = outDelayPerUnit;
	}
	
	private double jobUnit(ComplexGridlet gl) {
		if (gl.getRam() > 1) {
			double ramGB = Math.max(1.0, gl.getRam() / MB_UNIT);
			if (gl.getNumNodes() > 0)
				return ramGB * gl.getNumNodes();
			else
				return ramGB;
		}
		else
			return gl.getNumPE();
	}
	
	@Override
	public double genSwapinTime(ComplexGridlet gl) {
		return inDelayPerUnit * jobUnit(gl);
	}

	@Override
	public double genSwapoutTime(ComplexGridlet gl) {
		return outDelayPerUnit * jobUnit(gl);
	}

}
