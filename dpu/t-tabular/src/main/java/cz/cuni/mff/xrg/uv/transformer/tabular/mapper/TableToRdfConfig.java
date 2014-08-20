package cz.cuni.mff.xrg.uv.transformer.tabular.mapper;

import cz.cuni.mff.xrg.uv.transformer.tabular.column.ColumnInfo_V1;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Škoda Petr
 */
public class TableToRdfConfig {

    /**
     * Name of column with key, null, or template.
     */
    final String keyColumn;

    /**
     * Base URI used to prefix generated URIs.
     */
    final String baseURI;

    /**
     * User configuration about parsing process.
     */
    final Map<String, ColumnInfo_V1> columnsInfo;

    /**
     * Advanced configuration about parsing.
     */
    final Map<String, String> columnsInfoAdv;

    /**
     * If true then new column, not specified in {@link #columnsInfo},
     * can be added.
     */
    final boolean generateNew;

    /**
     * Metadata for column - type.
     */
    final String rowsClass;

    final boolean ignoreBlankCells;

    final boolean advancedKeyColumn;

    final boolean generateLabels;

    final boolean generateRowTriple;

    public TableToRdfConfig(String keyColumnName, String baseURI,
            Map<String, ColumnInfo_V1> columnsInfo, boolean generateNew,
            String rowsClass, boolean ignoreBlankCells,
            Map<String, String> columnsInfoAdv,
            boolean advancedKeyColumn, boolean generateLabels,
            boolean generateRowTriple) {
        this.keyColumn = keyColumnName;
        this.baseURI = baseURI;
        this.columnsInfo = columnsInfo != null ? columnsInfo :
                new HashMap<String, ColumnInfo_V1>();
        this.generateNew = generateNew;
        this.rowsClass = rowsClass;
        this.ignoreBlankCells = ignoreBlankCells;
        this.columnsInfoAdv = columnsInfoAdv != null ? columnsInfoAdv :
                new HashMap<String, String>();;
        this.advancedKeyColumn = advancedKeyColumn;
        this.generateLabels = generateLabels;
        this.generateRowTriple = generateRowTriple;
    }

}
