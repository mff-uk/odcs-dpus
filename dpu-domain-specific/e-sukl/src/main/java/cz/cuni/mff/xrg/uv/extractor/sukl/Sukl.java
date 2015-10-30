package cz.cuni.mff.xrg.uv.extractor.sukl;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionException;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultToleranceUtils;
import eu.unifiedviews.helpers.dpu.extension.files.simple.WritableSimpleFiles;
import eu.unifiedviews.helpers.dpu.extension.rdf.simple.SimpleRdf;
import eu.unifiedviews.helpers.dpu.extension.rdf.simple.WritableSimpleRdf;
import eu.unifiedviews.helpers.dpu.rdf.sparql.SparqlUtils;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Škoda Petr
 */
@DPU.AsExtractor
public class Sukl extends AbstractDpu<SuklConfig_V1> {

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

    @ExtensionInitializer.Init(param = "rdfInSkosNotation")
    public SimpleRdf inSkosNotation;

    @DataUnit.AsOutput(name = "Info", description = "Active substances and links to files.")
    public WritableRDFDataUnit rdfOutInfo;

    @ExtensionInitializer.Init(param = "rdfOutInfo")
    public WritableSimpleRdf outInfo;

    @DataUnit.AsOutput(name = "NewTexts", description = "Newly downloaded text files.")
    public WritableFilesDataUnit filesOutNewTexts;

    @ExtensionInitializer.Init(param = "filesOutNewTexts")
    public WritableSimpleFiles outNewTexts;

    @ExtensionInitializer.Init
    public CachedFileDownloader downloaderService;

    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;

    private ValueFactory valueFactory;

    /**
     * Count number of files on output.
     */
    private int numberOfDownloaded = 0;

    private int numberOfChached = 0;

    /**
     * Count number of missing.
     */
    private int numberOfMissing = 0;

    /**
     * Store info about files on output as files can be shared.
     */
    private final Set<String> downloadedFiles = new HashSet<>();

    public Sukl() {
        super(SuklVaadinDialog.class, ConfigHistory.noHistory(SuklConfig_V1.class));
    }

    @Override
    protected void innerInit() throws DataUnitException, DPUException {
        super.innerInit();
        // Some local variables.
        numberOfDownloaded = 0;
        numberOfChached = 0;
        numberOfMissing = 0;
        downloadedFiles.clear();
    }

    @Override
    protected void innerExecute() throws DPUException {
        valueFactory = outInfo.getValueFactory();
        // Go for per-graph mode.
        final List<RDFDataUnit.Entry> entries = FaultToleranceUtils.getEntries(faultTolerance,
                rdfInSkosNotation, RDFDataUnit.Entry.class);
        for (RDFDataUnit.Entry entry : entries) {
            process(Arrays.asList(entry));
            if (!config.isCountNumberOfMissing() && this.numberOfMissing > 0) {
                // Missing file occured.
                LOG.info("Break for missing file!");
                break;
            }
            // Support for cancel.
            if (ctx.canceled()) {
                ContextUtils.sendShortInfo(ctx, "{0} downloaded, {1} cached, {2} missing",
                        numberOfDownloaded, numberOfChached, numberOfMissing);
                throw ContextUtils.dpuExceptionCancelled(ctx);
            }
        }
        ContextUtils.sendShortInfo(ctx, "{0} downloaded, {1} cached, {2} missing",
                numberOfDownloaded, numberOfChached, numberOfMissing);
    }

    /**
     * Process given graphs.
     *
     * @param entries
     * @throws DPUException
     */
    public void process(final List<RDFDataUnit.Entry> entries) throws DPUException {
        final SparqlUtils.QueryResultCollector colector = new SparqlUtils.QueryResultCollector();
        faultTolerance.execute(rdfInSkosNotation, new FaultTolerance.ConnectionAction() {

            @Override
            public void action(RepositoryConnection connection) throws Exception {
                final SparqlUtils.SparqlSelectObject select = SparqlUtils.createSelect(IN_QUERY, entries);
                SparqlUtils.execute(connection, ctx, select, colector);
            }
        });
        // For each row of result (tuple).
        int counter = 0;
        for (Map<String, Value> row : colector.getResults()) {
            final String notation = row.get(SKOSNOTATION_BINDING).stringValue();
            final URI subject = (URI) row.get(SUBJECT_BINDING);
            try {
                downloadInfo(subject, notation);
            } catch (IOException | ExtensionException ex) {
                LOG.error("downloadInfo failed.", ex);
                throw ContextUtils.dpuException(ctx, ex, "Downloading failed.");
            }
            // Support for cancel.
            if (ctx.canceled()) {
                ContextUtils.sendShortInfo(ctx, "{0} downloaded, {1} cached, {2} missing",
                        numberOfDownloaded, numberOfChached, numberOfMissing);
                throw ContextUtils.dpuExceptionCancelled(ctx);
            }
            ++counter;
            if (counter % 1000 == 0) {
                LOG.info("Progress {}/{}", counter, colector.getResults().size());
            }
        }
    }

