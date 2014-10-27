package cz.cuni.mff.xrg.uv.extractor.sukl;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Škoda Petr
 */
public class SubstanceNameParse {

    @Test
    public void simpleName() {
        String name = "RIVASTIGMIN-HYDROGEN-TARTAR&Aacute;T (RIVASTIGMINI TARTRAS)";

        int index = Utils.getLastOpeningBraceIndex(name);
        String cs = name.substring(0, index).trim();
        String la = name.substring(index + 1, name.length() - 1);

        System.out.println("> " + cs);
        System.out.println("> " + la);

        Assert.assertEquals("RIVASTIGMIN-HYDROGEN-TARTAR&Aacute;T", cs);
        Assert.assertEquals("RIVASTIGMINI TARTRAS", la);
    }

    @Test
    public void withBraces() {
        String name = "ETHANOL 96 % (V/V) (ETHANOLUM 96% (V/V))";

        int index = Utils.getLastOpeningBraceIndex(name);
        String cs = name.substring(0, index).trim();
        String la = name.substring(index + 1, name.length() - 1);

        System.out.println("> " + cs);
        System.out.println("> " + la);

        Assert.assertEquals("ETHANOL 96 % (V/V)", cs);
        Assert.assertEquals("ETHANOLUM 96% (V/V)", la);
    }

    @Test
    public void problemCase_1() {
        String name = "INFLUENZA A/TEXAS (H3N2) (SPLIT VIRION) (INFLUAENZAE VIRI A/TEXAS (H3N2) FRAGMENTUM)";

        int index = Utils.getLastOpeningBraceIndex(name);
        String cs = name.substring(0, index).trim();
        String la = name.substring(index + 1, name.length() - 1);

        System.out.println("> " + cs);
        System.out.println("> " + la);

        Assert.assertEquals("INFLUENZA A/TEXAS (H3N2) (SPLIT VIRION)", cs);
        Assert.assertEquals("INFLUAENZAE VIRI A/TEXAS (H3N2) FRAGMENTUM", la);
    }

}
