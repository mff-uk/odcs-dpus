package cz.cuni.mff.xrg.uv.extractor.sukl;

import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonException;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.impl.CachedFileDownloader;
import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;
import cz.cuni.mff.xrg.uv.boost.dpu.config.MasterConfigObject;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.SelectQuery;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.AddPolicy;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.OperationFailedException;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.SimpleRdfFactory;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.SimpleRdfRead;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.SimpleRdfWrite;
import cz.cuni.mff.xrg.uv.utils.dataunit.metadata.Manipulator;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.virtualpathhelper.VirtualPathHelper;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import java.io.File;
import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Škoda Petr
 */
@DPU.AsExtractor
public class Sukl extends DpuAdvancedBase<SuklConfig_V1>
        implements SelectQuery.BindingIterator {

    private static final Logger LOG = LoggerFactory.getLogger(Sukl.class);

    private static final String SUBJECT_BINDING = "s";

    private static final String SKOSNOTATION_BINDING = "skos";

    private static final String IN_QUERY
            = "SELECT ?" + SUBJECT_BINDING + " ?" + SKOSNOTATION_BINDING
            + " WHERE {"
            + " ?" + SUBJECT_BINDING + " <http://www.w3.org/2004/02/skos/core#notation> ?" + SKOSNOTATION_BINDING
            + " }";

    private static final String URI_BASE_INFO_CZ
            = "http://www.sukl.cz/modules/medication/detail.php?code=";

    private static final String URI_BASE_INFO_EN
            = "http://www.sukl.cz/modules/medication/detail.php?lang=2&code=";

    private static final String URI_BASE_TEXTS
            = "http://www.sukl.cz/modules/medication/detail.php?tab=texts&code=";

    private static final String CSS_SELECTOR
            = "div#medicine-box>table.zebra.vertical tbody tr";

    @DataUnit.AsInput(name = "SkosNotation")
    public RDFDataUnit rdfInSkosNotation;

    public SimpleRdfRead inSkosNoation;

    @DataUnit.AsOutput(name = "Info", description = "Active substances and links to files.")
    public WritableRDFDataUnit rdfOutInfo;

    public SimpleRdfWrite outInfo;

    @DataUnit.AsOutput(name = "Texts", description = "Text files.")
    public WritableFilesDataUnit filesOutTexts;

    private final CachedFileDownloader downloaderService;

    private ValueFactory valueFactory;

    public Sukl() {
        super(SuklConfig_V1.class,
                AddonInitializer.create(new CachedFileDownloader()));

        downloaderService = getAddon(CachedFileDownloader.class);
    }

    @Override
    protected void innerInit() throws DataUnitException {
        super.innerInit();
        //
        // wraps
        //
        inSkosNoation = SimpleRdfFactory.create(rdfInSkosNotation, context);
        outInfo = SimpleRdfFactory.create(rdfOutInfo, context);
        outInfo.setPolicy(AddPolicy.BUFFERED);
        valueFactory = this.outInfo.getValueFactory();
        //
        // ontology
        //
        SuklOntology.P_EFFECTIVE_SUBSTANCE_URI
                = valueFactory.createURI(SuklOntology.P_EFFECTIVE_SUBSTANCE);
        SuklOntology.P_PIL_URI
                = valueFactory.createURI(SuklOntology.P_PIL);
        SuklOntology.P_SPC_URI
                = valueFactory.createURI(SuklOntology.P_SPC);
        SuklOntology.P_TEXT_ON_THE_WRAP_URI
                = valueFactory.createURI(SuklOntology.P_TEXT_ON_THE_WRAP);
        SuklOntology.O_NOT_SET_URI
                = valueFactory.createURI(SuklOntology.O_NOT_SET);
    }

    @Override
    protected void innerExecute() throws DPUException {
        try {
            // will call processStatement
            SelectQuery.iterate(inSkosNoation, IN_QUERY, this);
            // flush buffer
            outInfo.flushBuffer();
        } catch (OperationFailedException | QueryEvaluationException e) {
            throw new DPUException("Query execution failed.", e);
        }
    }

    @Override
    public AbstractConfigDialog<MasterConfigObject> getConfigurationDialog() {
        return new SuklVaadinDialog();
    }

    @Override
    public void processStatement(BindingSet binding) throws DPUException {
        final String notation = binding.getBinding(SKOSNOTATION_BINDING)
                .getValue().stringValue();
        final URI subject = (URI) binding.getBinding(SUBJECT_BINDING)
                .getValue();
        // get information for given notation
        try {
            downloadInfo(subject, notation);
        } catch (AddonException | IOException e) {
            context.sendMessage(DPUContext.MessageType.WARNING,
                    "Failed to get info about notation.",
                    "Notation: " + notation, e);

            // TODO terminate or continue?

        } catch (DataUnitException ex) {
            throw new DPUException("Problem with dataUnit.", ex);
        }
    }

    /**
     * Download info for medicament with given skos:notation number.
     *
     * @param subject
     * @param notation
     * @throws cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonException
     * @throws java.io.IOException
     */
    private void downloadInfo(URI subject, String notation) throws AddonException,
            IOException, OperationFailedException, DataUnitException {
        LOG.debug("downloadInfo({}, {})", subject.toString(), notation);
        // parse info cz
        final File fileInfoCz = downloaderService.get(
                URI_BASE_INFO_CZ + notation);
        final Document docInfoCz = Jsoup.parse(fileInfoCz,
                null, "view-source:www.sukl.cz");
        if (parseNameCz(subject, docInfoCz)) {
            // parse info en only if we get the czech one
            // as if we not nor english is probably provided
            final File fileInfoEn = downloaderService.get(
                    URI_BASE_INFO_EN + notation);
            final Document docInfoEn = Jsoup.parse(fileInfoEn,
                    null, "view-source:www.sukl.cz");
            parseNameEn(subject, docInfoEn);
        }
        // parse info cz
        final File fileText = downloaderService.get(URI_BASE_TEXTS + notation);
        final Document docText = Jsoup.parse(fileText,
                null, "view-source:www.sukl.cz");
        parseTexts(subject, notation, docText);
    }

    /**
     * Parse document with Czech 'Main' tab.
     *
     * @param subject
     * @param doc
     * @return True if name is provided.
     * @throws OperationFailedException
     */
    private boolean parseNameCz(URI subject, Document doc)
            throws OperationFailedException {
        final Elements elements = doc.select(CSS_SELECTOR);
        for (Element element : elements) {
            if (element.getElementsByTag("th").first().text().compareTo(
                    "Účinná látka") == 0) {
                final String val = element.getElementsByTag("td").first().text();
                if (val.length() < 2) {
                    // this is just an empty string, we skip this
                    return false;
                }
                final String nameCz = val.substring(0, val.indexOf("("));
                final String nameLa = val.substring(val.indexOf("(") + 1,
                        val.indexOf(")"));
                // add
                outInfo.add(subject, SuklOntology.P_EFFECTIVE_SUBSTANCE_URI,
                        valueFactory.createLiteral(nameCz, "cz"));
                outInfo.add(subject, SuklOntology.P_EFFECTIVE_SUBSTANCE_URI,
                        valueFactory.createLiteral(nameLa, "la"));

                return true;
            }
        }
        // else not set
        outInfo.add(subject, SuklOntology.P_EFFECTIVE_SUBSTANCE_URI,
                SuklOntology.O_NOT_SET_URI);
        return false;
    }

    /**
     * Parse document with English 'Main" tab.
     *
     * @param subject
     * @param doc
     * @throws OperationFailedException
     */
    private void parseNameEn(URI subject, Document doc)
            throws OperationFailedException {
        final Elements elements = doc.select(CSS_SELECTOR);
        for (Element element : elements) {
            if (element.getElementsByTag("th").first().text().compareTo(
                    "Active substance") == 0) {
                final String val = element.getElementsByTag("td").first().text();
                final String nameEn = val.substring(0, val.indexOf("("));
                // add
                outInfo.add(subject, SuklOntology.P_EFFECTIVE_SUBSTANCE_URI,
                        valueFactory.createLiteral(nameEn, "en"));
            }
        }
    }

    /**
     * Parse page with 'Texts' tab.
     *
     * @param subject
     * @param notation
     * @param doc
     */
    private void parseTexts(URI subject, String notation, Document doc)
            throws OperationFailedException, AddonException, IOException,
            DataUnitException {
        final Elements elements = doc.select(CSS_SELECTOR);
        for (Element element : elements) {
            final Elements withHref = element.select("td a");
            // get name and value
            final String name = element.getElementsByTag("th").first().text();
            final String value = !withHref.isEmpty()
                    ? withHref.first().attr("abs:href") : null;
            // skil rows without values
            if (value == null) {
                // TODO we may add info if null value is one of our selected
                continue;
            }
            // parse
            URI predicate = null;
            String fileType = "";
            switch (name) {
                case "SPC - Souhrn údajů o přípravku":
                    predicate = SuklOntology.P_SPC_URI;
                    fileType = "spc";
                    break;
                case "PIL - Příbalová informace":
                    predicate = SuklOntology.P_PIL_URI;
                    fileType = "pil";
                    break;
                case "Text na obalu":
                    predicate = SuklOntology.P_TEXT_ON_THE_WRAP_URI;
                    fileType = "textOnTheWrap";
                    break;
            }
            if (predicate == null) {
                // value is not interesting for us
                continue;
            }
            // add info
            outInfo.add(subject, predicate,
                    valueFactory.createURI(value));
            // download
            final File file = downloaderService.get(value);
            filesOutTexts.addExistingFile(value, file.toURI().toString());
            // add metadata for path
            final String path = notation + "-" + fileType + ".pdf";
            Manipulator.add(filesOutTexts, value,
                    VirtualPathHelper.PREDICATE_VIRTUAL_PATH, path);
        }
    }

}
