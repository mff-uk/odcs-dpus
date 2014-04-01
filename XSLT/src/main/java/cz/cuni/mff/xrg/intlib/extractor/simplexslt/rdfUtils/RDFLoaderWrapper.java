/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.mff.xrg.intlib.extractor.simplexslt.rdfUtils;

import cz.cuni.mff.xrg.odcs.rdf.exceptions.RDFException;
import cz.cuni.mff.xrg.odcs.rdf.interfaces.RDFDataUnit;
import java.io.File;

/**
 *
 * @author tomasknap
 */
public abstract class RDFLoaderWrapper {

    protected RDFDataUnit du;
    protected File outputFile;
    
    public RDFLoaderWrapper(RDFDataUnit _du, File outputFile) {
        this.du = _du;
        this.outputFile = outputFile;
    }
    
    public abstract void addData() throws RDFException;

    public File getOutputFile() {
        return outputFile;
    }
    
    
    
}
