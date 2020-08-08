package agung.extensions.urgent;

public class RNGNormal extends RNGUniform {
	
	public RNGNormal(long seed) {
		super(seed);
	}

	@Override
	public float nextFloat() {
		Double d = rand.nextGaussian();
		return d.floatValue();
	}

}
