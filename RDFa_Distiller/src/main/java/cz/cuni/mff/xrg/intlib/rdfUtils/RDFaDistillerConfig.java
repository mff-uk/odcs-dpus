package cz.cuni.mff.xrg.intlib.rdfUtils;

import cz.cuni.xrg.intlib.commons.configuration.DPUConfigObject;
import cz.cuni.xrg.intlib.commons.module.config.DPUConfigObjectBase;
import cz.cuni.xrg.intlib.commons.ontology.OdcsTerms;
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
