/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agung.extensions.urgent;

import java.util.Comparator;
import java.util.List;

import xklusac.environment.ComplexGridlet;
import xklusac.environment.GridletInfo;
import xklusac.environment.ResourceInfo;

/**
 * Utility for urgent jobs
 * @author agung
 */
public class UrgentGridletUtil {
    
    public static final int DEFAULT_URGENCY = 999;
    public static double defaultDeadlineRatio = 1.0;
    
    public static boolean isUrgent(ComplexGridlet gl) {
        return gl.getUrgency() == DEFAULT_URGENCY;
    }
    
    public static boolean isUrgent(GridletInfo gi) {
        return gi.getUrgency() == DEFAULT_URGENCY;
    }
    
    public static void setDefaultDeadlineRatio(double val) {
    	UrgentGridletUtil.defaultDeadlineRatio = val;
    }
    
    public static double getDefaultDeadlineRatio() {
    	return defaultDeadlineRatio;
    }
    
    public static boolean isUrgentJobsSorted(List<GridletInfo> infos) {
        for (int i = 0; i < infos.size()-1; ++i) {
            ///GridletInfo g1 = (GridletInfo) infos.get(i);
            //GridletInfo g2 = (GridletInfo) infos.get(i+1);
            if (urgencyComparator.compare(infos.get(i), infos.get(i+1)) > 0)
                return false;
            	//System.out.println(g1.getUrgency() + " <> " + g2.getUrgency());
        }
        return true;
    }
    
    public static Comparator<GridletInfo> finishSoFarComparator = new Comparator<GridletInfo>() {
        @Override
        public int compare(GridletInfo o1, GridletInfo o2) {
            if (o1.getFinishedSoFar() > o1.getFinishedSoFar())
                return 1;
            else if (o1.getFinishedSoFar() < o1.getFinishedSoFar())
                return -1;
            else
                return 0;
        }
    };
    
    public static Comparator<ResourceInfo> numFreePEComparator = new Comparator<ResourceInfo>() {
        @Override
        public int compare(ResourceInfo o1, ResourceInfo o2) {
            return o2.getNumFreePE() - o1.getNumFreePE();
        }
    };
    
    /**
     * UrgencyComparator<p>
     * Compares two gridlets according to their urgencies.
     */
    public static Comparator<GridletInfo> urgencyComparator = new Comparator<GridletInfo>() {
		@Override
		public int compare(GridletInfo o1, GridletInfo o2) {
	        // The gridlet with a higher urgency is moved to the left
	        if(o1.getUrgency() > o2.getUrgency()) 
	        	return -1;
	        else if(o1.getUrgency() < o2.getUrgency()) 
	        	return 1;
	        else
	        	return 0;
		}
	};
	
	public static boolean canRunByPreemption(GridletInfo gi, List<GridletInfo> runningJobs, int nowFreePE) {
		boolean result = true;
		
		int toFree = gi.getNumPE() - nowFreePE;
	    int usedPEbyRegular = 0;
	    int usedPEbyUrgent = 0;
	    for (GridletInfo info: runningJobs) {
	    	if (!UrgentGridletUtil.isUrgent(info))
	    		usedPEbyRegular += info.getNumPE();
	    	else
	    		usedPEbyUrgent += info.getNumPE();
	    }
	  
		if (toFree > usedPEbyRegular) {
			result = false;
			System.out.println("** Urgent job #" + gi.getID() + " (nuMPE=" + gi.getNumPE() 
								+ ") cannot be executed by preemption (usedPEbyRegular=" 
								+ usedPEbyRegular + ", usedPEbyUrgent=" + usedPEbyUrgent + ").");
		}
		return result;
	}
}
