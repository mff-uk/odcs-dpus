package cz.cuni.mff.xrg.uv.transformer.tabular.parser;

/**
 *
 * @author Škoda Petr
 */
public class ParserDbfConfig {

    final String encoding;

    final Integer rowLimit;

    final boolean hasHeader;

    final boolean checkStaticRowCounter;

    public ParserDbfConfig(String encoding, Integer rowLimit, boolean hasHeader,
            boolean checkStaticRowCounter) {
        this.encoding = encoding;
        this.rowLimit = rowLimit;
        this.hasHeader = hasHeader;
        this.checkStaticRowCounter = checkStaticRowCounter;
    }

}
