package agung.extensions.urgent;

/**
 * Interface of swapping time generator.
 * Provides times for swapin and swapout.
 * @author agung
 *
 */
public interface SwapTimeGen {
	double genSwapinTime();
	double genSwapoutTime();
}
