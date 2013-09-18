/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.mff.xrg.intlib.extractor.jtaggerExtractor.uriGenerator.link;

/**
 *
 * @author Jakub
 */
public class Shortcut {
    private String countryOfIssue;
    private WorkType typeOfWork;
    private Integer year;
    private String number;
    private Integer section;
    private Integer subsection;
    private Integer paraNumber;

    public Shortcut(
            String countryOfIssue, 
            String typeOfWork, 
            String year, 
            String number, 
            String section, 
            String subsection, 
            String paraNumber
    ) {
        this.countryOfIssue = countryOfIssue;
        for (WorkType wt: WorkType.values()) {
            if (wt.toString().equals(typeOfWork)) {
                this.typeOfWork = wt;
            }
        }
        if (year != null && !year.isEmpty()) {
            this.year = Integer.parseInt(year);
        }
        this.number = number;
    }
    
    public void apply(Work w) {
        if (this.year != null) {
            if (w.getYear() == null) {
                w.setYear(this.year);
            }
        } 
    
        if (this.number != null) {
            if (w.getNumber() == null) {
                w.setNumber(this.number);
            }
        } 
    }
    
}
