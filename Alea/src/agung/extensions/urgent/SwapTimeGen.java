package agung.extensions.urgent;

import xklusac.environment.ComplexGridlet;

/**
 * Interface of swapping time generator.
 * Provides times for swapin and swapout.
 * @author agung
 *
 */
public interface SwapTimeGen {
	double genSwapinTime(ComplexGridlet gl);
	double genSwapoutTime(ComplexGridlet gl);
}
