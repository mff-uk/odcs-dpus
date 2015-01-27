package cz.cuni.mff.xrg.uv.transformer.tabular.dbf;

/**
 *
 * @author Škoda Petr
 */
public class ParserDbfConfig {

    final String encoding;

    final Integer rowLimit;

    final boolean checkStaticRowCounter;

    public ParserDbfConfig(String encoding, Integer rowLimit,
            boolean checkStaticRowCounter) {
        this.encoding = encoding;
        this.rowLimit = rowLimit;
        this.checkStaticRowCounter = checkStaticRowCounter;
    }

}
