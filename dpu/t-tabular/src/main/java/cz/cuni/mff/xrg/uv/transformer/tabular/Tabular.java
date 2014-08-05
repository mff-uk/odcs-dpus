package cz.cuni.mff.xrg.uv.transformer.tabular;

import cz.cuni.mff.xrg.uv.boost.dpu.simple.DpuSimpleBase;
import cz.cuni.mff.xrg.uv.boost.dpu.utils.CloseAutoCloseable;
import cz.cuni.mff.xrg.uv.rdf.simple.AddPolicy;
import cz.cuni.mff.xrg.uv.rdf.simple.OperationFailedException;
import cz.cuni.mff.xrg.uv.rdf.simple.SimpleRdfFactory;
import cz.cuni.mff.xrg.uv.rdf.simple.SimpleRdfWrite;
import cz.cuni.mff.xrg.uv.transformer.tabular.mapper.TableToRdf;
import cz.cuni.mff.xrg.uv.transformer.tabular.parser.*;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DPU.AsTransformer
public class Tabular extends DpuSimpleBase<TabularConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(Tabular.class);

    @DataUnit.AsInput(name = "table")
    public FilesDataUnit inFilesTable;

    @DataUnit.AsOutput(name = "triplifiedTable")
    public WritableRDFDataUnit outRdfTables;

    private SimpleRdfWrite rdfTableWrap = null;

    private final CloseAutoCloseable closeClosabel = new CloseAutoCloseable();

    public Tabular() {
        super(TabularConfig_V1.class);
    }

    @Override
    public AbstractConfigDialog<TabularConfig_V1> getConfigurationDialog() {
        return new TabularVaadinDialog();
    }

    @Override
    protected void init() throws DPUException, DataUnitException {
        super.init();
        rdfTableWrap = SimpleRdfFactory.create(outRdfTables, context);
        rdfTableWrap.setPolicy(AddPolicy.BUFFERED);
        TabularOntology.init(rdfTableWrap.getValueFactory());
    }

    @Override
    protected void innerExecute() throws DPUException, DataUnitException {
        //
        // prepare tabular convertor
        //
        final TableToRdf tableToRdf = new TableToRdf(
                config.getTableToRdfConfig(),
                rdfTableWrap,
                rdfTableWrap.getValueFactory());
        //
        // prepare parser based on type
        //
        final Parser parser;
        if (config.getTableType() == ParserType.CSV) {
            parser = new ParserCsv(config.getParserCsvConfig(),
                    tableToRdf, context);
        } else if (config.getTableType() == ParserType.DBF) {
            parser = new ParserDbf(config.getParserDbfConfig(),
                    tableToRdf, context);
        } else {
            context.sendMessage(DPUContext.MessageType.ERROR,
                    "Unknown table file: " + config.getTableType());
            return;
        }
        //
        // execute ever files
        //
        final FilesDataUnit.Iteration iteration = inFilesTable.getIteration();
        closeClosabel.add(iteration);
        while(iteration.hasNext()) {
            final FilesDataUnit.Entry entry = iteration.next();
            // set output graph
            rdfTableWrap.setOutputGraph(entry.getSymbolicName());
            // output data
            try {
                parser.parse(new File(
                        java.net.URI.create(entry.getFileURIString())));
            } catch(ParseFailed ex) {
                context.sendMessage(DPUContext.MessageType.ERROR,
                        "Failed to convert file",
                        "File:" + entry.getSymbolicName(), ex);
                break;
            }
        }
    }

    @Override
    protected void close() {
        super.close();
        if (rdfTableWrap != null) {
            try {
                rdfTableWrap.flushBuffer();
            } catch (OperationFailedException ex) {
                context.sendMessage(DPUContext.MessageType.ERROR,
                        "Can't save data into rdf.", "", ex);
            }
        }
        closeClosabel.closeAll(context);
    }

}
