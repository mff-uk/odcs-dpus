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
public class DataTTL extends RDFLoaderWrapper {

    public DataTTL(RDFDataUnit _du, String outputPath) {
        super(_du, outputPath);
    }

    
    @Override
    public void addData(File f) throws RDFException {
         du.addFromTurtleFile(f);
         
    }
    
}