    /**
     *
     * @param notation
     * @return True if no other content for this novation should be downloaded.
     * @throws DPUException
     * @throws IOException
     * @throws ExtensionException
     */
    private boolean onDownloadError(String notation) throws DPUException, IOException, ExtensionException {
        if (config.isDeletePagesOnError()) {
            // Move index files into the delete folder.
            final File deletedStorage = new File(config.getDeletedFileStorage(),
                    new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            deletedStorage.mkdirs();
            // Copy exisitng file.
            final File fileInfoCz = downloaderService.getFromCache(URI_BASE_INFO_CZ + notation);
            LOG.debug("Moving files ...");
            if (fileInfoCz != null) {
                fileInfoCz.renameTo(new File(deletedStorage, fileInfoCz.getName()));
            }
            final File fileInfoEn = downloaderService.getFromCache(URI_BASE_INFO_EN + notation);
            if (fileInfoEn != null) {
                fileInfoEn.renameTo(new File(deletedStorage, fileInfoEn.getName()));
            }
            final File fileText = downloaderService.getFromCache(URI_BASE_TEXTS + notation);
            if (fileText != null) {
                fileText.renameTo(new File(deletedStorage, fileText.getName()));
            }
            LOG.debug("Moving files ... done");
            return true;
        }
        return false;
    }

    /**
     * Download info for medicament with given skos:notation number.
     *
     * @param subject
     * @param notation Id of given medicament.
     * @throws DPUException
     */
    private void downloadInfo(URI subject, String notation) throws DPUException, IOException, ExtensionException {
        // Parse info cz.
        final CachedFileDownloader.DownloadResult fileInfoCz = downloaderService.get(URI_BASE_INFO_CZ + notation);
        switch (fileInfoCz.getType()) {
            case DOWNLOADED:
                LOG.info("File (new): {}", URI_BASE_INFO_CZ + notation);
                parseNameCz(subject, Jsoup.parse(fileInfoCz.getFile(), null));
                ++numberOfDownloaded;
                break;
            case CACHED:
                parseNameCz(subject, Jsoup.parse(fileInfoCz.getFile(), null));
                ++numberOfChached;
                break;
            case ERROR:
                LOG.error("File (error) : {}", URI_BASE_INFO_CZ + notation);
                if (config.isFailOnDownloadError()) {
                    throw new DPUException("Can't download file!");
                }
                if (onDownloadError(notation)) {
                    return;
                }
                break;
            case MISSING:
                ++numberOfMissing;
                break;
            default:
                throw new DPUException("Unknown type!");
        }

        // Parse 'en' only if we get the czech one as if we not nor english is probably provided.
        final CachedFileDownloader.DownloadResult fileInfoEn = downloaderService.get(URI_BASE_INFO_EN + notation);
        switch (fileInfoEn.getType()) {
            case DOWNLOADED:
                LOG.info("File (new): {}", URI_BASE_INFO_EN + notation);
                parseNameEn(subject, Jsoup.parse(fileInfoEn.getFile(), null));
                ++numberOfDownloaded;
                break;
            case CACHED:
                parseNameEn(subject, Jsoup.parse(fileInfoEn.getFile(), null));
                ++numberOfChached;
                break;
            case ERROR:
                LOG.error("File (error) : {}", URI_BASE_INFO_EN + notation);
                if (config.isFailOnDownloadError()) {
                    throw new DPUException("Can't download file!");
                }
                if (onDownloadError(notation)) {
                    return;
                }
                break;
            case MISSING:
                ++numberOfMissing;
                break;
            default:
                throw new DPUException("Unknown type!");
        }
        // Donwload texts.
        final CachedFileDownloader.DownloadResult fileText = downloaderService.get(URI_BASE_TEXTS + notation);
        switch (fileText.getType()) {
            case DOWNLOADED:
                LOG.info("File (new): {}", URI_BASE_TEXTS + notation);
                parseTexts(subject, notation, Jsoup.parse(fileText.getFile(), null));
                ++numberOfDownloaded;
                break;
            case CACHED:
                parseTexts(subject, notation, Jsoup.parse(fileText.getFile(), null));
                ++numberOfChached;
                break;
            case ERROR:
                LOG.error("File (error) : {}", URI_BASE_TEXTS + notation);
                if (config.isFailOnDownloadError()) {
                    throw new DPUException("Can't download file!");
                }
                if (onDownloadError(notation)) {
                    return;
                }
                break;
            case MISSING:
                ++numberOfMissing;
                break;
            default:
                throw new DPUException("Unknown type!");
        }
    }

    /**
     * Parse document with Czech 'Main' tab.
     *
     * @param subject
     * @param doc
     */
    private void parseNameCz(URI subject, Document doc) throws DPUException {
        final Elements elements = doc.select(CSS_SELECTOR);
        for (Element element : elements) {
            if (element.getElementsByTag("th").first().text().compareTo("Účinná látka") == 0) {
                final String val = element.getElementsByTag("td").first().html();
                final String valText = element.getElementsByTag("td").first().text().trim();
                if (valText.length() < 2) {
                    // Just an empty string, we skip this
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
     */
    private void parseNameEn(URI subject, Document doc) throws DPUException {
        final Elements elements = doc.select(CSS_SELECTOR);
        for (Element element : elements) {
            if (element.getElementsByTag("th").first().text().compareTo("Active substance") == 0) {
                final String val = element.getElementsByTag("td").first().html();
                final String valText = element.getElementsByTag("td").first().text().trim();
                if (valText.length() < 2) {
                    // Just an empty string, we skip this
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
     * @param nameLa Identifier of ingredient - name in latin.
     * @param name
     * @param lang
     */
    private void addIngredient(URI subject, String nameLa, String name, String lang) throws DPUException {
        final String ingredientSubjectStr = SuklOntology.INGREDIEND_PREFIX
                + Utils.convertStringToURIPart(nameLa);
        final URI ingredientUri = valueFactory.createURI(ingredientSubjectStr);

        outInfo.add(subject, SuklOntology.HAS_INGREDIEND, ingredientUri);
        // Add names.
        outInfo.add(ingredientUri, SuklOntology.INGREDIEND_NAME_SKOS, valueFactory.createLiteral(nameLa, "la"));
        outInfo.add(ingredientUri, SuklOntology.INGREDIEND_NAME_SKOS, valueFactory.createLiteral(name, lang));
    }

    /**
     * Parse page with 'Texts' tab.
     *
     * @param subject
     * @param notation
     * @param doc
     */
    private void parseTexts(URI subject, String notation, Document doc) throws IOException, DPUException, ExtensionException {
        final Elements elements = doc.select(CSS_SELECTOR);
        for (Element element : elements) {
            final Elements withHref = element.select("td a");
            // Get name and value.
            final String name = element.getElementsByTag("th").first().text();
            final String value = !withHref.isEmpty() ? withHref.first().attr("abs:href") : null;
            // Skip rows without values.
            if (value == null) {
                // TODO we may add info if null value is one of our selected
                continue;
            }
            // Parse.
            URI predicate = null;
            URI predicateFile = null;
            switch (name) {
                case "SPC - Souhrn údajů o přípravku":
                    predicate = SuklOntology.SPC;
                    predicateFile = SuklOntology.SPC_FILE;
                    break;
                case "PIL - Příbalová informace":
                    predicate = SuklOntology.PIL;
                    predicateFile = SuklOntology.PIL_FILE;
                    break;
                case "Text na obalu":
                    predicate = SuklOntology.TEXT_ON_THE_WRAP;
                    predicateFile = SuklOntology.TEXT_ON_THE_WRAP_FILE;
                    break;
                default:
                    break;
            }
            if (predicate == null || predicateFile == null) {
                // Value is not interesting for us.
                continue;
            }
            // Add info.
            outInfo.add(subject, predicate, valueFactory.createURI(value));
            final String fileName = Utils.convertStringToURIPart(value);
            outInfo.add(subject, predicateFile, valueFactory.createLiteral(fileName));

            // Store to output.
            if (downloadedFiles.contains(fileName)) {
                // Already added - this can happen if the same file is shared by multiple instances.
                continue;
            }

            final CachedFileDownloader.DownloadResult downloaded = downloaderService.get(value);
            switch (downloaded.getType()) {
                case DOWNLOADED:
                    LOG.info("File (new): {} with name: {}", value, fileName);
                    downloadedFiles.add(fileName);
                    ++numberOfDownloaded;
                    // Add to new list.
                    if (config.isNewFileToOutput()) {
                        outNewTexts.add(downloaded.getFile(), fileName);
                    }
                    break;
                case CACHED:
                    downloadedFiles.add(fileName);
                    ++numberOfChached;
                    break;
                case ERROR:
                    LOG.error("File (error, notation:{}) : {} with name: {}", notation, value, fileName);
                    if (config.isFailOnDownloadError()) {
                        throw new DPUException("Can't download file!");
                    }
                    if (onDownloadError(notation)) {
                        return;
                    }
                    break;
                case MISSING:
                    ++numberOfMissing;
                    break;
                default:
                    throw new DPUException("Unknown type!");
            }
        }
    }

}
