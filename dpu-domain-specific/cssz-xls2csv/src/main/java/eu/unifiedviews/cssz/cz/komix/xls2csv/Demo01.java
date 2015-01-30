package eu.unifiedviews.cssz.cz.komix.xls2csv;

import eu.unifiedviews.cssz.Xls2csv;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Demo01 {

    static {
    }
    public String fileName = "DEFAULT";
    public List<String> outputFilePathList = new ArrayList();
    public String FILEPATH = "";// "C:\\Data\\KMX\\Rocenka\\";
    public String TEMPLATE_PREFIX = "SABLONA_";
    public String PREFIX = "\\%\\%";
    public String SUFFIX = "\\%\\%";
    public boolean ONE_LIST_ONLY = false;

    private HSSFWorkbook wbTemplate, wbData;
    private HSSFSheet shTemplate;
    private HSSFFormulaEvaluator formEval;

    private DimenzeBox dimBox;
    private FaktBox faktBox;
    private List<Link> linkBox;
    //private Map<Integer, List<Dimenze>> asgnBox;
    private Map<Integer, List<Integer>> groupBox; //  GROUPs
    public Map<Integer, String> souborNames;
    public Map<Integer, String> faktCubeNames;
    
        private static final Logger log = LoggerFactory.getLogger(Demo01.class);

    
    public static void main(String args[]) {
        Demo01 demo = new Demo01();
        if (args.length == 1) {
            demo.fileName = args[0];
        } else {
            throw new RuntimeException("Pouziti: java -jar transform.jar <KOREN_NAZVU>");
        }

       
        demo.souborNames = new HashMap<Integer, String>();
        demo.faktCubeNames = new HashMap<Integer, String>();
        try {
            Properties props = new Properties();
            props.load(Demo01.class.getResourceAsStream("/xml2csv.properties"));
//        Properties prop = new Properties();
//        prop.load(appContext.getResource("classpath:cz/ozp/csob_ozp.properties").getInputStream());
            if (props.getProperty("FILEPATH") != null)
                demo.FILEPATH = props.getProperty("FILEPATH");
            if (props.getProperty("TEMPLATE_PREFIX") != null)
                demo.TEMPLATE_PREFIX = props.getProperty("TEMPLATE_PREFIX");
            if (props.getProperty("PREFIX") != null)
                demo.PREFIX = props.getProperty("PREFIX");
            if (props.getProperty("SUFFIX") != null)
                demo.SUFFIX = props.getProperty("SUFFIX");
            demo.init();
            demo.parse();
            demo.save();
        } catch (IOException e) {
            log.debug("Nepovedlo se otevrit vstupni nebo vystupni soubory");
            e.printStackTrace();
        }
    }
    

    public Demo01() {
        faktBox = new FaktBox(this);
//        asgnBox = new HashMap<Integer, List<Dimenze>>();
        groupBox = new HashMap<Integer, List<Integer>>();
    }

    /**
     * Otevira vstupni soubory
     * @throws IOException
     */
    public void init() throws IOException {
        log.info("Statisticka rocenka (c) 2014 - v0.8 (2014-09-02)");
        wbData = new HSSFWorkbook(new FileInputStream(FILEPATH + fileName + ".xls"));
        wbTemplate = new HSSFWorkbook(new FileInputStream(FILEPATH + TEMPLATE_PREFIX + fileName + ".xls"));
        log.debug("Number of sheets in template: " + wbTemplate.getNumberOfSheets());
    }

    /**
     * Parsuje soubor se sablonou a na zaklade nalezenych znacek
     * nastavuje datovou bazi a/nebo provadi vstupne-vystupni ukony.
     */
    public void parse() {
        log.info("Starting parsing XLS documents..");
        for (int sheetNumber=0; sheetNumber < wbTemplate.getNumberOfSheets(); sheetNumber++) {
            log.debug("JDU NA SHEET: " + sheetNumber);
            // Every sheet has its own dimension descriptors (e.g. column/row headers)
            dimBox = new DimenzeBox();
            linkBox = new ArrayList<Link>();
            shTemplate = wbTemplate.getSheetAt(sheetNumber);
            log.debug("Number of data rows ["+ shTemplate.getSheetName()+"]: " + shTemplate.getPhysicalNumberOfRows() +
                    " (" + shTemplate.getFirstRowNum() + " - " + shTemplate.getLastRowNum()+")");
            Iterator<Row> rowTemplateIter = shTemplate.rowIterator();
            while (rowTemplateIter.hasNext()) {
                HSSFRow rowTemplate = shTemplate.getRow(rowTemplateIter.next().getRowNum());
                Iterator<Cell> cellTemplateIter = rowTemplate.cellIterator();
                while (cellTemplateIter.hasNext()) {
                    HSSFCell cellTemplate = rowTemplate.getCell(cellTemplateIter.next().getColumnIndex());
                    if (cellTemplate.getCellType() == Cell.CELL_TYPE_STRING) {
                        String cellValue = cellTemplate.getStringCellValue();
//                            log.debug("["+cellTemplate.getColumnIndex() + ","+rowTemplate.getRowNum()+"] " + cellValue);
                        // FAKT:
                        if (cellValue.matches(PREFIX + "F[0-9]+" + SUFFIX)) {
                            parseFakt(sheetNumber, cellTemplate);
                        }
                        // DIMENZE:
                        else if (cellValue.matches(PREFIX + "D[0-9]+(_\\(.+\\))?" + SUFFIX)) {
                            parseDimenze(sheetNumber, cellTemplate);
                        }
                        // PRIRAZENI DIMENZI FAKTU
                        else if (cellValue.matches(PREFIX + "P([0-9]+)_\\((D[0-9]+[,])*D[0-9]+\\)" + SUFFIX)) {
                            parseAsgn(cellTemplate);
                        }
                        // SOUBOR PRO VYSTUP FAKTU
                        else if (cellValue.matches(PREFIX + "S([0-9]+)_\\(([^)]*)\\)" + SUFFIX)) {
                            log.debug("S");
                            parseSoubor(cellTemplate);
                        }
                        // SOUBOR PRO PRIRAZENI FAKTU KE KOSTCE -- %%C_(datova-kostka-01#1,2)%%
                        else if (cellValue.matches(PREFIX + "C_\\(([^)]*)[#]([^)]*)\\)" + SUFFIX)) {
                            log.debug("C");
                            parseCubename(cellTemplate);
                        }
                        // GRUPA LISTU SE STEJNOU SABLONOU
                        else if (cellValue.matches(PREFIX + "G([0-9]+)_\\((([0-9]+[,])*[0-9]+)\\)" + SUFFIX)
                                || cellValue.matches(PREFIX + "G_\\(ALL\\)\" + SUFFIX")) {
                            log.debug("G");
                            parseGroup(cellTemplate);
                        }
                        // ODKAZ NA JINOU BUNKU
                        else if (cellValue.matches(PREFIX + "B_\\(([A-Z]),([0-9]+)\\)" + SUFFIX)) {
                            parseOdkaz(sheetNumber, cellTemplate);
                        } else if (cellValue.startsWith("%%")) {
                            log.debug("UNMATCHED: " + cellValue);
                        }
                    }
                }
            }
            if (ONE_LIST_ONLY) break; // TODO - projit vsechny
        }
    }

    public void save() {
        log.info("About to save output files..");
        for (Fakt f: faktBox.box) {
            String outputFilePath = f.saveToFile(FILEPATH, fileName);
            outputFilePathList.add(outputFilePath);
        }
    }

    String getCellFormatedData(int sheetNumber, int colIndex, int rowNum) {
        if (wbData == null || wbData.getSheetAt(sheetNumber) == null ||
                wbData.getSheetAt(sheetNumber).getRow(rowNum) == null ||
                wbData.getSheetAt(sheetNumber).getRow(rowNum).getCell(colIndex) == null) {
            return "## NULL ##";
        }
        HSSFCell cell = wbData.getSheetAt(sheetNumber).getRow(rowNum).getCell(colIndex);
        if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
            return cell.getStringCellValue().trim();
        }
        if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            return "" + cell.getNumericCellValue(); //cell.getCellStyle().getDataFormatString();
        }
        if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
            if (cell.getCachedFormulaResultType() == Cell.CELL_TYPE_STRING) {
                return cell.getStringCellValue().trim();
            }
            if (cell.getCachedFormulaResultType() == Cell.CELL_TYPE_NUMERIC) {
                return "" + cell.getNumericCellValue();
            }
            return "## UnKnownCachedType: "+ cell.getCachedFormulaResultType()+ " ##";
        }
        return "## UnKnownType: "+cell.getCellType()+" ##";
    }

    CellRangeAddress getMergedRegion(HSSFCell cell, HSSFSheet sheet) {
        for (int i=0; i < sheet.getNumMergedRegions(); i++) {
            if (sheet.getMergedRegion(i).isInRange(cell.getRowIndex(), cell.getColumnIndex())) {
                return sheet.getMergedRegion(i);
            }
        }
        return null;
    }

    private void parseFakt(int sheetNumber, HSSFCell cellTemplate) {
        String cellValue = cellTemplate.getStringCellValue();
        String factId = cellValue.replaceAll(PREFIX + "F([0-9]+)" + SUFFIX, "$1");

        List<Dimenze> dims = dimBox.getDimenze(cellTemplate.getColumnIndex(),
                cellTemplate.getRow().getRowNum(), linkBox);
        log.debug("FAKT SH: " + sheetNumber
                + ", COL: " +  cellTemplate.getColumnIndex() + ", ROW: " +  cellTemplate.getRow().getRowNum() +
                ": " + faktBox.getTextDimenzi(Integer.parseInt(factId), wbData.getSheetAt(sheetNumber), dims, cellTemplate));
        String textDimenzi = "\"" + getCellFormatedData(
                sheetNumber, cellTemplate.getColumnIndex(), cellTemplate.getRow().getRowNum()) +
                "\"," + faktBox.getTextDimenzi(Integer.parseInt(factId), wbData.getSheetAt(sheetNumber), dims, cellTemplate);
        faktBox.saveFakt(
                Integer.parseInt(factId), textDimenzi, dims);

        for (Integer groupId: groupBox.keySet()) {
            // Neosetreno - je-li v nektere skupine tento sheet jako prvni (zdroj definice), pak:
            if (groupBox.get(groupId).get(0).intValue() == sheetNumber) {
                // Zpracuj zbyvajici sheety stejnou definici, jako je aktualni (jiz zpracovana)
                for (int nextSheet=1; nextSheet < groupBox.get(groupId).size(); nextSheet++) {
                    textDimenzi = "\"" + getCellFormatedData(
                            groupBox.get(groupId).get(nextSheet), cellTemplate.getColumnIndex(), cellTemplate.getRow().getRowNum()) +
                            "\"," + faktBox.getTextDimenzi(Integer.parseInt(factId), wbData.getSheetAt(groupBox.get(groupId).get(nextSheet)),
                            dims, cellTemplate);
                    faktBox.saveFakt(
                            Integer.parseInt(factId), textDimenzi, dims);
                }
            }
        }
    }

    private void parseDimenze(int sheetNumber, HSSFCell cellTemplate) {
        String cellValue = cellTemplate.getStringCellValue();
        String dimId = cellValue.replaceAll(PREFIX + "D([0-9]+)(_\\(.+\\))?" + SUFFIX, "$1");
        String dimName = "D" + dimId;
        String konstanta = null;
        if (cellValue.matches(PREFIX + "D[0-9]+_\\(.+\\)" + SUFFIX)) {
            dimName = cellValue.replaceAll(PREFIX + "D[0-9]+_\\((.+)\\)" + SUFFIX, "$1");
            if (dimName.contains("#")) {
                String[] casti = dimName.split("[#]");
                if (casti.length != 2) throw new RuntimeException("Spatny syntaxe v textu dimenzi @ " + cellTemplate);
                if ("".equals(casti[0])) {
                    dimName = "D" + dimId; // DEFAULT
                } else {
                    dimName = casti[0];
                }
                log.debug("Nasel jsem konstantu dimenze " + dimName + ": " + casti[1]);
                konstanta = casti[1];
            }
        }
        CellRangeAddress merge = getMergedRegion(cellTemplate, shTemplate);
        if (merge != null) {
            dimBox.saveDimenze(new Dimenze(Integer.parseInt(dimId), dimName, konstanta, merge.getFirstColumn(),
                    merge.getLastColumn(), merge.getFirstRow(), merge.getLastRow()));
        } else {
            dimBox.saveDimenze(new Dimenze(Integer.parseInt(dimId), dimName, konstanta,
                    cellTemplate.getColumnIndex(), cellTemplate.getRow().getRowNum()));
        }
    }

    private void parseOdkaz(int sheetNumber, HSSFCell cellTemplate) {
        String cellValue = cellTemplate.getStringCellValue();
        CellRangeAddress merge = getMergedRegion(cellTemplate, shTemplate);
        if (merge != null) {
            linkBox.add(new Link(cellValue, merge.getFirstColumn(),
                    merge.getLastColumn(), merge.getFirstRow(), merge.getLastRow()));
        } else {
            linkBox.add(new Link(cellValue, cellTemplate.getColumnIndex(),
                    cellTemplate.getRow().getRowNum()));
        }

    }

    private void parseAsgn(HSSFCell cellTemplate) {
        String cellValue = cellTemplate.getStringCellValue();
        String sFaktId = cellValue.replaceAll(PREFIX + "P([0-9]+)_\\(((D[0-9]+[,])*D[0-9]+)\\)" + SUFFIX, "$1");
        String sDimIds = cellValue.replaceAll(PREFIX + "P([0-9]+)_\\(((D[0-9]+[,])*D[0-9]+)\\)" + SUFFIX, "$2");
        List<Integer> dims = new ArrayList<Integer>();
        String[] sDims = sDimIds.split(",");
        for (String sDim: sDims) {
            dims.add(Integer.parseInt(sDim.substring(1)));
        }
        faktBox.saveFiltr(Integer.parseInt(sFaktId), dims);
        log.debug("ASGN: Fakt " + sFaktId + " --> " + dims);
    }

    private void parseGroup(HSSFCell cellTemplate) {
        String cellValue = cellTemplate.getStringCellValue();
        Integer groupId = null;
        List<Integer> sheets = new ArrayList<Integer>();
        if (cellValue.matches(PREFIX + "G([0-9]+)_\\((([0-9]+[,])*[0-9]+)\\)" + SUFFIX)) {
            groupId = Integer.parseInt(cellValue.replaceAll(PREFIX + "G([0-9]+)_\\((([0-9]+[,])*[0-9]+)\\)" + SUFFIX, "$1"));
            String sSheetIds = cellValue.replaceAll(PREFIX + "G([0-9]+)_\\((([0-9]+[,])*[0-9]+)\\)" + SUFFIX, "$2");
            log.debug("sSheetIds: " + sSheetIds);
            log.debug("sSheetIds $3: " + cellValue.replaceAll(PREFIX + "G([0-9]+)_\\((([0-9]+[,])*[0-9]+)\\)" + SUFFIX, "$3"));
            String[] sSheets = sSheetIds.split(",");
            for (String sSheet: sSheets) {
                sheets.add(Integer.parseInt(sSheet)-1);
            }
        } else if (cellValue.matches(PREFIX + "G_\\(ALL\\)" + SUFFIX)) {
            groupId = 0;
            for (int i=0; i<wbTemplate.getNumberOfSheets(); i++) {
                sheets.add(new Integer(i));
            }
        }
        if (groupId == null || sheets.isEmpty()) throw new RuntimeException("Nekompletni data pro definici grupy");
        if (groupBox.containsKey(groupId)) throw new RuntimeException("Duplicitni cislo Grupy : " + groupId);
        groupBox.put(groupId, sheets);
        log.debug("GROUP: gid #" + groupId + " --> " + sheets);
    }

    private void parseSoubor(HSSFCell cellTemplate) {
        String cellValue = cellTemplate.getStringCellValue();
        String sFaktId = cellValue.replaceAll(PREFIX + "S([0-9]+)_\\(([^)]*)\\)" + SUFFIX, "$1");
        String sFilename = cellValue.replaceAll(PREFIX + "S([0-9]+)_\\(([^)]*)\\)" + SUFFIX, "$2");
        log.debug("SOUBOR: Fakt " + sFaktId + " --> " + sFilename);
        souborNames.put(Integer.parseInt(sFaktId),sFilename);
    }

    private void parseCubename(HSSFCell cellTemplate) {
        String cellValue = cellTemplate.getStringCellValue();
        String cubeName = cellValue.replaceAll(PREFIX + "C_\\(([^)]*)[#]([^)]*)\\)" + SUFFIX, "$1");
        String sFaktIds = cellValue.replaceAll(PREFIX + "C_\\(([^)]*)[#]([^)]*)\\)" + SUFFIX, "$2");
        String asFaktIds[] = sFaktIds.split(",");
        for (int i = 0; i < asFaktIds.length; i++) {
            faktCubeNames.put(Integer.parseInt(asFaktIds[i]),cubeName);
            log.debug("CUBENAME: Fakt " + Integer.parseInt(asFaktIds[i]) + " --> " + cubeName);
        }
    }
}
