package cz.cuni.mff.xrg.uv.transformer.xslt;

import java.io.File;
import java.io.StringReader;
import java.util.List;

import javax.xml.transform.stream.StreamSource;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.uv.boost.dpu.advanced.AbstractDpu;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigHistory;
import cz.cuni.mff.xrg.uv.boost.dpu.context.ContextUtils;
import cz.cuni.mff.xrg.uv.boost.dpu.initialization.AutoInitializer;
import cz.cuni.mff.xrg.uv.boost.extensions.FaultTolerance;
import cz.cuni.mff.xrg.uv.boost.extensions.RdfConfiguration;
import cz.cuni.mff.xrg.uv.utils.dataunit.DataUnitUtils;
import cz.cuni.mff.xrg.uv.utils.dataunit.files.FilesDataUnitUtils;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

@DPU.AsTransformer
public class Xslt extends AbstractDpu<XsltConfig_V2> {

    private static final Logger LOG = LoggerFactory.getLogger(Xslt.class);

    @RdfConfiguration.ContainsConfiguration
    @DataUnit.AsInput(name = "config", description = "Optional input for RDF configuration.", optional = true)
    public RDFDataUnit rdfConfig;

    @DataUnit.AsInput(name = "input", description = "Files to process with XSLT processor.")
    public FilesDataUnit filesInput;

    @DataUnit.AsOutput(name = "output", description = "Processed files. Only file contet has been changed.")
    public WritableFilesDataUnit filesOutput;

    @AutoInitializer.Init
    public FaultTolerance faultTolerance;

    @AutoInitializer.Init
    public RdfConfiguration _rdfConfiguration;

    public Xslt() {
        super(XsltVaadinDialog.class, ConfigHistory.noHistory(XsltConfig_V2.class), XsltTOntology.class);
    }

    @Override
    protected void innerExecute() throws DPUException {
        LOG.info("Properties size: {}", config.getFilesParameters().size());
        for (XsltConfig_V2.FileInformations info : config.getFilesParameters()) {
            LOG.info("File info for: {}", info.getSymbolicName());
            LOG.info("\t param size: {}", info.getParameters().size());
        }

        // Get and compile XSLT template.
        if (config.getXsltTemplate() == null || config.getXsltTemplate().isEmpty()) {
            throw new DPUException("No XSLT template available.");
        }
        final Processor proc = new Processor(false);
        final XsltCompiler compiler = proc.newXsltCompiler();
        final XsltExecutable executable;
        try {
            executable = compiler.compile(new StreamSource(new StringReader(config.getXsltTemplate())));
        } catch (SaxonApiException ex) {
            throw new DPUException("Cannot compile XSLT template.", ex);
        }
        ContextUtils.sendShortInfo(ctx, "Stylesheet was compiled successully");
        // Get files to iterate.
        final List<FilesDataUnit.Entry> files
                = faultTolerance.execute(new FaultTolerance.ActionReturn<List<FilesDataUnit.Entry>>() {

                    @Override
                    public List<FilesDataUnit.Entry> action() throws Exception {
                        return DataUnitUtils.getEntries(filesInput, FilesDataUnit.Entry.class);
                    }

                });
        int index = 0;
        int filesSuccessfulCounter = 0;
        for (final FilesDataUnit.Entry source : files) {
            LOG.info("Processing {}/{}", index, files.size());
            // Get URI of input file.
            final File sourceFile = faultTolerance.execute(new FaultTolerance.ActionReturn<File>() {

                @Override
                public File action() throws Exception {
                    return FilesDataUnitUtils.asFile(source);
                }
            });
            final String symbolicName = faultTolerance.execute(new FaultTolerance.ActionReturn<String>() {

                @Override
                public String action() throws Exception {
                    return source.getSymbolicName();
                }
            });
            // Create output entity.
            final FilesDataUnit.Entry target
                    = faultTolerance.execute(new FaultTolerance.ActionReturn<FilesDataUnit.Entry>() {

                        @Override
                        public FilesDataUnit.Entry action() throws Exception {                            
                            // Create a file, possibli with wrong VirtualPath.
                            return FilesDataUnitUtils.createFile(filesOutput, symbolicName);
                        }

                    });
            // Copy metadata ie. also VirtualPath, that may be wrongly set on line before.

            // TODO Petr Copy metadata?

            final File targetFile = faultTolerance.execute(new FaultTolerance.ActionReturn<File>() {

                @Override
                public File action() throws Exception {
                    return FilesDataUnitUtils.asFile(target);
                }
            });
            // Transform file.
            try {
                LOG.debug("Memory used: {}M", String.valueOf((Runtime.getRuntime().totalMemory()
                        - Runtime.getRuntime().freeMemory()) / 1024 / 1024));
                // Prepare classes and parameters.
                final Serializer out = new Serializer(targetFile);
                final XsltTransformer transformer = executable.load();

                LOG.debug("File: {}", source);
                LOG.debug("\tsymbolic name: {}", symbolicName);

                for (XsltConfig_V2.Parameter parameter : config.getFilesParameters(symbolicName)) {
                    LOG.debug("\t {} : {}", parameter.getKey(), parameter.getValue());
                    transformer.setParameter(new QName(parameter.getKey()), new XdmAtomicValue(parameter.getValue()));
                }
                transformer.setSource(new StreamSource(sourceFile));
                transformer.setDestination(out);
                transformer.transform();
                transformer.getUnderlyingController().clearDocumentPool();                
                LOG.debug("Memory used: {}M", String.valueOf((Runtime.getRuntime().totalMemory()
                        - Runtime.getRuntime().freeMemory()) / 1024 / 1024));
                ++filesSuccessfulCounter;
            } catch (SaxonApiException ex) {
                ContextUtils.sendError(ctx, "Error processig file", ex,
                        "File: %s</br>File location: %s", symbolicName, sourceFile);
                if (config.isFailOnError()) {
                    throw new DPUException("Error processig file");
                }
            }
            // Update indexes.
            ++index;
            if (ctx.canceled()) {
                throw new DPUException("Interrupted.");
            }
        }
        if (filesSuccessfulCounter == index) {
            ContextUtils.sendShortInfo(ctx, "Processed %d/%d", filesSuccessfulCounter, index);
        } else {
            ContextUtils.sendShortWarn(ctx, "Processed %d/%d", filesSuccessfulCounter, index);
        }
    }

}
