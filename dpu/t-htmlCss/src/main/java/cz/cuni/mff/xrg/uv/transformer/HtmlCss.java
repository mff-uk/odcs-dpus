package cz.cuni.mff.xrg.uv.transformer;

import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.impl.CloseCloseable;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.impl.SimpleRdfConfigurator;
import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;
import cz.cuni.mff.xrg.uv.boost.dpu.config.MasterConfigObject;
import cz.cuni.mff.xrg.uv.boost.dpu.utils.SendMessage;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.OperationFailedException;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.SimpleRdfWrite;
import cz.cuni.mff.xrg.uv.utils.dataunit.files.CreateFile;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import java.io.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Škoda Petr
 */
@DPU.AsTransformer
public class HtmlCss extends DpuAdvancedBase<HtmlCssConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(HtmlCss.class);

    @DataUnit.AsInput(name = "html")
    public FilesDataUnit inFilesHtml;

    @DataUnit.AsOutput(name = "rdfData")
    public WritableRDFDataUnit outRdfData;

    @DataUnit.AsOutput(name = "files")
    public WritableFilesDataUnit outFilesData;

    @SimpleRdfConfigurator.Configure(dataUnitFieldName = "outRdfData")
    public SimpleRdfWrite outData;

    public HtmlCss() {
        super(HtmlCssConfig_V1.class,
                AddonInitializer.create(new SimpleRdfConfigurator(HtmlCss.class), new CloseCloseable()));
    }

    @Override
    protected void innerExecute() throws DPUException {

        final FilesDataUnit.Iteration iter;
        try {
            iter = inFilesHtml.getIteration();
        } catch (DataUnitException ex) {
            SendMessage.sendMessage(context, ex);
            return;
        }
        getAddon(CloseCloseable.class).add(iter);

        final ValueFactory valueFactory;
        try {
            valueFactory = outData.getValueFactory();
        } catch (OperationFailedException ex) {
            SendMessage.sendMessage(context, ex);
            return;
        }
        // parse given files
        try {
            while (iter.hasNext()) {
                final FilesDataUnit.Entry entry = iter.next();
                LOG.info("Parsing file: {}", entry);
                Document doc = Jsoup.parse(new File(java.net.URI.create(entry.getFileURIString())), null);
                outData.setOutputGraph(entry.getFileURIString());
                parse(valueFactory, doc);
            }
        } catch (OperationFailedException ex) {
            SendMessage.sendMessage(context, ex);
        } catch (DataUnitException ex) {
            SendMessage.sendMessage(context, ex);
        } catch (IOException ex) {
            context.sendMessage(DPUContext.MessageType.ERROR, "Can't parse given document.", "", ex);
        }
    }

    @Override
    public AbstractConfigDialog<MasterConfigObject> getConfigurationDialog() {
        return new HtmlCssVaadinDialog();
    }

    /**
     * Parse given document.
     *
     * @param fileUriStr
     * @param doc
     */
    private void parse(ValueFactory valueFactory, Document doc)
            throws OperationFailedException, DataUnitException, IOException {
        int index = 0;
        Integer tableIndex = 0;
        for (HtmlCssConfig_V1.Query q : config.getQueries()) {
            LOG.trace("query: {}", q.getQuery());
            final URI subject = valueFactory.createURI("http://localhost/temp/" + Integer.toString(index));            
            final URI predicate = valueFactory.createURI(q.getPredicate());
            final Elements elements = doc.select(q.getQuery());
            LOG.trace("\tresult size: {}", elements.size());
            for (Element element : elements) {
                switch (q.getType()) {
                    case HTML:
                        outData.add(subject, predicate, valueFactory.createLiteral(element.html()));
                        break;
                    case TABLE_HTML:
                    case TABLE_LINKS:
                    case TABLE_TEXT:
                        parseTable(valueFactory, subject, predicate, element, tableIndex++, q.getType());
                        break;
                    case TEXT:
                        final String value;
                        if (q.getAttrName() == null) {
                            value = element.text();
                        } else {
                            value = element.attr(q.getAttrName());
                        }
                        outData.add(subject, predicate, valueFactory.createLiteral(value));
                        break;
                }
            }
        }
    }

    /**
     * Process given values as a table. Given value is converted into csv and saved into file.
     *
     * @param valueFactory
     * @param subject
     * @param predicate
     * @param element
     * @param index
     * @param type Table type.
     */
    private void parseTable(ValueFactory valueFactory, URI subject, URI predicate, Element elementTable,
            Integer index, HtmlCssConfig_V1.ElementType type)
            throws OperationFailedException, DataUnitException, IOException {
        LOG.trace("parseTable(,{},{},,{})", subject, predicate, index);
        final URI fileURI = valueFactory.createURI("http://localhost/temp/resource/table/"
                + index.toString());
        outData.add(subject, predicate, fileURI);
        // parse value and create file
        File tableFile = CreateFile.createFile(outFilesData, fileURI.stringValue());
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(tableFile), "UTF-8"))) {
            final Elements elements = elementTable.select("tr");

            int firstLineSize = -1;
            for (Element line : elements) {
                final Elements cells = line.select("td,th");
                boolean isFirst = true;
                int lineSize = 0;
                for (Element cell : cells) {
                    // write separator
                    if (isFirst) {
                        isFirst = false;
                    } else {
                        writer.write(",");
                    }

                    // get value based on type
                    String value;
                    switch (type) {
                        case TABLE_HTML:
                            value = cell.html();
                            break;
                        case TABLE_LINKS:
                            if (cell.childNodes().size() == 1 && cell.childNodes().get(0).hasAttr("href")) {
                                Element single = cell.child(0);
                                value = single.attr("href");
                            } else {
                                value = cell.text();
                            }
                            break;
                        default:
                            value = cell.text();
                            break;
                    }

                    // get value and espace "
                    value = value.replaceAll("\"", "\\\"");

                    writer.write("\"");
                    writer.write(value);
                    writer.write("\"");
                    lineSize++;
                }
                // set line size
                if (firstLineSize == -1) {
                    firstLineSize = lineSize;
                }
                // add cells to fit the firstLineSize
                for (int i = lineSize; i < firstLineSize; ++i) {
                    writer.write(",\"\"");
                }
                writer.newLine();
            }
        }
    }

}
