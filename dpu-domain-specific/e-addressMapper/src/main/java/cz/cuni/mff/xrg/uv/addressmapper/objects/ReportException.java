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
 * Used to report exception that occurred during entity processing.
 *
 * @author Škoda Petr
 */
public class ReportException extends Report {

    public ReportException(URI source, Exception exception) {
        super(source, "Exception: " + exception.getMessage());
    }

    @Override
    public List<Statement> asStatements(URI subject) {
        final ValueFactory valueFactory = ValueFactoryImpl.getInstance();
        final EntityBuilder entityBuilder = prepareEntityBuilder(subject, valueFactory);

        entityBuilder.property(RDF.TYPE, AddressMapperOntology.REPORT_EXCEPTION);

        return entityBuilder.asStatements();
    }

}
