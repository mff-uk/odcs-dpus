package eu.unifiedviews.cssz.cz.komix.xls2csv;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by hugo on 29.5.14.
 */
public class DimenzeBox {

    private static final Logger log = LoggerFactory.getLogger(DimenzeBox.class);

    
    public List<Dimenze> box;
    public List<Link> linkBox;

    public DimenzeBox() {
        box = new ArrayList<Dimenze>();
    }

    /**
     * Metoda pridava do seznamu dimenzi bez kontroly duplicity CISLA - pro seznamy vsech vyskytu a jejich adresaci
     *
     * @param nova
     */
    public void appendDimenze(Dimenze nova) {
        box.add(nova);
    }

    /**
     * Metoda pridava do seznamu dimenzi NEDUPLICTNE dle CISLA - pro hlavni katalog s pojmenovanim
     * @param nova
     */
    public void saveDimenze(Dimenze nova) {
        for (Dimenze d: this.box) {
            if (d.poradi == nova.poradi) {
                log.debug("Appending SUB " + nova + " to " + d);
                d.sub.appendDimenze(nova);
                return; // Uz existuje
            }
        }
        this.box.add(nova); // FIXME !!!
    }

    /**
     * Metoda dohledava a vraci serazeny seznam dimenzi ve sloupci a radku dle zadanych souradnic prochazenim
     * celeho hlavniho Boxu dimenzi.
     * @param col
     * @param row
     * @return
     */
    public Dimenze getDimenzeAt(int col, int row) {
//        log.debug("getDimenzeAt(" + col + "," + row +")");
        for (Dimenze d: box) {
            if ((d.xMin <= col && d.xMax >= col) && (d.yMin <= row && d.yMax >= row)) {
                return d;
            }
            List<Dimenze> subs = d.sub.box;
            for (Dimenze sub: subs) {
                if ((sub.xMin <= col && sub.xMax >= col) && (sub.yMin <= row && sub.yMax >= row)) {
                    return(sub);
                }
            }
        }
        return null; // TODO : Chyba odkazu?
    }

    /**
     * Metoda dohledava a vraci serazeny seznam dimenzi ve sloupci a radku dle zadanych souradnic prochazenim
     * celeho hlavniho Boxu dimenzi.
     * @param col
     * @param row
     * @return
     */
    public List<Dimenze> getDimenze(int col, int row, List<Link> linkBox) {
        List<Dimenze> nalez = new ArrayList<Dimenze>();
        for (Dimenze d: box) {
            // Pokud je dimenze ve sloupci NEBO je dimenze v radku
            if ((d.xMin <= col && d.xMax >= col) || (d.yMin <= row && d.yMax >= row)) {
                boolean found = false;
                for (Dimenze k: nalez) {
                    if (k.poradi == d.poradi) {
                        found = true;
                        nalez.set(nalez.indexOf(k),d);
                        log.debug("Replacing D-main : " + k + " -> " + d);
                        break;
                    }
                }
                // ale jeste neni ve vysledku hledani, tak ji tam pridej
                if (! found) {
                    log.debug("Setting D-main : " + d + "("+d.sub.box+")");
                    nalez.add(d);
                }
            }
            List<Dimenze> subs = d.sub.box;
            for (Dimenze sub: subs) {
                if ((sub.xMin <= col && sub.xMax >= col) || (sub.yMin <= row && sub.yMax >= row)) {
                    boolean found = false;
                    for (Dimenze k: nalez) {
                        if (k.poradi == d.poradi) {
                            found = true;
                            log.debug("Replacing D-sub : " + k + " -> " + sub);
                            nalez.set(nalez.indexOf(k),sub);
                            break;
                        }
                    }
                    if (! found) {
                        nalez.add(sub);
                    }
                }
            }
        }
        for (Link l: linkBox) {
            if ((l.xMin <= col && l.xMax >= col) || (l.yMin <= row && l.yMax >= row)) {
                Dimenze dim = getDimenzeAt(l.linkX, l.linkY);
                log.debug("Linked dimenze z " + l + " na " + dim);
//                log.debug("Nalezen odkaz na DIM: " + (dim==null?"null":dim.poradi));
                if (dim != null) {
                    boolean found = false;
                    for (Dimenze k: nalez) {
                        if (k.poradi == dim.poradi) {
                            found = true;
                            log.debug("Replacing D-link : " + k + " -> " + dim);
                            nalez.set(nalez.indexOf(k),dim);
                            break;
                        }
                    }
                    if (! found) {
                        nalez.add(dim);
                    }
                }
            }
        }
        Collections.sort(nalez, new Comparator<Dimenze>() {
            @Override
            public int compare(Dimenze o1, Dimenze o2) {
                return o1.poradi - o2.poradi;
            }
        });
        return nalez;
    }

    public List<Dimenze> getSortedDimenze() {
        Collections.sort(box, new Comparator<Dimenze>() {
            @Override
            public int compare(Dimenze o1, Dimenze o2) {
                return o1.poradi - o2.poradi;
            }
        });
        return box;
    }

}
