package cz.cuni.mff.xrg.intlib.rdfUtils;

import cz.cuni.mff.xrg.odcs.commons.configuration.DPUConfigObject;
import cz.cuni.mff.xrg.odcs.commons.module.config.DPUConfigObjectBase;
import cz.cuni.mff.xrg.odcs.commons.ontology.OdcsTerms;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.LoggerFactory;

/**
 *
 * Put your DPU's configuration here.
 *
 */
public class RDFaDistillerConfig extends DPUConfigObjectBase {

    private String inputPredicate = OdcsTerms.DATA_UNIT_XML_VALUE_PREDICATE;
     
   

    public String getInputPredicate() {
        return inputPredicate;
    }
}
