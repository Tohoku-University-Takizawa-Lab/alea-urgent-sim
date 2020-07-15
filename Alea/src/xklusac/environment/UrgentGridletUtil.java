/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xklusac.environment;

import java.util.Comparator;

/**
 * Utility for urgent jobs
 * @author agung
 */
public class UrgentGridletUtil {
    
    public static final int DEFAULT_URGENCY = 999;
    
    public static boolean isUrgent(ComplexGridlet gl) {
        return gl.getUrgency() == DEFAULT_URGENCY;
    }
    
    public static boolean isUrgent(GridletInfo gi) {
        return gi.getUrgency() == DEFAULT_URGENCY;
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
}
