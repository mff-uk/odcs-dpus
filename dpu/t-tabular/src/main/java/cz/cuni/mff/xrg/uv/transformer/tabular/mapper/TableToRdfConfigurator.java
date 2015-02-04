package cz.cuni.mff.xrg.uv.transformer.tabular.mapper;

import cz.cuni.mff.xrg.uv.transformer.tabular.TabularConfig_V2;
import cz.cuni.mff.xrg.uv.transformer.tabular.TabularOntology;
import cz.cuni.mff.xrg.uv.transformer.tabular.Utils;
import cz.cuni.mff.xrg.uv.transformer.tabular.column.ColumnInfo_V1;
import cz.cuni.mff.xrg.uv.transformer.tabular.column.ColumnType;
import cz.cuni.mff.xrg.uv.transformer.tabular.column.ValueGenerator;
import cz.cuni.mff.xrg.uv.transformer.tabular.column.ValueGeneratorReplace;
import cz.cuni.mff.xrg.uv.transformer.tabular.parser.ParseFailed;
import java.util.*;
import org.openrdf.model.vocabulary.XMLSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.uv.boost.dpu.context.UserContext;
import cz.cuni.mff.xrg.uv.boost.serialization.rdf.SimpleRdfException;

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
     * @param data       Contains first data row, or ColumnType if type is already known.
     * @param ctx
     * @throws cz.cuni.mff.xrg.uv.transformer.tabular.parser.ParseFailed
     * @throws cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.OperationFailedException
     */
    public static void configure(TableToRdf tableToRdf, List<String> header, List<Object> data,
            UserContext ctx) throws ParseFailed, SimpleRdfException {
        // Initial checks,
        if (data == null) {
            throw new ParseFailed("First data row is null!");
        }
        if (header != null && header.size() != data.size()) {
            throw new ParseFailed("Diff number of cells in header ("
                    + header.size() + ") and data (" + data.size() + ")");
        }
        final TableToRdfConfig config = tableToRdf.config;
        // Clear configuration.
        tableToRdf.baseUri = config.baseURI;
        tableToRdf.infoMap = null;
        tableToRdf.keyColumn = null;
        tableToRdf.nameToIndex = new HashMap<>();
        // Prepare locals variables.
        Map<String, ColumnInfo_V1> unused = new HashMap<>();
        unused.putAll(config.columnsInfo);
        List<ValueGenerator> valueGenerators = new ArrayList<>(data.size());
        // Generate configuration - Column Mapping.
        String keyTemplateStr = null;
        for (int index = 0; index < data.size(); index++) {
            // Generate column name and add it to map.
            final String columnName;
            if (header != null) {
                columnName = header.get(index);
            } else {
                // Use generated one - first is col1, col2 ...
                columnName = "col" + Integer.toString(index + 1);
            }
            LOG.debug("New column found '{}'", columnName);
            // Check for null.
            if (columnName == null) {
                LOG.warn("Column with name='null' is ignored.");
                continue;
            }
            // Add column name.
            tableToRdf.nameToIndex.put(columnName, index);
            // Test for key.
            if (config.keyColumn != null && !config.advancedKeyColumn
                    && config.keyColumn.compareTo(columnName) == 0) {
                // we construct tempalte and use it
                keyTemplateStr = "<" + prepareAsUri("{", config) + columnName + "}>";
            }
            // Check for user template.
            final ColumnInfo_V1 columnInfo;
            if (config.columnsInfo.containsKey(columnName)) {
                // Ise user config.
                columnInfo = config.columnsInfo.get(columnName);
                unused.remove(columnName);
            } else {
                if (!config.generateNew) {
                    // Do not generate new template - ignore this column.
                    continue;
                } else {
                    // Generate template for this column.
                    columnInfo = new ColumnInfo_V1();
                }
            }
            // Fill other values into configuration if needed.
            if (columnInfo.getURI() == null) {
                columnInfo.setURI(config.baseURI + Utils.convertStringToURIPart(columnName));
            } else {
                columnInfo.setURI(prepareAsUri(columnInfo.getURI(), config));
            }
            if (columnInfo.getType() == ColumnType.Auto) {
                if (config.autoAsStrings) {
                    columnInfo.setType(ColumnType.String);
                } else {
                    columnInfo.setType(guessType(columnName, data.get(index), columnInfo.isUseTypeFromDfb()));
                }
            }
            // Generate tableToRdf configuration from 'columnInfo'.
            final String template = generateTemplate(columnInfo, columnName);
            LOG.debug("Template for column '{}' is '{}'", columnName, template);
            // Add to configuration.
            valueGenerators.add(ValueGeneratorReplace.create(
                    tableToRdf.valueFactory.createURI(columnInfo.getURI()), template));
            // Generate metadata about column - for now only labels.
            if (config.generateLabels) {
                tableToRdf.outRdf.add(
                        tableToRdf.valueFactory.createURI(columnInfo.getURI()),
                        ctx.getOntology().get(TabularOntology.RDF_ROW_LABEL),
                        tableToRdf.valueFactory.createLiteral(columnName));
            }
        }
        // Do we also use template for key (row subject)?
        if (config.advancedKeyColumn) {
            // Use keyColumn directly.
            tableToRdf.keyColumn = ValueGeneratorReplace.create(null, config.keyColumn);
            tableToRdf.keyColumn.compile(tableToRdf.nameToIndex,
                    tableToRdf.valueFactory);
        } else if (keyTemplateStr != null) {
            // We have consructed tempalte.
            LOG.info("Key column template: {}", keyTemplateStr);
            tableToRdf.keyColumn = ValueGeneratorReplace.create(null, keyTemplateStr);
            tableToRdf.keyColumn.compile(tableToRdf.nameToIndex, tableToRdf.valueFactory);
        } else {
            // We use null -> then row number is used as a key subject.
        }
        // Write info about ignored columns.
        for (String key : unused.keySet()) {
            if (key.isEmpty()) {
                // Bug fix for empty keys in configurations, they were caused by certain version of
                // tabular configuration.
                continue;
            }
            LOG.warn("Column '{}' (uri:{}) ignored as does not match original columns.", key,
                    unused.get(key).getURI());
        }
        // Add advanced templates.
        for (TabularConfig_V2.AdvanceMapping item : tableToRdf.config.columnsInfoAdv) {
            // Prepare URI.
            String uri = prepareAsUri(item.getUri(), config);
            // Add tempalte.
            valueGenerators.add(ValueGeneratorReplace.create(tableToRdf.valueFactory.createURI(uri),
                    item.getTemplate()));
        }
        // Compile valueGenerators.
        for (ValueGenerator generator : valueGenerators) {
            generator.compile(tableToRdf.nameToIndex, tableToRdf.valueFactory);
        }
        // Final checks and data sets.
        tableToRdf.infoMap = valueGenerators.toArray(new ValueGenerator[0]);
        if (config.rowsClass != null && !config.rowsClass.isEmpty()) {
            try {
                tableToRdf.rowClass = tableToRdf.valueFactory.createURI(config.rowsClass);
            } catch (IllegalArgumentException ex) {
                throw new ParseFailed("Failed to create row's class URI from:" + config.rowsClass, ex);
            }
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
    private static ColumnType guessType(String columnName, Object value, Boolean useDataType) {

        if (value instanceof ColumnType) {
            ColumnType type = (ColumnType) value;
            if (type == ColumnType.Auto) {
                throw new RuntimeException("ColumnType.Auto!");
            }
            return type;
        }

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
        // Try to parse value.
        if (value == null) {
            // We can gues the value, as we can not read it from first row ..
            LOG.warn("Can't determine type for: {} as value in first row is empty, string used as default.",
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

        // TODO Petr:Parse Date
        // If we got here, we do not know so we use string as reasonable detault.
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
        // Update columnName.
        columnName = columnName.replaceAll("\\{", "\\\\{").replaceAll("\\}", "\\\\}");

        final String placeHolder = "\"{" + columnName + "}\"";
        switch (columnInfo.getType()) {
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
                if (columnInfo.getLanguage() == null || columnInfo.getLanguage().isEmpty()) {
                    return placeHolder;
                } else {
                    return placeHolder + "@" + columnInfo.getLanguage();
                }
            case gYear:
                return placeHolder + "^^" + XMLSchema.GYEAR;
            case Decimal:
                return placeHolder + "^^" + XMLSchema.DECIMAL;
            default:
                LOG.error("No type used for: {}", columnName);
                return placeHolder;

        }
    }

    /**
     * Prepare URI to be used. If given uri is absolute then return it if it's relative then config.baseURI is
     * used to resolve the uri
     *
     * @param uri
     * @param config
     * @return
     */
    private static String prepareAsUri(String uri, TableToRdfConfig config) {
        if (uri.contains("://")) {
            // Uri is absolute like http://, file://
            return uri;
        } else {
            final String newUri;
            // Uri is relative, concat with base URI just be carefull about the ending '/'
            if (uri.startsWith("/")) {
                if (config.baseURI.endsWith("/")) {
                    // Both have '/'
                    newUri = config.baseURI + uri.substring(1);
                } else {
                    // Just one has '/'
                    newUri = config.baseURI + uri;
                }
            } else {
                if (config.baseURI.endsWith("/")) {
                    // Just one has '/'
                    newUri = config.baseURI + uri;
                } else {
                    // One one has '/'
                    newUri = config.baseURI + "/" + uri;
                }
            }
            LOG.debug("URI '{}' -> '{}'", uri, newUri);
            return newUri;
        }
    }

}
