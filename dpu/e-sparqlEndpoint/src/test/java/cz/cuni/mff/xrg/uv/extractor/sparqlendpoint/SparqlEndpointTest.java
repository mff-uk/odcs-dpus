package cz.cuni.mff.xrg.uv.extractor.sparqlendpoint;

import java.util.Map;
import org.junit.Test;
import org.mockito.Mockito;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryConnection;

import cz.cuni.mff.xrg.uv.service.external.ExternalServicesFactory;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.DataUnitUtils;
import eu.unifiedviews.helpers.dpu.exec.ExecContext;
import eu.unifiedviews.helpers.dpu.exec.UserExecContext;
import eu.unifiedviews.helpers.dpu.rdf.sparql.SparqlUtils;

/**
 *
 * @author Škoda Petr
 */
public class SparqlEndpointTest {

    //@Test
    public void executeQueryRuianConcepts() throws Exception {
        UserExecContext ctx = Mockito.mock(UserExecContext.class);
        Mockito.when(ctx.getExecMasterContext()).thenReturn(Mockito.mock(ExecContext.class));

        final RDFDataUnit remote = ExternalServicesFactory.remoteRdf(ctx,
                "http://ruian.linked.opendata.cz/sparql", new URI[0]);

        final RepositoryConnection connection = remote.getConnection();

        SparqlUtils.SparqlSelectObject query = SparqlUtils.createSelect(
                "select distinct ?Concept where {[] a ?Concept} LIMIT 100",
                DataUnitUtils.getEntries(remote, RDFDataUnit.Entry.class));

        SparqlUtils.QueryResultCollector collector = new SparqlUtils.QueryResultCollector();

        SparqlUtils.execute(connection, ctx, query, collector);

        for (Map<String, Value> item : collector.getResults()) {
            for (String key : item.keySet()) {
                System.out.println(key + " > " + item.get(key).stringValue());
            }
        }

        connection.close();
    }

    //@Test
    public void executeInternal_EH13() throws Exception {
        UserExecContext ctx = Mockito.mock(UserExecContext.class);
        Mockito.when(ctx.getExecMasterContext()).thenReturn(Mockito.mock(ExecContext.class));

        final RDFDataUnit remote = ExternalServicesFactory.remoteRdf(ctx,
                "http://internal.opendata.cz:8890/sparql", new URI[0]);

        final RepositoryConnection connection = remote.getConnection();

        GraphQuery query = connection.prepareGraphQuery(QueryLanguage.SPARQL,
                "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n"
                + "\n"
                + "CONSTRUCT {\n"
                + "  ?mpp skos:notation ?notation .\n"
                + "}\n"
                + "FROM <http://linked.opendata.cz/resource/dataset/sukl/drug-ingredients>\n"
                + "FROM <http://linked.opendata.cz/resource/dataset/sukl/drugs>\n"
                + "WHERE {\n"
                + "  ?mpp a <http://linked.opendata.cz/ontology/sukl/MedicinalProductPackaging> ;\n"
                + "    skos:notation ?notation .\n"
                + "  FILTER NOT EXISTS {\n"
                + "    ?mpp <http://linked.opendata.cz/ontology/sukl/hasActiveIngredient> ?ai .\n"
                + "  }\n"
                + "}");

        GraphQueryResult result = query.evaluate();
        Integer counter = 0;
        while (result.hasNext()) {
            Statement s = result.next();
            System.out.println(" " + s.getSubject() + " " + s.getPredicate() + " " + s.getObject());
            ++counter;
        }
        System.out.println("> " + counter.toString());

        connection.close();
    }

    //@Test
    public void executeInternal_EH13_all() throws Exception {
        UserExecContext ctx = Mockito.mock(UserExecContext.class);
        Mockito.when(ctx.getExecMasterContext()).thenReturn(Mockito.mock(ExecContext.class));

        final RDFDataUnit remote = ExternalServicesFactory.remoteRdf(ctx,
                "http://internal.opendata.cz:8890/sparql", new URI[0]);

        final RepositoryConnection connection = remote.getConnection();

        GraphQuery query = connection.prepareGraphQuery(QueryLanguage.SPARQL,
                "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n"
                + "\n"
                + "CONSTRUCT {\n"
                + "  ?s ?p ?o \n"
                + "}\n"
                + "FROM <http://linked.opendata.cz/resource/dataset/sukl/drugs>\n"
                + "WHERE {\n"
                + "  ?s ?p ?o .\n"
                + "}");

        GraphQueryResult result = query.evaluate();
        Integer counter = 0;
        while (result.hasNext()) {
            Statement s = result.next();
            ++counter;
            if (counter % 10000 == 0) {
                System.out.println("> " + counter.toString());
            }
        }
        System.out.println("Total: " + counter.toString());

        connection.close();
    }

    //@Test
    public void executeInternal_MFCR_FUSEKI() throws Exception {
        UserExecContext ctx = Mockito.mock(UserExecContext.class);
        Mockito.when(ctx.getExecMasterContext()).thenReturn(Mockito.mock(ExecContext.class));

        final RDFDataUnit remote = ExternalServicesFactory.remoteRdf(ctx,
                "http://xrg15.projekty.ms.mff.cuni.cz:3330/mfcr/query", new URI[0]);

        final RepositoryConnection connection = remote.getConnection();

        GraphQuery query = connection.prepareGraphQuery(QueryLanguage.SPARQL,
                "construct {?s ?p ?o} where {?s ?p ?o}");

        GraphQueryResult result = query.evaluate();
        Integer counter = 0;
        while (result.hasNext()) {
            Statement s = result.next();
            ++counter;
            if (counter % 10000 == 0) {
                System.out.println("> " + counter.toString());
            }
        }
        System.out.println("Total: " + counter.toString());

        connection.close();
    }

}
