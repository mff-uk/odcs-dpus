package cz.cuni.mff.xrg.uv.addressmapper.objects;

import java.util.List;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;

import cz.cuni.mff.xrg.uv.addressmapper.AddressMapperOntology;
import eu.unifiedviews.helpers.dpu.rdf.EntityBuilder;


/**
 * Used to report
 *
 * @author Škoda Petr
 */
public class Report {

    protected String message;

    protected final URI source;

    public Report(URI source, String message) {
        this.message = message;
        this.source = source;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Statement> asStatements(URI subject) {
        final ValueFactory valueFactory = ValueFactoryImpl.getInstance();
        final EntityBuilder entityBuilder = new EntityBuilder(subject, valueFactory);

        entityBuilder.property(RDF.TYPE, AddressMapperOntology.REPORT);

        entityBuilder.property(AddressMapperOntology.MESSAGE, message);
        entityBuilder.property(AddressMapperOntology.SOURCE, source);

        return entityBuilder.asStatements();
    }

}
