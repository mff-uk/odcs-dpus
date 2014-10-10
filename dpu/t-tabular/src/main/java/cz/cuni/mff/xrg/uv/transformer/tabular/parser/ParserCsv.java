package cz.cuni.mff.xrg.uv.transformer.tabular.parser;

import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.OperationFailedException;
import cz.cuni.mff.xrg.uv.transformer.tabular.mapper.TableToRdf;
import cz.cuni.mff.xrg.uv.transformer.tabular.mapper.TableToRdfConfigurator;
import eu.unifiedviews.dpu.DPUContext;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.input.BOMInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;

/**
 * Parse csv file.
 *
 * @author Škoda Petr
 */
public class ParserCsv implements Parser {

    private static final Logger LOG = LoggerFactory.getLogger(ParserCsv.class);

    private final ParserCsvConfig config;

    private final TableToRdf tableToRdf;

    private final DPUContext context;

    private int rowNumber = 0;

    public ParserCsv(ParserCsvConfig config, TableToRdf tableToRdf,
            DPUContext context) {
        this.config = config;
        this.tableToRdf = tableToRdf;
        this.context = context;
    }

    @Override
    public void parse(File inFile) throws OperationFailedException, ParseFailed {
        final CsvPreference csvPreference = new CsvPreference.Builder(
                config.quoteChar.charAt(0),
                config.delimiterChar.charAt(0),
                "\\n") // is not used during reading
                .build();

        // set if for first time or if we use static row counter
        if (!config.checkStaticRowCounter || rowNumber == 0) {
           rowNumber = config.hasHeader ? 2 : 1;
        }

        try (FileInputStream fileInputStream = new FileInputStream(inFile);
                InputStreamReader inputStreamReader = getInputStream(
                        fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(
                        inputStreamReader);
                CsvListReader csvListReader = new CsvListReader(bufferedReader,
                        csvPreference)) {
            //
            // ignore initial ? lines
            //
            for (int i = 0; i < config.numberOfStartLinesToIgnore; ++i) {
                bufferedReader.readLine();
            }
            //
            // get header
            //
            final List<String> header;
            if (config.hasHeader) {
                header = Arrays.asList(csvListReader.getHeader(true));
            } else {
                header = null;
            }
            //
            // read rows and parse
            //
            int rowNumPerFile = 0;
            List<String> row = csvListReader.read();
            if (row == null) {
                // no data
                LOG.info("No data found!");
                return;
            }

            // configure parser
            TableToRdfConfigurator.configure(tableToRdf, header, (List)row);
            // go ...
            if (config.rowLimit == null) {
                LOG.debug("Row limit: not used");
            } else {
                LOG.debug("Row limit: {}", config.rowLimit);
            }
            while (row != null && 
                    (config.rowLimit == null || rowNumPerFile < config.rowLimit) &&
                    !context.canceled()) {
                // cast string to objects
                tableToRdf.paserRow((List)row, rowNumber);
                // read next row
                rowNumber++;
                rowNumPerFile++;
                row = csvListReader.read();
                // log
                if ((rowNumPerFile % 1000) == 0) {
                    LOG.debug("Row number {} processed.", rowNumPerFile);
                }
            }
        } catch (IOException ex) {
            throw new ParseFailed("Parse of '" + inFile.toString() + "' failed",
                    ex);
        }
    }

    /**
     * Create {@link InputStreamReader}. If "UTF-8" as encoding is given then
     * {@link BOMInputStream} is used as intermedian between given
     * fileInputStream and output {@link InputStreamReader} to remove
     * possible BOM mark at the start of "UTF" files.
     *
     * @param fileInputStream
     * @return
     * @throws UnsupportedEncodingException
     */
    private InputStreamReader getInputStream(FileInputStream fileInputStream) throws UnsupportedEncodingException {
        if (config.encoding.compareToIgnoreCase("UTF-8") == 0) {
            return new InputStreamReader(new BOMInputStream(fileInputStream, false), config.encoding);
        } else {
            return new InputStreamReader(fileInputStream, config.encoding);
        }
    }

}
