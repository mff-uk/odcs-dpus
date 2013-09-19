/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.mff.xrg.intlib.extractor.legislation.decisions.uriGenerator.link;

/**
 *
 * @author Jakub
 */
public enum WorkType {

    ACT,
    NOTICE,
    DECISION,
    AMENDMENT,
    JUDGMENT,
    REGULATION,
    DECREE,
    NOTIFICATION;

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
    
    public static WorkType getEnum(String value) {
        String val = value.toLowerCase();
        switch (val) {
            case ("act"):
                return WorkType.ACT;
            case ("notice"):
                return WorkType.NOTICE;
            case ("amentment"):
                return WorkType.AMENDMENT;
            case ("decision"):
                return WorkType.DECISION;
            case ("judgment"):
                return WorkType.JUDGMENT;                
            case ("regulation"):
                return WorkType.REGULATION;                
            case ("decree"):
                return WorkType.DECREE;                
            case ("notification"):
                return WorkType.NOTIFICATION;                                
            default:
                return WorkType.ACT;
        }
    }
}
