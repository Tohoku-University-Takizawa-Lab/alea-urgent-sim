package agung.extensions.urgent;

public interface RNG {
	enum TYPE {
		UNIFORM, NORMAL
	}
	
	float nextFloat();
	int nextInt(int bound);
}
