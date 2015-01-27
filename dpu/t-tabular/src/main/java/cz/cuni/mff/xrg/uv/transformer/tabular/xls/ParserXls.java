package cz.cuni.mff.xrg.uv.transformer.tabular.xls;

import cz.cuni.mff.xrg.uv.transformer.tabular.column.ColumnType;
import cz.cuni.mff.xrg.uv.transformer.tabular.column.NamedCell_V1;
import cz.cuni.mff.xrg.uv.transformer.tabular.mapper.TableToRdf;
import cz.cuni.mff.xrg.uv.transformer.tabular.mapper.TableToRdfConfigurator;
import cz.cuni.mff.xrg.uv.transformer.tabular.parser.ParseFailed;
import cz.cuni.mff.xrg.uv.transformer.tabular.parser.Parser;
import eu.unifiedviews.dpu.DPUContext;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.uv.boost.serialization.rdf.SimpleRdfException;

/**
 *
 * @author Škoda Petr
 */
public class ParserXls implements Parser {

    private static final Logger LOG = LoggerFactory.getLogger(ParserXlsConfig.class);

    /**
     * Name of column where sheet name is stored.
     */
    public static final String SHEET_COLUMN_NAME = "__SheetName__";

    private final ParserXlsConfig config;

    private final TableToRdf tableToRdf;

    private final DPUContext context;

    private int rowNumber = 0;

    public ParserXls(ParserXlsConfig config, TableToRdf tableToRdf,
            DPUContext context) {
        this.config = config;
        this.tableToRdf = tableToRdf;
        this.context = context;
    }

    @Override
    public void parse(File inFile) throws ParseFailed, SimpleRdfException {
        final Workbook wb;
        try {
            wb = WorkbookFactory.create(inFile);
        } catch (IOException | InvalidFormatException ex) {
            throw new ParseFailed("WorkbookFactory creation failed.", ex);
        }
        // Get sheets to process.
        final List<Integer> toProcess = new LinkedList<>();
        for (Integer index = 0; index < wb.getNumberOfSheets(); ++index) {
            if (config.sheetName == null || config.sheetName.compareTo(wb.getSheetName(index)) == 0) {
                toProcess.add(index);
            }
        }
        // Process selected sheets.
        for (Integer sheetIndex : toProcess) {
            if (context.canceled()) {
                break;
            }
            parseSheet(wb, sheetIndex);
        }
    }

