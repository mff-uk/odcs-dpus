package cz.cuni.mff.xrg.uv.transformer.tabular.parser;

import cz.cuni.mff.xrg.uv.rdf.simple.OperationFailedException;
import cz.cuni.mff.xrg.uv.transformer.tabular.mapper.TableToRdf;
import cz.cuni.mff.xrg.uv.transformer.tabular.mapper.TableToRdfConfigurator;
import eu.unifiedviews.dpu.DPUContext;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
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

        try (FileInputStream fileInputStream = new FileInputStream(inFile);
                InputStreamReader inputStreamReader = new InputStreamReader(
                        fileInputStream, config.encoding);
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
            int rowNumber = config.hasHeader ? 2 : 1;
            List<String> row = csvListReader.read();
            // configure parser
            TableToRdfConfigurator.configure(tableToRdf, header, (List)row);
            // go ...
            while (row != null && 
                    (config.rowLimit == null || rowNumber < config.rowLimit) &&
                    !context.canceled()) {
                // cast string to objects
                tableToRdf.paserRow((List)row, rowNumber);
                // read next row
                rowNumber++;
                row = csvListReader.read();
                // log
                if ((rowNumber % 1000) == 0) {
                    LOG.debug("Row number {} processed.", rowNumber);
                }
            }
        } catch (IOException ex) {
            throw new ParseFailed("Parse of '" + inFile.toString() + "' failed",
                    ex);
        }
    }



}
