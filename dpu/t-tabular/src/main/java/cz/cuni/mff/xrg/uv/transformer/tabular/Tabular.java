package cz.cuni.mff.xrg.uv.transformer.tabular;

import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.impl.CloseCloseable;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.impl.SimpleRdfConfigurator;
import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigHistory;
import cz.cuni.mff.xrg.uv.boost.dpu.config.MasterConfigObject;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.AddPolicy;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.OperationFailedException;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.SimpleRdfFactory;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.SimpleRdfWrite;
import cz.cuni.mff.xrg.uv.transformer.tabular.mapper.TableToRdf;
import cz.cuni.mff.xrg.uv.transformer.tabular.parser.*;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.FilesDataUnit.Entry;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.fileshelper.FilesHelper;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import java.io.File;
import java.util.Iterator;
import java.util.Set;
import org.openrdf.model.URI;

/**
 *
 * @author Škoda Petr
 */
@DPU.AsTransformer
public class Tabular extends DpuAdvancedBase<TabularConfig_V2> {


    @DataUnit.AsInput(name = "table")
    public FilesDataUnit inFilesTable;

    @DataUnit.AsOutput(name = "triplifiedTable")
    public WritableRDFDataUnit outRdfTables;

    @SimpleRdfConfigurator.Configure(dataUnitFieldName = "outRdfTables")
    public SimpleRdfWrite rdfTableWrap = null;

    public Tabular() {
        super(ConfigHistory.create(TabularConfig_V1.class).addCurrent(TabularConfig_V2.class),
                AddonInitializer.create(new CloseCloseable(), new SimpleRdfConfigurator(Tabular.class)));
    }

    @Override
    public AbstractConfigDialog<MasterConfigObject> getConfigurationDialog() {
        return new TabularVaadinDialog();
    }

    @Override
    protected void innerInit() throws DataUnitException {
        
        rdfTableWrap = SimpleRdfFactory.create(outRdfTables, context);
        rdfTableWrap.setPolicy(AddPolicy.BUFFERED);
        TabularOntology.init(rdfTableWrap.getValueFactory());
    }

    @Override
    protected void innerExecute() throws DPUException, OperationFailedException, DataUnitException {
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
        switch(config.getTableType()) {
            case CSV:
                parser = new ParserCsv(config.getParserCsvConfig(),
                    tableToRdf, context);
                break;
            case DBF:
                parser = new ParserDbf(config.getParserDbfConfig(),
                    tableToRdf, context);
                break;
            case XLS:
                parser = new ParserXls(config.getParserXlsConfig(),
                    tableToRdf, context);
                break;
            default:
                context.sendMessage(DPUContext.MessageType.ERROR,
                    "Unknown table file: " + config.getTableType());
                return;
        }
        //
        // execute ever files
        //
//        final FilesDataUnit.Iteration iteration = inFilesTable.getIteration();
//        getAddon(CloseCloseable.class).add(iteration);

//        if (!iteration.hasNext()) {
//            context.sendMessage(DPUContext.MessageType.ERROR, "No input files!");
//            return;
//        }

        // fix for problem with repository
        Set<Entry> filesInputSet = FilesHelper.getFiles(inFilesTable);
        Iterator<Entry> iteration = filesInputSet.iterator();

        while(iteration.hasNext() && !context.canceled()) {
            final FilesDataUnit.Entry entry = iteration.next();
            // set output graph
            rdfTableWrap.setOutputGraph(entry.getSymbolicName());
            context.sendMessage(DPUContext.MessageType.INFO,
                    "Processing file: '" + entry.getSymbolicName() + "'");
            // output data
            try {
                if (config.isUseTableSubject()) {
                    URI tableURI = rdfTableWrap.getValueFactory().createURI(
                            entry.getFileURIString());
                    tableToRdf.setTableSubject(tableURI);
                    // add info about symbolic name
                    rdfTableWrap.add(tableURI,
                            TabularOntology.URI_TABLE_SYMBOLIC_NAME,
                            rdfTableWrap.getValueFactory().createLiteral(
                                    entry.getSymbolicName()));
                }

                parser.parse(new File(java.net.URI.create(
                        entry.getFileURIString())));
            } catch(ParseFailed ex) {
                context.sendMessage(DPUContext.MessageType.ERROR,
                        "Failed to convert file",
                        "File:" + entry.getSymbolicName(), ex);
                break;
            }
        }
    }

    @Override
    protected void innerCleanUp() {
        if (rdfTableWrap != null) {
            try {
                rdfTableWrap.flushBuffer();
            } catch (OperationFailedException ex) {
                context.sendMessage(DPUContext.MessageType.ERROR,
                        "Can't save data into rdf.", "", ex);
            }
        }
    }
}
