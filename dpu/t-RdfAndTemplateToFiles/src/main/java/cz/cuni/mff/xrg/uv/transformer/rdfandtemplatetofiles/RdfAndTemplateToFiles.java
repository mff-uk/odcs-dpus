package cz.cuni.mff.xrg.uv.transformer.rdfandtemplatetofiles;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;

import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.uv.transformer.rdfandtemplatetofiles.template.CantCreateTemplate;
import cz.cuni.mff.xrg.uv.transformer.rdfandtemplatetofiles.template.RenderContext;
import cz.cuni.mff.xrg.uv.transformer.rdfandtemplatetofiles.template.Template;
import cz.cuni.mff.xrg.uv.transformer.rdfandtemplatetofiles.template.TemplateFactory;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultToleranceUtils;
import eu.unifiedviews.helpers.dpu.extension.files.simple.WritableSimpleFiles;
import eu.unifiedviews.helpers.dpu.rdf.sparql.SparqlUtils;

/**
 * Main data processing unit class.
 *
 * @author Petr Škoda
 */
@DPU.AsTransformer
public class RdfAndTemplateToFiles extends AbstractDpu<RdfAndTemplateToFilesConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(RdfAndTemplateToFiles.class);

    private static final String SELECT_DOCUMENTS = "SELECT ?document ?fileName WHERE {\n"
            + "  ?document a <" + RdfAndTemplateToFilesVocabulary.DOCUMENT + "> ;\n"
            + "    <" + RdfAndTemplateToFilesVocabulary.FILENAME + "> ?fileName .\n"
            + "}";

    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;

    @DataUnit.AsInput(name = "input")
    public RDFDataUnit input;

    @DataUnit.AsOutput(name = "output")
    public FilesDataUnit outputFiles;

    @ExtensionInitializer.Init(param = "outputFiles")
    public WritableSimpleFiles output;

    public RdfAndTemplateToFiles() {
        super(RdfAndTemplateToFilesVaadinDialog.class, ConfigHistory.noHistory(
                RdfAndTemplateToFilesConfig_V1.class));
    }

    @Override
    protected void innerExecute() throws DPUException {
        // Prepare template.
        final Template template = TemplateFactory.create(config.getTemplate());
        // Get graphs.
        final List<RDFDataUnit.Entry> graphs = FaultToleranceUtils.getEntries(faultTolerance, input,
                RDFDataUnit.Entry.class);
        int graphCounter = 0;
        for (final RDFDataUnit.Entry graph : graphs) {
            LOG.info("Processing {}/{}", ++graphCounter, graphs.size());
            // Get documents to process.
            final SparqlUtils.QueryResultCollector result = new SparqlUtils.QueryResultCollector();
            faultTolerance.execute(input, new FaultTolerance.ConnectionAction() {

                @Override
                public void action(RepositoryConnection connection) throws Exception {
                    result.prepare();
                    SparqlUtils.execute(connection,
                            ctx,
                            SparqlUtils.createSelect(SELECT_DOCUMENTS, Arrays.asList(graph)),
                            result);
                }
            });
            final URI graphUri = FaultToleranceUtils.asGraph(faultTolerance, graph);
            final RenderContext context = new RenderContext(input, graphUri, faultTolerance);
            int fileCounter = 0;
            for (Map<String, Value> document : result.getResults()) {
                LOG.info("Processing {}/{} - {}/{} : {}", graphCounter, graphs.size(), ++fileCounter,
                        result.getResults().size(), document.get("document"));
                if (document.get("document") instanceof URI) {
                    // Ok we can proceed.
                } else {
                    LOG.warn("Subject '{}' is not URI!", document.get("document"));
                    continue;
                }
                // Create file.
                final File newFile = output.create(document.get("fileName").stringValue());
                try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newFile),
                        "utf-8"))) {
                    // Prepare context.
                    context.setWriter(writer);
                    // Write.
                    template.render(context, (URI) document.get("document"));
                } catch (IOException ex) {
                    throw ContextUtils.dpuException(ctx, ex, "Can't write a file!");
                } catch (CantCreateTemplate ex) {
                    if (config.isSoftFail()) {
                        ContextUtils.sendWarn(ctx, "Can't create document", ex, "Subject: {0}",
                                document.get("document").stringValue());
                    } else {
                        throw ContextUtils.dpuException(ctx, ex, "Can't create document!");
                    }
                }
            }
        }
    }

}
