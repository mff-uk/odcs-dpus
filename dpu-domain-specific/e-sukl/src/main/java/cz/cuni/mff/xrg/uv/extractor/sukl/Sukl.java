package cz.cuni.mff.xrg.uv.extractor.sukl;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;

import java.io.File;
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

    @DataUnit.AsOutput(name = "Texts", description = "Text files.")
    public WritableFilesDataUnit filesOutTexts;

    @DataUnit.AsOutput(name = "NewTexts", description = "Newly downloaded text files.")
    public WritableFilesDataUnit filesOutNewTexts;

    @ExtensionInitializer.Init(param = "filesOutTexts")
    public WritableSimpleFiles outTexts;

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
    private Integer filesOnOutput = 0;

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
        filesOnOutput = 0;
        downloadedFiles.clear();
    }

    @Override
    protected void innerExecute() throws DPUException {
        valueFactory = outInfo.getValueFactory();
        // Go for per-graph mode.
        final List<RDFDataUnit.Entry> entries = FaultToleranceUtils.getEntries(faultTolerance,
                rdfInSkosNotation, RDFDataUnit.Entry.class);
        int counter = 0;
        for (RDFDataUnit.Entry entry : entries) {
            LOG.info("Processing {}/{} input graphs.", ++counter, entries.size());
            process(Arrays.asList(entry));
        }
        ContextUtils.sendShortInfo(ctx, "{0} files on output", filesOnOutput);
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
        for (Map<String, Value> row : colector.getResults()) {
            final String notation = row.get(SKOSNOTATION_BINDING).stringValue();
            final URI subject = (URI) row.get(SUBJECT_BINDING);
            try {
                downloadInfo(subject, notation);
            } catch (IOException | ExtensionException ex) {
                throw ContextUtils.dpuException(ctx, ex, "Downloading failed.");
            }
        }
    }

    /**
     * Download info for medicament with given skos:notation number.
     *
     * @param subject
     * @param notation Id of given medicament.
     * @throws DPUException
     */
    private void downloadInfo(URI subject, String notation) throws DPUException, IOException, ExtensionException {
        LOG.debug("downloadInfo({}, {})", subject.toString(), notation);
        // Parse info cz.
        final File fileInfoCz = downloaderService.get(URI_BASE_INFO_CZ + notation);
        if (fileInfoCz != null) {
            final Document docInfoCz = Jsoup.parse(fileInfoCz, null);
            parseNameCz(subject, docInfoCz);
        }
        // Parse 'en' only if we get the czech one as if we not nor english is probably provided.
        final File fileInfoEn = downloaderService.get(URI_BASE_INFO_EN + notation);
        if (fileInfoEn != null) {
            final Document docInfoEn = Jsoup.parse(fileInfoEn, null);
            parseNameEn(subject, docInfoEn);
        }
        // Donwload texts.
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
     */
    private void parseNameCz(URI subject, Document doc) throws DPUException  {
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
     */
    private void parseNameEn(URI subject, Document doc) throws DPUException  {
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
     */
    private void addIngredient(URI subject, String nameLa, String name, String lang) throws DPUException  {
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
            }
            if (predicate == null || predicateFile == null) {
                // Value is not interesting for us.
                continue;
            }
            // Add info.
            outInfo.add(subject, predicate, valueFactory.createURI(value));
            final String fileName = Utils.convertStringToURIPart(value);
            outInfo.add(subject, predicateFile, valueFactory.createLiteral(fileName));
            // Download.

            int numberOfDownloaded = downloaderService.getNumberOfDownloads();
            final File file = downloaderService.get(value);

            if (file == null) {
                // File is missing.
                LOG.warn("Missing file: {} with name: {}", value, fileName);
                return;
            }

            if (downloadedFiles.contains(fileName)) {
                // Already downloaded.
                return;
            }

            // Add to output.
            outTexts.add(file, fileName);

            // Add to new file if file has not been taken from cache ie. numberOfDownloaded icreased.
            if (numberOfDownloaded < downloaderService.getNumberOfDownloads()) {
                outNewTexts.add(file, fileName);
            }

            // Files has been added.
            ++filesOnOutput;

            LOG.debug("new file {} -> {}", file.toURI().toString(), fileName);
            // Add file to touptut.
            downloadedFiles.add(fileName);
        }
    }

}
