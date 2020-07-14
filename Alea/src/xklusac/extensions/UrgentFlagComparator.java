package xklusac.extensions;
import java.util.Comparator;
import xklusac.environment.GridletInfo;

/**
 * Class UrgentFlagComparator<p>
 * Compares two gridlets according to their URGENT flags.
 * @author agung
 */
public class UrgentFlagComparator implements Comparator {

    @Override
    public int compare(Object o1, Object o2) {
        GridletInfo g1 = (GridletInfo) o1;
        GridletInfo g2 = (GridletInfo) o2;
        if(g1.getUrgency() > g2.getUrgency()) return 1;
        if(g1.getUrgency() == g2.getUrgency()) return 0;
        if(g1.getUrgency() < g2.getUrgency()) return -1;
        return 0;
    }
    

}
