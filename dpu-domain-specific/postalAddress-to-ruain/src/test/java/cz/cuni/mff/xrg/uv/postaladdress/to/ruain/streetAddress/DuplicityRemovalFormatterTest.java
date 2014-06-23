package cz.cuni.mff.xrg.uv.postaladdress.to.ruain.streetAddress;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Škoda Petr
 */
public class DuplicityRemovalFormatterTest {

    private final DuplicityRemovalFormatter formatter = new DuplicityRemovalFormatter();

    @Test
    public void removeDuplicit() {
        Assert.assertEquals("Jiřího z Poděbrad 2725/21",
                formatter.format("Jiřího z Poděbrad 2725/21 2725/21"));
    }

}
