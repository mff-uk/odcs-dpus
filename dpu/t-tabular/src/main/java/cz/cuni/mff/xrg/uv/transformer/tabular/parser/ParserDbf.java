package cz.cuni.mff.xrg.uv.transformer.tabular.parser;

import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.OperationFailedException;
import cz.cuni.mff.xrg.uv.transformer.tabular.mapper.TableToRdf;
import cz.cuni.mff.xrg.uv.transformer.tabular.mapper.TableToRdfConfigurator;
import eu.unifiedviews.dpu.DPUContext;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jamel.dbf.DbfReader;
import org.jamel.dbf.structure.DbfField;
import org.jamel.dbf.structure.DbfHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Škoda Petr
 */
public class ParserDbf implements Parser {

    private static final Logger LOG = LoggerFactory.getLogger(ParserDbf.class);

    private final ParserDbfConfig config;

    private final TableToRdf tableToRdf;

    private final DPUContext context;

    public ParserDbf(ParserDbfConfig config, TableToRdf tableToRdf,
            DPUContext context) {
        this.config = config;
        this.tableToRdf = tableToRdf;
        this.context = context;
    }

    @Override
    public void parse(File inFile) throws OperationFailedException, ParseFailed {
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
        //
        // get header
        //        
        final List<String> header;
        if (config.hasHeader) {
            final DbfHeader dbfHeader = reader.getHeader();
            header = new ArrayList<>(dbfHeader.getFieldsCount());
            for (int i = 0; i < dbfHeader.getFieldsCount(); ++i) {
                final DbfField field = dbfHeader.getField(i);
                header.add(field.getName());
            }
        } else {
            header = null;
        }
        //
        // prase other rows
        //
        int rowNumber = config.hasHeader ? 2 : 1;
        Object[] row = reader.nextRecord();
        // configure parser
        TableToRdfConfigurator.configure(tableToRdf, header, Arrays.asList(row));
        // go ...
        while (row != null
                && (config.rowLimit == null || rowNumber < config.rowLimit)
                && !context.canceled()) {
            tableToRdf.paserRow(Arrays.asList(row), rowNumber);
            // read next row
            rowNumber++;
            row = reader.nextRecord();
            // log
            if ((rowNumber % 1000) == 0) {
                LOG.debug("Row number {} processed.", rowNumber);
            }
        }
    }

}
