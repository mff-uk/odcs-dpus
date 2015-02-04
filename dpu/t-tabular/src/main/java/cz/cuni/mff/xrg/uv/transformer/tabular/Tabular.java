package cz.cuni.mff.xrg.uv.transformer.tabular;

import cz.cuni.mff.xrg.uv.transformer.tabular.xls.ParserXls;
import cz.cuni.mff.xrg.uv.transformer.tabular.dbf.ParserDbf;
import cz.cuni.mff.xrg.uv.transformer.tabular.csv.ParserCsv;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigHistory;
import cz.cuni.mff.xrg.uv.transformer.tabular.mapper.TableToRdf;
import cz.cuni.mff.xrg.uv.transformer.tabular.parser.*;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import org.openrdf.model.URI;

import cz.cuni.mff.xrg.uv.boost.dpu.advanced.AbstractDpu;
import cz.cuni.mff.xrg.uv.boost.dpu.context.ContextUtils;
import cz.cuni.mff.xrg.uv.boost.dpu.initialization.AutoInitializer;
import cz.cuni.mff.xrg.uv.boost.extensions.FaultTolerance;
import cz.cuni.mff.xrg.uv.boost.rdf.simple.WritableSimpleRdf;
import cz.cuni.mff.xrg.uv.utils.dataunit.DataUnitUtils;
import cz.cuni.mff.xrg.uv.utils.dataunit.rdf.RdfDataUnitUtils;

/**
 *
 * @author Škoda Petr
 */
@DPU.AsTransformer
public class Tabular extends AbstractDpu<TabularConfig_V2, TabularOntology> {

    @DataUnit.AsInput(name = "table")
    public FilesDataUnit inFilesTable;

    @DataUnit.AsOutput(name = "triplifiedTable")
    public WritableRDFDataUnit outRdfTables;

    @AutoInitializer.Init(param = "outRdfTables")
    public WritableSimpleRdf rdfTableWrap = null;

    @AutoInitializer.Init
    public FaultTolerance faultTolerance;

    public Tabular() {
        super(TabularVaadinDialog.class,
                ConfigHistory.history(TabularConfig_V1.class).addCurrent(TabularConfig_V2.class),
                TabularOntology.class);
    }

    @Override
    protected void innerExecute() throws DPUException, DataUnitException {
        // Prepare tabular convertor (tabular -> rdf).
        final TableToRdf tableToRdf = new TableToRdf(config.getTableToRdfConfig(), rdfTableWrap,
                rdfTableWrap.getValueFactory());
        // Prepare parser based on configuration.
        final Parser parser;
        switch (config.getTableType()) {
            case CSV:
                parser = new ParserCsv(config.getParserCsvConfig(), tableToRdf, ctx);
                break;
            case DBF:
                parser = new ParserDbf(config.getParserDbfConfig(), tableToRdf, ctx);
                break;
            case XLS:
                parser = new ParserXls(config.getParserXlsConfig(), tableToRdf, ctx);
                break;
            default:
                throw new DPUException("Unknown table file: " + config.getTableType());
        }
        // We eager load as there is bug with repository: https://openrdf.atlassian.net/browse/SES-2106
        final List<FilesDataUnit.Entry> files = new LinkedList<>();
        faultTolerance.execute(new FaultTolerance.Action() {

            @Override
            public void action() throws Exception {
                files.addAll(DataUnitUtils.getEntries(inFilesTable, FilesDataUnit.Entry.class));
            }
        });
        // Execute over files.
        for (final FilesDataUnit.Entry entry : files) {
            if (ctx.canceled()) {
                throw new DPUException("Execution cancelled!");
            }

            // Set output graph for each file.
            faultTolerance.execute(new FaultTolerance.Action() {
                @Override
                public void action() throws Exception {
                    rdfTableWrap.setOutput(RdfDataUnitUtils.addGraph(outRdfTables, entry.getSymbolicName()));
                }
            });
            ContextUtils.sendInfo(ctx, "Processing file: " + entry.getSymbolicName(),
                    "Source entry: %s", entry);
            // Process data.
            try {
                if (config.isUseTableSubject()) {
                    URI tableURI = rdfTableWrap.getValueFactory().createURI(entry.getFileURIString());
                    tableToRdf.setTableSubject(tableURI);
                    // Add info about symbolic name.
                    rdfTableWrap.add(tableURI, ctx.getOntology().get(TabularOntology.TABLE_SYMBOLIC_NAME),
                            rdfTableWrap.getValueFactory().createLiteral(entry.getSymbolicName()));
                }
                parser.parse(new File(java.net.URI.create(entry.getFileURIString())));
            } catch (ParseFailed ex) {
                throw new DPUException("File processing failed!", ex);
            }
        }
    }

}
