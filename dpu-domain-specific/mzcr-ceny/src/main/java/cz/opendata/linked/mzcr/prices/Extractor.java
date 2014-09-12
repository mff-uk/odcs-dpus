package cz.opendata.linked.mzcr.prices;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.openrdf.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.css_parser.utils.Cache;
import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import cz.cuni.mff.xrg.uv.boost.dpu.config.MasterConfigObject;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.AddPolicy;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.SimpleRdfFactory;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.SimpleRdfRead;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.SimpleRdfWrite;

@DPU.AsExtractor
public class Extractor 
extends DpuAdvancedBase<ExtractorConfig> 
{

    @DataUnit.AsOutput(name = "output")
    public WritableRDFDataUnit outputDataUnit;
    
    @DataUnit.AsInput(name = "input")
    public RDFDataUnit inputDataUnit;

    private static final Logger LOG = LoggerFactory.getLogger(Extractor.class);

    public Extractor(){
        super(ExtractorConfig.class,AddonInitializer.noAddons());
    }

    @Override
    public AbstractConfigDialog<MasterConfigObject> getConfigurationDialog() {        
        return new ExtractorDialog();
    }

    @Override
    protected void innerExecute() throws DPUException, DataUnitException
    {
        final SimpleRdfRead inputWrap = SimpleRdfFactory.create(inputDataUnit, context);
        final SimpleRdfWrite outputWrap = SimpleRdfFactory.create(outputDataUnit, context);
        outputWrap.setPolicy(AddPolicy.BUFFERED);
        
        // vytvorime si parser
        Cache.setInterval(config.getInterval());
        Cache.setTimeout(config.getTimeout());
        Cache.setBaseDir(context.getUserDirectory() + "/cache/");
        Cache.rewriteCache = config.isRewriteCache();
        Cache.logger = LOG;
        
        Parser s = new Parser();
        s.logger = LOG;
        s.context = context;
        s.output = outputWrap;
        
        LOG.info("Starting extraction.");
        
        int lines = 0;
        java.util.Date date = new java.util.Date();
        long start = date.getTime();
        
        List<Statement> statements = inputWrap.getStatements();
        if (!statements.isEmpty())
        {
            int total = statements.size();
            
            URL notationPredicate;
            try {
                notationPredicate = new URL("http://www.w3.org/2004/02/skos/core#notation");
                for (Statement stmt : statements)
                {
                    if (context.canceled())
                    {
                        LOG.error("Interrupted");
                        break;
                    }
                    if (stmt.getPredicate().toString().equals(notationPredicate.toString()))
                    {
                        lines++;
                        LOG.debug("Parsing " + lines + "/" + total);
                        s.parse(Cache.getDocument(new URL("http://mzcr.cz/LekyNehrazene.aspx?naz=" + stmt.getObject().stringValue()), 10000), "tab");
                    }
                }
            } catch (MalformedURLException e) {
                LOG.error("Unexpected malformed URL of ODCS textValue predicate");
            } catch (IOException e) {
                LOG.error("IOException", e);
            } catch (InterruptedException e) {
                
            }
        }
        
        outputWrap.flushBuffer();
        
        java.util.Date date2 = new java.util.Date();
        long end = date2.getTime();
        LOG.info("Processed " + lines + " in " + (end-start) + "ms");

    }

}
