package cz.cuni.mff.xrg.uv.addressmapper.streetAddress;

import cz.cuni.mff.xrg.uv.addressmapper.streetAddress.NumbersSeparationFormatter;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Škoda Petr
 */
public class NumbersSeparationFormatterTest {

    private final NumbersSeparationFormatter formatter = new NumbersSeparationFormatter();

    @Test
    public void twoNumbersToConnect() {
        Assert.assertEquals("435/52", formatter.format("435 52"));
    }

    @Test
    public void noChange() {
        final String text = "Náměstí 5. května 19";
        Assert.assertEquals(text, formatter.format(text));
    }

}
