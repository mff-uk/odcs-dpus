package cz.cuni.mff.xrg.uv.transformer.tabular.mapper;

import cz.cuni.mff.xrg.uv.transformer.tabular.Utils;
import cz.cuni.mff.xrg.uv.transformer.tabular.column.ColumnInfo_V1;
import cz.cuni.mff.xrg.uv.transformer.tabular.column.ColumnType;
import cz.cuni.mff.xrg.uv.transformer.tabular.column.ValueGenerator;
import cz.cuni.mff.xrg.uv.transformer.tabular.parser.ParseFailed;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.openrdf.model.vocabulary.XMLSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configure {@link TableToRdf} class.
 *
 * @author Škoda Petr
 */
public class TableToRdfConfigurator {

    private static final Logger LOG = LoggerFactory.getLogger(TableToRdfConfigurator.class);

    private TableToRdfConfigurator() {
    }

    /**
     * Configure given {@link TableToRdf} convertor.
     *
     * @param tableToRdf
     * @param header
     * @param data
     * @throws cz.cuni.mff.xrg.uv.transformer.tabular.parser.ParseFailed
     */
    public static void configure(TableToRdf tableToRdf, List<String> header, List<Object> data) throws ParseFailed {
        // initial checks
        if (data == null) {
            throw new ParseFailed("First data row is null!");
        }
        if (header != null && header.size() != data.size()) {
            throw new ParseFailed("Diff number of cells in header (" + header.size() + ") and data (" + data.size() + ")");
        }
        //
        final TableToRdfConfig config = tableToRdf.config;
        //
        // clear configuration
        //
        tableToRdf.baseUri = config.baseURI;
        tableToRdf.infoMap = null;
        tableToRdf.keyColumnIndex = null;
        tableToRdf.nameToIndex = new HashMap<>();
        //
        // prepare locals
        //
        List<ColumnInfo_V1> unused = new LinkedList<>();
        unused.addAll(config.columnsInfo.values());
        List<ValueGenerator> valueGenerator = new ArrayList<>(data.size());
        //
        // generate configuration - Column Mapping
        //
        for (int index = 0; index < data.size(); index++) {
            //
            // generate column name and add it to map
            //
            final String columnName;
            if (header != null) {
                columnName = header.get(index);
            } else {
                // use generated one - first is col1, col2 ... 
                columnName = "col" + Integer.toString(index + 1);
            }
            // add column name
            tableToRdf.nameToIndex.put("{" + columnName + "}", index);
            //
            // test for key
            //
            if (config.keyColumnName != null &&
                    columnName.compareTo(config.keyColumnName) == 0) {
                tableToRdf.keyColumnIndex = index;
            }
            //
            // check for user template
            //
            final ColumnInfo_V1 columnInfo;
            if (config.columnsInfo.containsKey(columnName)) {
                // use user config
                columnInfo = config.columnsInfo.get(columnName);
                unused.remove(columnInfo);
            } else {
                if (!config.generateNew) {
                    // no new generation
                    continue;
                } else {
                    // generate new
                    columnInfo = new ColumnInfo_V1();
                }
            }
            //
            // fill other values if needed
            //
            if (columnInfo.getURI() == null) {
                columnInfo.setURI(config.baseURI +
                        Utils.convertStringToURIPart(columnName));
            }
            if (columnInfo.getType() == ColumnType.Auto) {
                columnInfo.setType(guessType(columnName, data.get(index),
                        columnInfo.isUseTypeFromDfb()));
                LOG.debug("Type for {} is {}", columnName,
                        columnInfo.getType().toString());
            }
            //
            // generate tableToRdf configuration from 'columnInfo'
            //
            final String template = generateTemplate(columnInfo, columnName);
            //
            // add to configuration
            //
            valueGenerator.add(new ValueGenerator(
                tableToRdf.valueFactory.createURI(columnInfo.getURI()),
                template));
        }
        //
        // add columns from user - Template Mapping
        // TODO: we do not support this functionality ..
        for (ColumnInfo_V1 info : unused) {
            LOG.warn("Column <{}> ignored as does not match original columns.",
                    info.getURI());
        }
        //
        // final checks and data sets
        //
        tableToRdf.infoMap = valueGenerator.toArray(new ValueGenerator[0]);
        if (config.rowsClass != null) {
            tableToRdf.rowClass =
                    tableToRdf.valueFactory.createURI(config.rowsClass);
        }
    }

    /**
     * Auto type of given value.
     *
     * @param columnName
     * @param value
     * @param useDataType Null is considered to be false.
     * @return
     */
    private static ColumnType guessType(String columnName, Object value,
            Boolean useDataType) {

        if (useDataType != null && useDataType) {
            if (value instanceof Date) {
                return ColumnType.Date;
            }
            if (value instanceof Float) {
                return ColumnType.Float;
            }
            if (value instanceof Boolean) {
                return ColumnType.Boolean;
            }
            if (value instanceof Number) {
                return ColumnType.Long;
            }
        }
        //
        // Try to parse value
        //
        if (value == null) {
            // we can gues ..
            LOG.warn("Can't determine type for: {}, string used as default.",
                    columnName);
            return ColumnType.String;
        }

        final String valueStr = value.toString();
        try {
            Long.parseLong(valueStr);
            return ColumnType.Long;
        } catch (NumberFormatException ex) {

        }
        try {
            Double.parseDouble(valueStr);
            return ColumnType.Double;
        } catch (NumberFormatException ex) {

        }

        // TODO Parse Date

        // use string as default
        return ColumnType.String;
    }

    /**
     * Generate template for given colum.
     * 
     * @param columnInfo
     * @param columnName
     * @return
     */
    private static String generateTemplate(ColumnInfo_V1 columnInfo, String columnName) {
        final String placeHolder = "\"{" +columnName + "}\"";
        switch(columnInfo.getType()) {
            case Boolean:
                return placeHolder + "^^" + XMLSchema.BOOLEAN;
            case Date:
                return placeHolder + "^^" + XMLSchema.DATE;
            case Double:
                return placeHolder + "^^" + XMLSchema.DOUBLE;
            case Float:
                return placeHolder + "^^" + XMLSchema.FLOAT;
            case Integer:
                return placeHolder + "^^" + XMLSchema.INT;
            case Long:
                return placeHolder + "^^" + XMLSchema.LONG;
            case String:
                if (columnInfo.getLanguage() == null ||
                        columnInfo.getLanguage().isEmpty()) {
                    return placeHolder;
                } else {
                    return placeHolder + "@" + columnInfo.getLanguage();
                }
            default:
                LOG.error("No type used for: {}", columnName);
                return placeHolder;

        }
    }
}
