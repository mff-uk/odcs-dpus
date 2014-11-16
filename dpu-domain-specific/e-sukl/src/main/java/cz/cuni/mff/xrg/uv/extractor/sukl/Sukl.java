package cz.cuni.mff.xrg.uv.extractor.sukl;

import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonException;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.impl.CachedFileDownloader;
import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;
import cz.cuni.mff.xrg.uv.boost.dpu.config.MasterConfigObject;
import cz.cuni.mff.xrg.uv.boost.dpu.utils.SendMessage;
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
import java.util.HashSet;
import java.util.Set;
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

    public SimpleRdfRead inSkosNotation;

    @DataUnit.AsOutput(name = "Info", description = "Active substances and links to files.")
    public WritableRDFDataUnit rdfOutInfo;

    public SimpleRdfWrite outInfo;

    @DataUnit.AsOutput(name = "Texts", description = "Text files.")
    public WritableFilesDataUnit filesOutTexts;

    private final CachedFileDownloader downloaderService;

    private ValueFactory valueFactory;

    /**
     * Count number of files on output.
     */
    private Integer filesOnOutput = 0;

    /**
     * Store info about files on output as files can be shared.
     */
    private final Set<String> downloadedFiles = new HashSet<>();

    public Sukl() {
        super(SuklConfig_V1.class, AddonInitializer.create(new CachedFileDownloader()));

        downloaderService = getAddon(CachedFileDownloader.class);
    }

    @Override
    protected void innerInit() throws DataUnitException {
        super.innerInit();
        //
        // wraps
        //
        inSkosNotation = SimpleRdfFactory.create(rdfInSkosNotation, context);
        outInfo = SimpleRdfFactory.create(rdfOutInfo, context);
        outInfo.setPolicy(AddPolicy.BUFFERED);
        valueFactory = this.outInfo.getValueFactory();
        //
        // ontology
        //
        SuklOntology.O_INGREDIEND_CLASS_URI
                = valueFactory.createURI(SuklOntology.O_INGREDIEND_CLASS);
        SuklOntology.P_HAS_INGREDIEND_URI
                = valueFactory.createURI(SuklOntology.P_HAS_INGREDIEND);
        SuklOntology.P_INGREDIEND_NAME_DCTERMS_URI 
                = valueFactory.createURI(
                SuklOntology.P_INGREDIEND_NAME_DCTERMS);
        SuklOntology.P_INGREDIEND_NAME_SKOS_URI
                = valueFactory.createURI(SuklOntology.P_INGREDIEND_NAME_SKOS);
        SuklOntology.P_PIL_URI
                = valueFactory.createURI(SuklOntology.P_PIL);
        SuklOntology.P_PIL_FILE_URI
                = valueFactory.createURI(SuklOntology.P_PIL_FILE);
        SuklOntology.P_SPC_URI
                = valueFactory.createURI(SuklOntology.P_SPC);
        SuklOntology.P_SPC_FILE_URI
                = valueFactory.createURI(SuklOntology.P_SPC_FILE);
        SuklOntology.P_TEXT_ON_THE_WRAP_URI
                = valueFactory.createURI(SuklOntology.P_TEXT_ON_THE_WRAP);
        SuklOntology.P_TEXT_ON_THE_WRAP_FILE_URI
                = valueFactory.createURI(SuklOntology.P_TEXT_ON_THE_WRAP_FILE);
        // locals
        filesOnOutput = 0;
        downloadedFiles.clear();
    }

    @Override
    protected void innerExecute() throws DPUException {
        try {
            // will call processStatement
            SelectQuery.iterate(inSkosNotation, IN_QUERY, this, context);
            // flush buffer
            outInfo.flushBuffer();
        } catch (OperationFailedException ex) {
            SendMessage.sendMessage(context, ex);
        } catch (QueryEvaluationException ex) {
            throw new DPUException("Query execution failed.", ex);
        }
        // log number of files
        context.sendMessage(DPUContext.MessageType.INFO, filesOnOutput.toString() + " files downloaded.");
    }

    @Override
    public AbstractConfigDialog<MasterConfigObject> getConfigurationDialog() {
        return new SuklVaadinDialog();
    }

    @Override
    public void processStatement(BindingSet binding) throws DPUException {
        final String notation = binding.getBinding(SKOSNOTATION_BINDING).getValue().stringValue();
        final URI subject = (URI) binding.getBinding(SUBJECT_BINDING).getValue();
        // get information for given notation
        try {
            downloadInfo(subject, notation);
        } catch (AddonException | IOException e) {
            context.sendMessage(DPUContext.MessageType.WARNING, "Failed to get info about notation.",
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
     * @param notation Id of given medicament.
     * @throws cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonException
     * @throws java.io.IOException
     */
    private void downloadInfo(URI subject, String notation) throws AddonException,
            IOException, OperationFailedException, DataUnitException {
        LOG.debug("downloadInfo({}, {})", subject.toString(), notation);

        //
        // parse info cz
        //
        final File fileInfoCz = downloaderService.get(URI_BASE_INFO_CZ + notation);
        if (fileInfoCz != null) {
            final Document docInfoCz = Jsoup.parse(fileInfoCz, null);
            parseNameCz(subject, docInfoCz);
        }
        //
        // parse 'en' only if we get the czech one
        // as if we not nor english is probably provided
        //
        final File fileInfoEn = downloaderService.get(URI_BASE_INFO_EN + notation);
        if (fileInfoEn != null) {
            final Document docInfoEn = Jsoup.parse(fileInfoEn, null);
            parseNameEn(subject, docInfoEn);
        }
        //
        // donwload texts
        //
        final File fileText = downloaderService.get(URI_BASE_TEXTS + notation);
        if (fileText != null) {
            final Document docText = Jsoup.parse(fileText, null);
            parseTexts(subject, notation, docText);
        }
    }

    /**
     * Parse document with Czech 'Main' tab.
     *
     * @param subject
     * @param doc
     * @throws OperationFailedException
     */
    private void parseNameCz(URI subject, Document doc) throws OperationFailedException {
        final Elements elements = doc.select(CSS_SELECTOR);
        for (Element element : elements) {
            if (element.getElementsByTag("th").first().text().compareTo("Účinná látka") == 0) {
                final String val = element.getElementsByTag("td").first().html();
                final String valText = element.getElementsByTag("td").first().text().trim();
                if (valText.length() < 2) {
                    // this is just an empty string, we skip this
                    LOG.trace("Value '{}' skipped.", valText);
                    return;
                }

                final String[] substances = val.split("<br />");
                for (String substance : substances) {
                    substance = substance.trim();
                    // Get separation index and names.
                    int index = Utils.getLastOpeningBraceIndex(substance);
                    final String nameCs = substance.substring(0, index).trim();
                    final String nameLa = substance.substring(index + 1, substance.length() - 1).trim();
                    // Add substance names.
                    addIngredient(subject, nameLa, nameCs, "cs");
                }
            }
        }
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
            if (element.getElementsByTag("th").first().text().compareTo("Active substance") == 0) {
                final String val = element.getElementsByTag("td").first().html();
                final String valText = element.getElementsByTag("td").first().text().trim();
                if (valText.length() < 2) {
                    // this is just an empty string, we skip this
                    LOG.trace("Value '{}' skipped.", valText);
                    return;
                }

                final String[] substances = val.split("<br />");
                for (String substance : substances) {
                    substance = substance.trim();
                    // Get separation index and names.
                    int index = Utils.getLastOpeningBraceIndex(substance);
                    final String nameEn = substance.substring(0, index).trim();
                    final String nameLa = substance.substring(index + 1, substance.length() - 1).trim();
                    // Add substance names.
                    addIngredient(subject, nameLa, nameEn, "en");
                }
            }
        }
    }

    /**
     * Create record for given ingredient and bind it to given subject.
     *
     * @param subject
     * @param nameLa  Identifier of ingredient - name in latin.
     * @param name
     * @param lang
     * @throws OperationFailedException
     */
    private void addIngredient(URI subject, String nameLa, String name,
            String lang) throws OperationFailedException {
        final String ingredientSubjectStr = SuklOntology.INGREDIEND_PREFIX
                + Utils.convertStringToURIPart(nameLa);
        final URI ingredientUri = valueFactory.createURI(ingredientSubjectStr);

        outInfo.add(subject, SuklOntology.P_HAS_INGREDIEND_URI, ingredientUri);
        // add names
        outInfo.add(ingredientUri, SuklOntology.P_INGREDIEND_NAME_SKOS_URI,
                valueFactory.createLiteral(nameLa, "la"));
        outInfo.add(ingredientUri, SuklOntology.P_INGREDIEND_NAME_SKOS_URI,
                valueFactory.createLiteral(name, lang));
    }

    /**
     * Parse page with 'Texts' tab.
     *
     * @param subject
     * @param notation
     * @param doc
     * @throws cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.OperationFailedException
     * @throws cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonException
     * @throws java.io.IOException
     */
    private void parseTexts(URI subject, String notation, Document doc)
            throws OperationFailedException, AddonException, IOException,
            DataUnitException {
        final Elements elements = doc.select(CSS_SELECTOR);
        for (Element element : elements) {
            final Elements withHref = element.select("td a");
            // get name and value
            final String name = element.getElementsByTag("th").first().text();
            final String value = !withHref.isEmpty() ? withHref.first().attr("abs:href") : null;
            // skil rows without values
            if (value == null) {
                // TODO we may add info if null value is one of our selected
                continue;
            }
            // parse
            URI predicate = null;
            URI predicateFile = null;
            switch (name) {
                case "SPC - Souhrn údajů o přípravku":
                    predicate = SuklOntology.P_SPC_URI;
                    predicateFile = SuklOntology.P_SPC_FILE_URI;
                    break;
                case "PIL - Příbalová informace":
                    predicate = SuklOntology.P_PIL_URI;
                    predicateFile = SuklOntology.P_PIL_FILE_URI;
                    break;
                case "Text na obalu":
                    predicate = SuklOntology.P_TEXT_ON_THE_WRAP_URI;
                    predicateFile = SuklOntology.P_TEXT_ON_THE_WRAP_FILE_URI;
                    break;
            }
            if (predicate == null || predicateFile == null) {
                // value is not interesting for us
                continue;
            }
            // add info
            outInfo.add(subject, predicate, valueFactory.createURI(value));
            final String fileName = Utils.convertStringToURIPart(value);
            outInfo.add(subject, predicateFile, valueFactory.createLiteral(fileName));

            // download
            final File file = downloaderService.get(value);
            if (file == null) {
                // file is missing
                LOG.warn("Missing file: {} with name: {}", value, fileName);
                return;
            }
            
            if (downloadedFiles.contains(fileName)) {
                // already downloaded
                return;
            }

            // add metadata for path
            while (!context.canceled()) {
                try {
                    filesOutTexts.addExistingFile(fileName, file.toURI().toString());
                    // files has been added
                    ++filesOnOutput;
                    break;
                } catch (DataUnitException ex) {
                    LOG.warn("FilesDataUnit.addExistingFile throws. ", ex);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {

                }
            }
            LOG.debug("new file {} -> {}", file.toURI().toString(), fileName);
            Manipulator.add(filesOutTexts, fileName, VirtualPathHelper.PREDICATE_VIRTUAL_PATH, fileName);
            // add
            downloadedFiles.add(fileName);
        }
    }

}
