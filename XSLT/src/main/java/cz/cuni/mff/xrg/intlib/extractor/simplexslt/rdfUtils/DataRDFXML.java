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
public class DataRDFXML extends RDFLoaderWrapper {

    public DataRDFXML(RDFDataUnit _du) {
        super(_du);
    }

    
    @Override
    public void addData(File f) throws RDFException {
         du.addFromRDFXMLFile(f);
         
    }
    
}
