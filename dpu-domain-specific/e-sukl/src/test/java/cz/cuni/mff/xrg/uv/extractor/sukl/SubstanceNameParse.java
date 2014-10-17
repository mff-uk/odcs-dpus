package cz.cuni.mff.xrg.uv.extractor.sukl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Škoda Petr
 */
public class SubstanceNameParse {

    private static String regExp = "(?<csen>[^\\(\\)]*(\\([^\\(\\)]*\\))*[^\\(\\)]*)\\((?<la>[^\\(\\)]*(\\([^\\(\\)]*\\))*[^\\(\\)]*)\\)$";

    @Test
    public void simpleName() {
        String name = "RIVASTIGMIN-HYDROGEN-TARTAR&Aacute;T (RIVASTIGMINI TARTRAS)";

        Pattern pattern = Pattern.compile(regExp);
        Matcher matcher = pattern.matcher(name);

        System.out.println(matcher.matches());

        String cs = matcher.group("csen").trim();
        String la = matcher.group("la").trim();

        System.out.println("> " + cs);
        System.out.println("> " + la);

        Assert.assertEquals("RIVASTIGMIN-HYDROGEN-TARTAR&Aacute;T", cs);
        Assert.assertEquals("RIVASTIGMINI TARTRAS", la);

    }

    @Test
    public void withBraces() {
        String name = "ETHANOL 96 % (V/V) (ETHANOLUM 96% (V/V))";

        Pattern pattern = Pattern.compile(regExp);
        Matcher matcher = pattern.matcher(name);

        System.out.println(matcher.matches());

        String cs = matcher.group("csen").trim();
        String la = matcher.group("la").trim();

        System.out.println("> " + cs);
        System.out.println("> " + la);

        Assert.assertEquals("ETHANOL 96 % (V/V)", cs);
        Assert.assertEquals("ETHANOLUM 96% (V/V)", la);

    }

}
