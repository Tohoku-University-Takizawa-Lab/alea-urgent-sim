package agung.extensions.urgent;

import java.util.Random;

public class RNGUniform implements RNG {
	protected Random rand;
	
	public RNGUniform(long seed) {
		if (seed > 0)
			this.rand = new Random(seed);
		else
			this.rand = new Random();
	}

	@Override
	public float nextFloat() {
		return rand.nextFloat();
	}

	@Override
	public int nextInt(int bound) {
		return rand.nextInt(bound);
	}

}
