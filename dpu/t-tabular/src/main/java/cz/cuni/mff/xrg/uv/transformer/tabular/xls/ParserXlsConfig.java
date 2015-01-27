package cz.cuni.mff.xrg.uv.transformer.tabular.xls;

import cz.cuni.mff.xrg.uv.transformer.tabular.column.NamedCell_V1;
import java.util.Collections;
import java.util.List;

/**
 * Configuration for {@link ParserXls}.
 *
 * @author Škoda Petr
 */
public class ParserXlsConfig {

    /**
     * If null then every sheet is used.
     */
    final String sheetName;

    final int numberOfStartLinesToIgnore;

    final boolean hasHeader;

    final List<NamedCell_V1> namedCells;

    final Integer rowLimit;

    final boolean checkStaticRowCounter;

    public ParserXlsConfig(String sheetName, int numberOfStartLinesToIgnore,
            boolean hasHeader, List<NamedCell_V1> namedCells,
            Integer rowLimit, boolean checkStaticRowCounter) {
        this.sheetName = sheetName;
        this.numberOfStartLinesToIgnore = numberOfStartLinesToIgnore;
        this.hasHeader = hasHeader;
        if (namedCells == null) {
            this.namedCells = Collections.EMPTY_LIST;
        } else {
            this.namedCells = namedCells;
        }
        this.rowLimit = rowLimit;
        this.checkStaticRowCounter = checkStaticRowCounter;
    }
    
}
