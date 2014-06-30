/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.mff.xrg.intlib.extractor.simplexslt.rdfUtils;

import cz.cuni.mff.xrg.uv.rdf.simple.OperationFailedException;
import cz.cuni.mff.xrg.uv.rdf.simple.SimpleRdfRead;
import cz.cuni.mff.xrg.uv.rdf.simple.SimpleRdfWrite;
import java.io.File;
import org.openrdf.rio.RDFFormat;

/**
 *
 * @author tomasknap
 */
public class DataTTL extends RDFLoaderWrapper {

    public DataTTL(SimpleRdfWrite _du, File outputFile) {
        super(_du, outputFile);
    }

    
    @Override
    public void addData() throws OperationFailedException {
         du.extract(outputFile, RDFFormat.TURTLE, null);       
    }
    
}
