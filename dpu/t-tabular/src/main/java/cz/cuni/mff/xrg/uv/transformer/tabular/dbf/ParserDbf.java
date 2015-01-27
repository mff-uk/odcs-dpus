package cz.cuni.mff.xrg.uv.transformer.tabular.dbf;

import cz.cuni.mff.xrg.uv.transformer.tabular.mapper.TableToRdf;
import cz.cuni.mff.xrg.uv.transformer.tabular.mapper.TableToRdfConfigurator;
import cz.cuni.mff.xrg.uv.transformer.tabular.parser.ParseFailed;
import cz.cuni.mff.xrg.uv.transformer.tabular.parser.Parser;
import eu.unifiedviews.dpu.DPUContext;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jamel.dbf.DbfReader;
import org.jamel.dbf.structure.DbfField;
import org.jamel.dbf.structure.DbfHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.uv.boost.serialization.rdf.SimpleRdfException;

/**
 *
 * @author Škoda Petr
 */
public class ParserDbf implements Parser {

    private static final Logger LOG = LoggerFactory.getLogger(ParserDbf.class);

    private final ParserDbfConfig config;

    private final TableToRdf tableToRdf;

    private final DPUContext context;

    private int rowNumber = 0;

    public ParserDbf(ParserDbfConfig config, TableToRdf tableToRdf, DPUContext context) {
        this.config = config;
        this.tableToRdf = tableToRdf;
        this.context = context;
    }

    @Override
    public void parse(File inFile) throws  ParseFailed, SimpleRdfException {
        final String encoding;
        if (config.encoding == null || config.encoding.isEmpty()) {
            // parse from DBF file
            encoding = "UTF-8";
        } else {
            encoding = config.encoding;
        }
        if (!Charset.isSupported(encoding)) {
            throw new ParseFailed("Charset '" + encoding + "' is not supported.");
        }
        final DbfReader reader = new DbfReader(inFile);
        // Get header.
        final List<String> header;
        final DbfHeader dbfHeader = reader.getHeader();
        header = new ArrayList<>(dbfHeader.getFieldsCount());
        for (int i = 0; i < dbfHeader.getFieldsCount(); ++i) {
            final DbfField field = dbfHeader.getField(i);
            header.add(field.getName());
        }
        // Parase other rows.

        // Set rowNumber base on static counter.
        if (!config.checkStaticRowCounter || rowNumber == 0) {
            rowNumber = 1;
        }
        int rowNumPerFile = 0;
        Object[] row = reader.nextRecord();
        List<Object> stringRow = new ArrayList(row.length);
        // Configure parser.
        TableToRdfConfigurator.configure(tableToRdf, header, Arrays.asList(row));
        // Go ...
        if (config.rowLimit == null) {
            LOG.debug("Row limit: not used");
        } else {
            LOG.debug("Row limit: {}", config.rowLimit);
        }
        while (row != null && (config.rowLimit == null || rowNumPerFile < config.rowLimit)
                && !context.canceled()) {
            // Convert row into items, so we can pass them to mapper.
            for (Object item : row) {
                if (item instanceof byte[]) {
                    try {
                        stringRow.add(new String((byte[]) item, config.encoding));
                    } catch (UnsupportedEncodingException ex) {
                        // Terminate DPU as this can not be handled.
                        throw new RuntimeException(ex);
                    }
                } else {
                    stringRow.add(item);
                }
            }
            tableToRdf.paserRow(stringRow, rowNumber);
            // Read next row.
            rowNumber++;
            rowNumPerFile++;
            row = reader.nextRecord();
            stringRow.clear();
            if ((rowNumPerFile % 1000) == 0) {
                LOG.debug("Row number {} processed.", rowNumPerFile);
            }
        }
    }

}
