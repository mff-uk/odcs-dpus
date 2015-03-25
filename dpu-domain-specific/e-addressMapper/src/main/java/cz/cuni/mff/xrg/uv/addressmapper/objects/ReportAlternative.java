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
public class ReportAlternative extends Report {

    public ReportAlternative(URI source, String message) {
        super(source, message);
    }

    @Override
    public List<Statement> asStatements(URI subject) {
        final ValueFactory valueFactory = ValueFactoryImpl.getInstance();
        final EntityBuilder entityBuilder = new EntityBuilder(subject, valueFactory);

        entityBuilder.property(RDF.TYPE, AddressMapperOntology.REPORT_ALTERNATIVE);

        entityBuilder.property(AddressMapperOntology.MESSAGE, message);
        entityBuilder.property(AddressMapperOntology.SOURCE, source);

        return entityBuilder.asStatements();
    }


}