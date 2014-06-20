package cz.cuni.mff.xrg.uv.postaladdress.to.ruain.streetAddress;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Škoda Petr
 */
public class BracesRemovalFormatterTest {

    private final BracesRemovalFormatter formatter = new BracesRemovalFormatter();

    @Test
    public void removeBraces() {
        Assert.assertEquals("Olomoucká 90",
                formatter.format("Olomoucká 90 (Olympia) "));
    }

    @Test
    public void multipleRemoveBraces() {
        Assert.assertEquals("Skandinávská 9",
                formatter.format("Skandinávská 9 (15a) (15a)"));
    }

    @Test
    public void genericTest0() {
        Assert.assertEquals("Vrchlického 1009/6",
                formatter.format("Vrchlického (pošt.přihr.35) 1009/6"));
    }

}
