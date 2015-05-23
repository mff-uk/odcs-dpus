package cz.cuni.mff.xrg.uv.transformer.rdfandtemplatetofiles;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 *
 * @author Škoda Petr
 */
public class RdfAndTemplateToFilesVocabulary {

    public static final URI FILENAME;

    public static final URI DOCUMENT;
    
    static {
        final ValueFactory valueFactory = ValueFactoryImpl.getInstance();
        
        FILENAME = valueFactory.createURI("http://unifiedviews.eu/ontology/dpu/rdfTemplateToFiles/fileName");
        DOCUMENT = valueFactory.createURI("http://unifiedviews.eu/ontology/dpu/rdfTemplateToFiles/Document");
    }

}
