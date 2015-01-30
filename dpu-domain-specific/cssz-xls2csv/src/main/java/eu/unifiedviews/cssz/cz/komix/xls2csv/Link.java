package eu.unifiedviews.cssz.cz.komix.xls2csv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by hugo on 2.6.14.
 */
public class Link {

    private static final Logger log = LoggerFactory.getLogger(Demo01.class);

    
    public static final String sloupce = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public int xMin, xMax, yMin, yMax;
    public int linkX, linkY;

    public Link (String odkaz, int xMin, int xMax, int yMin, int yMax) {
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
        this.linkX = sloupce.indexOf(odkaz.replaceAll("\\%\\%B_\\(([A-Z]),([0-9]+)\\)\\%\\%.*", "$1"));
        this.linkY = Integer.parseInt(odkaz.replaceAll("\\%\\%B_\\(([A-Z]),([0-9]+)\\)\\%\\%.*", "$2"))-1;
        log.debug("Odkaz z X: " + xMin + " - " + xMax + ", Y: " + yMin + " - " + yMax);
        log.debug("Odkaz na " + odkaz + " prelozen na X: " + linkX + ", Y: " + linkY);
    }

    public Link (String odkaz, int x, int y) {
        this(odkaz, x, x, y, y);
    }

    @Override
    public String toString() {
        return String.format("L[%02d,%02d]",linkX, linkY);
    }
}