    /**
     * Parse given sheet.
     *
     * @param wb
     * @param sheetIndex
     * @throws cz.cuni.mff.xrg.uv.transformer.tabular.parser.ParseFailed
     * @throws cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.OperationFailedException
     */
    public void parseSheet(Workbook wb, Integer sheetIndex) throws ParseFailed, SimpleRdfException {
        LOG.debug("parseSheet({}, {})", wb.getSheetName(sheetIndex), sheetIndex);
        // For every row.
        final Sheet sheet = wb.getSheetAt(sheetIndex);
        int dataEndAtRow = sheet.getLastRowNum();
        if (config.numberOfStartLinesToIgnore > dataEndAtRow) {
            // No data to parse, terminate.
            return;
        }
        // Generate column names.
        int startRow = config.numberOfStartLinesToIgnore;
        List<String> columnNames;
        if (config.hasHeader) {
            // Parse line for header.
            final Row row = sheet.getRow(startRow++);
            if (row == null) {
                throw new ParseFailed("Header row is null!");
            }
            int columnEnd = row.getLastCellNum();
            columnNames = new ArrayList<>(columnEnd);
            for (int columnIndex = 0; columnIndex <= columnEnd; columnIndex++) {
                final Cell cell = row.getCell(columnIndex);
                if (cell == null) {
                    throw new ParseFailed("Header cell is null!");
                } else {
                    final String name = this.getCellValue(cell);
                    columnNames.add(name);
                }
            }
            // Global names will be added later.
        } else {
            columnNames = null;
        }
        // Prepare static cells -> xls enable reference for static cells.
        final List<String> namedCells = new LinkedList<>();
        for (NamedCell_V1 namedCell : config.namedCells) {
            final Row row = sheet.getRow(namedCell.getRowNumber() - 1);
            if (row == null) {
                throw new ParseFailed("Row for named cell is null! (" + namedCell.getName() + ")");
            }
            final Cell cell = row.getCell(namedCell.getColumnNumber() - 1);
            if (cell == null) {
                throw new ParseFailed("Cell for named cell is null! (" + namedCell.getName() + ")");
            }
            // Get value and add to namedCells.
            final String value = getCellValue(cell);
            LOG.debug("static cell {} = {}", namedCell.getName(), value);
            namedCells.add(value);
        }
        // Parse data file row by row.
        if (config.rowLimit == null) {
            LOG.debug("Row limit: not used");
        } else {
            LOG.debug("Row limit: {}", config.rowLimit);
        }
        // Set if for first time or if we use static row counter.
        if (!config.checkStaticRowCounter || rowNumber == 0) {
            rowNumber = config.hasHeader ? 2 : 1;
        }
        boolean headerGenerated = false;
        if (config.rowLimit != null) {
            // Limit number of lines.
            dataEndAtRow = startRow + config.rowLimit;
        }
        for (Integer rowNumPerFile = startRow; rowNumPerFile < dataEndAtRow; ++rowNumber, ++rowNumPerFile) {
            if (context.canceled()) {
                break;
            }
            // Skip given number of lines data.
            if (rowNumPerFile < config.numberOfStartLinesToIgnore) {
                continue;
            }
            // Get row.
            final Row row = sheet.getRow(rowNumPerFile);
            if (row == null) {
                continue;
            }
            int columnEnd = row.getLastCellNum();
            // Generate header if needed.
            if (!headerGenerated) {
                headerGenerated = true;
                // Use row data to generate types.
                final List<ColumnType> types = new ArrayList<>(columnEnd + namedCells.size() + 1);
                for (int columnIndex = 0; columnIndex <= columnEnd; columnIndex++) {
                    final Cell cell = row.getCell(columnIndex);
                    if (cell == null) {
                        types.add(null);
                        continue;
                    }
                    types.add(getCellType(cell));
                }
                if (columnNames == null) {
                    columnNames = new ArrayList<>(columnEnd);
                    // Generate column names.
                    for (int columnIndex = 0; columnIndex <= columnEnd; columnIndex++) {
                        columnNames.add("col" + Integer.toString(columnIndex + 1));
                    }
                }
                // Add user defined names - extend row for static cell.
                for (NamedCell_V1 item : config.namedCells) {
                    columnNames.add(item.getName());
                    types.add(ColumnType.String);
                }
                // Add global types and names.
                columnNames.add(SHEET_COLUMN_NAME);
                types.add(ColumnType.String);
                // Configure.
                TableToRdfConfigurator.configure(tableToRdf, columnNames, (List) types);
            }
            // Prepare row.
            final List<String> parsedRow = new ArrayList<>(columnEnd + namedCells.size() + 1);
            // Parse columns in row.
            for (int columnIndex = 0; columnIndex <= columnEnd; columnIndex++) {
                final Cell cell = row.getCell(columnIndex);
                if (cell == null) {
                    parsedRow.add(null);
                } else {
                    parsedRow.add(getCellValue(cell));
                }
            }
            // Add named columns first !!
            parsedRow.addAll(namedCells);
            // Add global data.
            parsedRow.add(wb.getSheetName(sheetIndex));
            // Convert row into RDF.
            tableToRdf.paserRow((List) parsedRow, rowNumber);
            if ((rowNumPerFile % 1000) == 0) {
                LOG.debug("Row number {} processed.", rowNumPerFile);
            }
        }
    }

    /**
     * Get value of given cell.
     *
     * @param cell
     * @return
     * @throws IllegalArgumentException
     */
    private String getCellValue(Cell cell) throws IllegalArgumentException {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_BLANK:
                return null;
            case Cell.CELL_TYPE_BOOLEAN:
                if (cell.getBooleanCellValue()) {
                    return "true";
                } else {
                    return "false";
                }
            case Cell.CELL_TYPE_ERROR:
            case Cell.CELL_TYPE_FORMULA:
                throw new IllegalArgumentException("Wrong cell type: " + cell.getCellType());
            case Cell.CELL_TYPE_NUMERIC:
                return Double.toString(cell.getNumericCellValue());
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue();
            default:
                throw new IllegalArgumentException("Unknown cell type: " + cell.getCellType());
        }
    }

    /**
     * Return type for based on given cell.
     *
     * @param cell
     * @return
     * @throws IllegalArgumentException
     */
    private ColumnType getCellType(Cell cell) throws IllegalArgumentException {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_BLANK:
                return null;
            case Cell.CELL_TYPE_BOOLEAN:
                return ColumnType.Boolean;
            case Cell.CELL_TYPE_ERROR:
                throw new IllegalArgumentException("Cell type is error.");
            case Cell.CELL_TYPE_FORMULA:
                throw new IllegalArgumentException("The cell contains a formula: " + cell.getCellFormula());
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return ColumnType.Date;
                } else {
                    String value = (new Double(cell.getNumericCellValue())).toString();
                    try {
                        Integer.parseInt(value);
                    } catch (NumberFormatException e) {
                        return ColumnType.Double;
                    }
                    return ColumnType.Integer;
                }
            case Cell.CELL_TYPE_STRING:
                return ColumnType.String;
            default:
                throw new IllegalArgumentException("Unknown cell type.");
        }
    }

}
