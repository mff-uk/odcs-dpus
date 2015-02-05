package cz.cuni.mff.xrg.uv.transformer.tabular.mapper;

import cz.cuni.mff.xrg.uv.transformer.tabular.TabularOntology;
import cz.cuni.mff.xrg.uv.transformer.tabular.column.ValueGenerator;
import java.util.List;
import java.util.Map;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.uv.boost.dpu.context.UserContext;
import cz.cuni.mff.xrg.uv.boost.ontology.OntologyDefinition;
import cz.cuni.mff.xrg.uv.boost.ontology.OntologyHolder;
import cz.cuni.mff.xrg.uv.boost.rdf.simple.WritableSimpleRdf;
import cz.cuni.mff.xrg.uv.boost.serialization.rdf.SimpleRdfException;
import eu.unifiedviews.dpu.DPUException;

/**
 * Parse table data into rdf. Before usage this class must be configured by
 * {@link TableToRdfConfigurator}.
 *
 * @author Škoda Petr
 */
public class TableToRdf {

    private static final Logger LOG = LoggerFactory.getLogger(TableToRdf.class);

    /**
     * Data output.
     */
    final WritableSimpleRdf outRdf;

    final ValueFactory valueFactory;

    final TableToRdfConfig config;

    ValueGenerator[] infoMap = null;

    ValueGenerator keyColumn = null;

    String baseUri = null;

    Map<String, Integer> nameToIndex = null;

    URI rowClass = null;

    private final URI typeUri;

    URI tableSubject = null;

    boolean tableInfoGenerated = false;

    public TableToRdf(TableToRdfConfig config, WritableSimpleRdf outRdf, ValueFactory valueFactory) {
        this.config = config;
        this.outRdf = outRdf;
        this.valueFactory = valueFactory;
        this.typeUri = valueFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
    }

    public void paserRow(List<Object> row, int rowNumber, OntologyHolder ontology)
            throws SimpleRdfException, DPUException {
        if (row.size() < nameToIndex.size()) {
            LOG.warn("Row is smaller ({} instead of {}) - ignore.",
                    row.size(), nameToIndex.size());
            return;
        }
        // Get subject - key.
        final URI subj = prepareUri(row, rowNumber);
        if (subj == null) {
            LOG.error("Row ({}) has null key, row skipped.", rowNumber);
        }
        // Parse the line, based on configuration.
        for (ValueGenerator item : infoMap) {
            final URI predicate = item.getUri();
            final Value value = item.generateValue(row, valueFactory);
            if (value == null) {
                if (config.ignoreBlankCells) {
                    // Ignore blacnk cell.
                } else {
                    // Insert blank cell URI.
                    outRdf.add(subj, predicate, ontology.get(TabularOntology.BLANK_CElL));
                }
            } else {
                // insert value
                outRdf.add(subj, predicate, value);
            }
        }
        // Add row data - number, class, connection to table.
        if (config.generateRowTriple) {
            outRdf.add(subj, ontology.get(TabularOntology.ROW_NUMBER), valueFactory.createLiteral(rowNumber));
        }
        if (rowClass != null) {
            outRdf.add(subj, typeUri, rowClass);
        }
        if (tableSubject != null) {
            outRdf.add(tableSubject, ontology.get(TabularOntology.TABLE_HAS_ROW), subj);
        }
        // Add table statistict only for the first time.
        if (!tableInfoGenerated && tableSubject != null) {
            tableInfoGenerated = true;
            if (config.generateTableClass) {
                outRdf.add(tableSubject, 
                        ontology.get(TabularOntology.RDF_A_PREDICATE),
                        ontology.get(TabularOntology.TABLE_CLASS));
            }
        }
    }

    /**
     * Set subject that will be used as table subject.
     *
     * @param newTableSubject Null to turn this functionality off.
     */
    public void setTableSubject(URI newTableSubject) {
        tableSubject = newTableSubject;
        tableInfoGenerated = false;
    }

    /**
     * Return key for given row.
     *
     * @param row
     * @param rowNumber
     * @return
     */
    protected URI prepareUri(List<Object> row, int rowNumber) {
        if (keyColumn == null) {
            return valueFactory.createURI(baseUri + Integer.toString(rowNumber));
        } else {
            return (URI)keyColumn.generateValue(row, valueFactory);
        }
    }

}
