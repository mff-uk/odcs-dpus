package eu.unifiedviews.cssz.cz.komix.xls2csv;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.Cell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by hugo on 29.5.14.
 */
public class FaktBox {

    private static final Logger log = LoggerFactory.getLogger(Demo01.class);

    
    List<Fakt> box;
    Demo01 demo;

    public FaktBox(Demo01 demo) {
        this.box = new ArrayList<Fakt>();
        this.demo = demo;
    }

    public void saveFakt(int poradi, String dato, List<Dimenze> dimenze) {
        for (Fakt f: box) {
            if (f.poradi == poradi) {
                f.data.add(dato);
                f.saveDimenze(dimenze);
                //f.dimenze.saveDimenze(d);
                return; // Uz existuje
            }
        }
        Fakt f = new Fakt(poradi, demo);
        f.saveDimenze(dimenze);
        f.data.add(dato);
/*        for (Dimenze d: dimenze) {
            log.debug("???");
            //f.dimenze.saveDimenze(d);
        }*/
        box.add(f);
    }

    public void saveFiltr(int poradi, List<Integer> filtr) {
        for (Fakt f: box) {
            if (f.poradi == poradi) {
                if (f.filtr == null) {
                    f.filtr = filtr;
                } else {
                    throw new RuntimeException("Chyba - duplicitni prirazeni filtru k dimenzi " + poradi);
                }
            }
        }
        Fakt f = new Fakt(poradi, demo);
        f.filtr = filtr;
        box.add(f);
    }

    /**
     * Prevadi seznam nalezenych dimenzi na text hodnot pro fakt.
     * Pokud chybi pozadovana dimenze pro dany fakt, vyhodi vyjimku!
     *
     * @param poradi
     * @param sheet
     * @param box
     * @return
     */
    public String getTextDimenzi(int poradi, HSSFSheet sheet, List<Dimenze> dims, HSSFCell cellTemplate) {
        Fakt fakt = null;
        for (Fakt f: this.box) {
            if (f.poradi == poradi) {
                fakt = f;
                break;
            }
        }
        if (fakt == null) {
            fakt = new Fakt(poradi, demo);
            this.box.add(fakt);
            log.debug("Zakladam fakt #"+poradi+" pro dims: " + dims);
            fakt.filtr = new ArrayList<Integer>();
            for (Dimenze d: dims) fakt.filtr.add(new Integer(d.poradi));
        }
        if (fakt.filtr == null) throw new RuntimeException("Filtr pro fakt "+ poradi +" nebyl naplnen");
        String result = "";
//        log.debug("FILTR: " + fakt.filtr);
        for (int i: fakt.filtr) {
            boolean found = false;
            for (Dimenze e: dims) {
                if (i == e.poradi) {
                    found = true;
                    break;
                }
            }
            if (found == false) {
                throw new RuntimeException("Pro fakt " + poradi + " na souradnici R:" +
                        cellTemplate.getRow().getRowNum() + ", S: " + cellTemplate.getColumnIndex() +
                        ", sheet: " + sheet.getSheetName() +
                        " nebyla nalezena pozadovana dimenze " + i);
            }
        }
//        if (poradi == 4) log.debug("Fakt 04 : " + dims + " x " + fakt.filtr);
        for (Dimenze d: dims) {
            // Pokud NALEZENA dimenze neni ve filtru, vynecham ji ve vystupu
            boolean found = false;
            for (int i: fakt.filtr) {
                if (d.poradi == i) {
                    found = true;
                    break;
                }
            }
            if (found == false) {
                continue;
            }

            String addon;
            if (d.konstanta == null) {
                addon = getCellFormatedData(sheet,d.yMin,d.xMin);
            } else {
                addon = d.konstanta;
            }

            if ("".equals(result)) {
                result = "\"" + addon + "\"";
            } else {
                result = result + ",\"" + addon + "\"";
            }
        }
        return result;
    }

    String getCellFormatedData(HSSFSheet wbSheet,int rowNum, int colIndex) {
        if (wbSheet == null || wbSheet.getRow(rowNum) == null ||
                wbSheet.getRow(rowNum).getCell(colIndex) == null) {
            return "";
        }
        HSSFCell cell = wbSheet.getRow(rowNum).getCell(colIndex);
        if (cell.getCellType() == Cell.CELL_TYPE_STRING) return cell.getStringCellValue().trim();
        if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) return "" + cell.getNumericCellValue(); //cell.getCellStyle().getDataFormatString();
        if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) return cell.getCellFormula() + " / " + cell.getCachedFormulaResultType();
        return "";
    }


}
