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
 *
 * @author Škoda Petr
 */
public class ReportSubstitution extends Report {

    private final String from;

    private final String to;

    public ReportSubstitution(URI source, String from, String to) {
        super(source, "Substituce hodnot.");
        this.from = from;
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    @Override
    public List<Statement> asStatements(URI subject) {
        final ValueFactory valueFactory = ValueFactoryImpl.getInstance();
        final EntityBuilder entityBuilder = prepareEntityBuilder(subject, valueFactory);

        entityBuilder.property(RDF.TYPE, AddressMapperOntology.REPORT_SUBSTITUTE);
        entityBuilder.property(AddressMapperOntology.FROM, from);
        entityBuilder.property(AddressMapperOntology.TO, to);

        return entityBuilder.asStatements();
    }

}
