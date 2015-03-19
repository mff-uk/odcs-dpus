package cz.cuni.mff.xrg.uv.addressmapper.facades;

import cz.cuni.mff.xrg.uv.addressmapper.objects.PostalAddress;
import java.util.List;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;

import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultToleranceUtils;
import eu.unifiedviews.helpers.dpu.serialization.rdf.SerializationRdf;
import eu.unifiedviews.helpers.dpu.serialization.rdf.SerializationRdfFactory;

/**
 *
 * @author Škoda Petr
 */
public class SchemaFacade {

    private final SerializationRdf serialization = SerializationRdfFactory.rdfSimple();

    private final FaultTolerance faultTolerace;

    private final RDFDataUnit input;

    private final List<RDFDataUnit.Entry> entries;

    public SchemaFacade(FaultTolerance faultTolerace, RDFDataUnit input) throws DPUException {
        this.faultTolerace = faultTolerace;
        this.input = input;
        // Prepare context.
        entries = FaultToleranceUtils.getEntries(faultTolerace, input, RDFDataUnit.Entry.class);
    }

    /**
     * Load a {@link PostalAddress} entity for given subject.
     *
     * @param entityUri
     * @return
     * @throws DPUException
     */
    public PostalAddress load(final URI entityUri) throws DPUException {
        final PostalAddress address = new PostalAddress(entityUri);
        faultTolerace.execute(input, new FaultTolerance.ConnectionAction() {
            
            @Override
            public void action(RepositoryConnection connection) throws Exception {
                serialization.convert(connection, entityUri, entries, address, null);
            }
        });
        return address;
    }

}
