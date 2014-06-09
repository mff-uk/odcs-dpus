/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.mff.xrg.intlib.extractor.simplexslt.rdfUtils;

import cz.cuni.mff.xrg.odcs.rdf.simple.OperationFailedException;
import cz.cuni.mff.xrg.odcs.rdf.simple.SimpleRdfRead;
import cz.cuni.mff.xrg.odcs.rdf.simple.SimpleRdfWrite;
import java.io.File;

/**
 *
 * @author tomasknap
 */
public abstract class RDFLoaderWrapper {

    protected SimpleRdfWrite du;
    protected File outputFile;
    
    public RDFLoaderWrapper(SimpleRdfWrite _du, File outputFile) {
        this.du = _du;
        this.outputFile = outputFile;
    }
    
    public abstract void addData() throws OperationFailedException;

    public File getOutputFile() {
        return outputFile;
    }
    
    
    
}
