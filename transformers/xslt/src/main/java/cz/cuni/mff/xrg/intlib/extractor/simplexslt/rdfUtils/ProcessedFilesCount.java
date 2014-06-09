/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.mff.xrg.intlib.extractor.simplexslt.rdfUtils;

/**
 *
 * @author tomasknap
 */
public class ProcessedFilesCount {
    
    public int successful = 0;
    public int all = 0;

    public ProcessedFilesCount(int successful, int all) {
        this.successful = successful;
        this.all = all;
    }

    public ProcessedFilesCount() {
    }
    
    

    
}
