/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.mff.xrg.intlib.extractor.simplexslt.rdfUtils;

import cz.cuni.xrg.intlib.rdf.exceptions.RDFException;
import cz.cuni.xrg.intlib.rdf.interfaces.RDFDataUnit;
import java.io.File;

/**
 *
 * @author tomasknap
 */
public abstract class RDFLoaderWrapper {

    protected RDFDataUnit du;
    
    public RDFLoaderWrapper(RDFDataUnit _du) {
        this.du = _du;
    }
    
    public abstract void addData(File f) throws RDFException;
    
    
    
}
