package eu.unifiedviews.cssz.cz.komix.xls2csv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by hugo on 29.5.14.
 */
public class Dimenze {

    private static final Logger log = LoggerFactory.getLogger(Dimenze.class);

    
    public int poradi;
    public int xMin, xMax, yMin, yMax;
    String popis;
    String konstanta;
    public DimenzeBox sub;

    public Dimenze (int poradi, String popis, String konstanta, int xMin, int xMax, int yMin, int yMax) {
        this.poradi = poradi;
        this.popis = popis;
        this.konstanta = konstanta;
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
        sub = new DimenzeBox();
    }

    public Dimenze (int poradi, String popis, String konstanta, int x, int y) {
        this(poradi, popis, konstanta, x, x, y, y);
    }

    @Override
    public String toString() {
        return String.format("D%02d",poradi) + " (" + popis + " # " + konstanta + " @ [" + xMin + "," + yMin + "])";
    }
}
