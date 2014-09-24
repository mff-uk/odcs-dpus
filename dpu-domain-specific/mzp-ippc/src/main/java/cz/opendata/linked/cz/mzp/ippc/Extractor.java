package cz.opendata.linked.cz.mzp.ippc;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.scraper.css_parser.utils.Cache;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.impl.SimpleRdfConfigurator;
import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;
import cz.cuni.mff.xrg.uv.boost.dpu.config.MasterConfigObject;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.OperationFailedException;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.SimpleRdfFactory;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.SimpleRdfWrite;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;

@DPU.AsExtractor
public class Extractor 
extends DpuAdvancedBase<ExtractorConfig> 
{

    @DataUnit.AsOutput(name = "output")
    public WritableRDFDataUnit output;

	@SimpleRdfConfigurator.Configure(dataUnitFieldName="output")
	public SimpleRdfWrite outputWrap;
    
    private static final Logger LOG = LoggerFactory.getLogger(DPU.class);

    public Extractor(){
        super(ExtractorConfig.class,AddonInitializer.create(new SimpleRdfConfigurator(Extractor.class)));
    }

    @Override
    public AbstractConfigDialog<MasterConfigObject> getConfigurationDialog() {        
        return new ExtractorDialog();
    }

    @Override
    protected void innerExecute() throws DPUException, OperationFailedException
    {
        Cache.setInterval(config.getInterval());
        Cache.setTimeout(config.getTimeout());
        Cache.setBaseDir(context.getUserDirectory() + "/cache/");
        Cache.rewriteCache = config.isRewriteCache();
        Cache.logger = LOG;

        try {
            Cache.setTrustAllCerts();
        } catch (Exception e) {
            LOG.error("Unexpected error when setting trust to all certificates.",e );
        }
        
        Parser s = new Parser();
        s.logger = LOG;
        s.context = context;
        s.outputDataUnit = outputWrap;
        s.valueFactory = outputWrap.getValueFactory();

        LOG.info("Starting extraction.");
        
            java.util.Date date = new java.util.Date();
            long start = date.getTime();
            
            CloseableHttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost("http://www.mzp.cz/www/ippc4.nsf/appliances.xsp");
            String viewid = null;
            try {
                CloseableHttpResponse response3 = httpclient.execute(httpPost); 
                HttpEntity entity3 = response3.getEntity();
                StringWriter writer = new StringWriter();
                viewid = Jsoup.parse(entity3.getContent(), "UTF-8", "http://www.mzp.cz/www/ippc4.nsf/appliances.xsp").getElementById("view:_id1__VUID").attr("value");
                System.out.println(viewid);
                
            } catch (IOException e1) {
                LOG.error(e1.getLocalizedMessage(),e1);
            }
            int i = 0;
            while (true) {
                i++;

                File currentFile = new File (context.getUserDirectory(), "listpage" + i + ".html");

                if (!currentFile.exists() || config.isRewriteCache() )
                {
                    CloseableHttpResponse response2 = null;
                    int attempt = 0;
                    while (attempt < config.getMaxattempts() && !context.canceled()) {
                        try {
                            List <NameValuePair> nvps = new ArrayList <NameValuePair>();
                            nvps.add(new BasicNameValuePair("$$ajaxid", "view:_id1:_id2:facetMiddle:tabPanel1:dataView1_OUTER_TABLE"));
                            nvps.add(new BasicNameValuePair("$$viewid", viewid));
                            nvps.add(new BasicNameValuePair("$$xspsubmitid", "view:_id1:_id2:facetMiddle:tabPanel1:dataView1:_id20:pager__Next"));
                            nvps.add(new BasicNameValuePair("$$xspexecid", "view:_id1:_id2:facetMiddle:tabPanel1:dataView1:_id20:pager"));
                            UrlEncodedFormEntity uefe = new UrlEncodedFormEntity(nvps, "UTF-8");
                            httpPost.setEntity(uefe);
                            
                            response2 = httpclient.execute(httpPost);
                            
                            HttpEntity entity2 = response2.getEntity();
                            Header[] headers = response2.getAllHeaders();
                            PrintWriter out = new PrintWriter(currentFile);
                            IOUtils.copy(entity2.getContent(), out, "UTF-8");
                            out.close();
                            EntityUtils.consume(entity2);
                            break;
                        } catch (UnsupportedEncodingException e1) {
                            LOG.error(e1.getLocalizedMessage(),e1);
                        } catch (ClientProtocolException e1) {
                            LOG.error(e1.getLocalizedMessage(),e1);
                        } catch (IOException e1) {
                            LOG.error(e1.getLocalizedMessage(),e1);
                        } finally {
                            try {
                                if (response2 != null) response2.close();
                            } catch (IOException e) {
                                LOG.error(e.getLocalizedMessage(),e);
                            }
                        }
                        
                        attempt++;
                        LOG.info("Warning (retrying) " + attempt);
                        try {
                            Thread.sleep(config.getInterval());
                        } catch (InterruptedException e1) {
                            LOG.warn(e1.getLocalizedMessage(), e1);
                        }
                        
                    }
                }
                
                Document doc = null;
                try {
                    doc = Jsoup.parse(currentFile, "UTF-8");
                } catch (IOException e1) {
                    LOG.error(e1.getLocalizedMessage(),e1);
                }
                for (Element e : doc.select("a.xspLinkViewColumn"))
                {
                    try {
                        s.parse(new URL("http://www.mzp.cz" + e.attr("href")), "docview");
                    } catch (MalformedURLException | InterruptedException e1) {
                        LOG.error("Parsing failed", e1);
                    }
                }
                if (doc.getElementById("view:_id1:_id2:facetMiddle:tabPanel1:dataView1:_id20:pager__Next__lnk") == null) break;
                        
                }
            
            java.util.Date date2 = new java.util.Date();
            long end = date2.getTime();
            
            context.sendMessage(DPUContext.MessageType.INFO, "Processed in " + (end-start) + "ms");
            
            LOG.info("Parsing done.");
    }

}
