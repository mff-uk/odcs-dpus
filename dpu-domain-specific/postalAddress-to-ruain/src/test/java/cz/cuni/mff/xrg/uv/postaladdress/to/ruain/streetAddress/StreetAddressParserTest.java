package cz.cuni.mff.xrg.uv.postaladdress.to.ruain.streetAddress;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Škoda Petr
 */
public class StreetAddressParserTest {

    private final StreetAddressParser parser = new StreetAddressParser();

    @Test
    public void name() throws WrongAddressFormatException {
        StreetAddress addr = parser.parse("Boršov");

        Assert.assertNull(addr.getTownName());
        Assert.assertNull(addr.getHouseNumber());
        Assert.assertNull(addr.getLandRegistryNumber());
        Assert.assertEquals("Boršov", addr.getStreetName());
    }

    @Test
    public void landNumber() throws WrongAddressFormatException {
        StreetAddress addr = parser.parse("466");

        Assert.assertNull(addr.getTownName());
        Assert.assertNull(addr.getHouseNumber());
        Assert.assertEquals("466", addr.getLandRegistryNumber());
        Assert.assertNull(addr.getStreetName());
    }

    @Test
    public void nameWithSpace() throws WrongAddressFormatException {
        StreetAddress addr = parser.parse("Boží Dar");

        Assert.assertNull(addr.getTownName());
        Assert.assertNull(addr.getHouseNumber());
        Assert.assertNull(addr.getLandRegistryNumber());
        Assert.assertEquals("Boží Dar", addr.getStreetName());
    }

    @Test
    public void nameWithSpaceAndNumber() throws WrongAddressFormatException {
        StreetAddress addr = parser.parse("1. května");

        Assert.assertNull(addr.getTownName());
        Assert.assertNull(addr.getHouseNumber());
        Assert.assertNull(addr.getLandRegistryNumber());
        Assert.assertEquals("1. května", addr.getStreetName());
    }

    @Test
    public void nameWithNumber() throws WrongAddressFormatException {
        StreetAddress addr = parser.parse("1.května");

        Assert.assertNull(addr.getTownName());
        Assert.assertNull(addr.getHouseNumber());
        Assert.assertNull(addr.getLandRegistryNumber());
        Assert.assertEquals("1. května", addr.getStreetName());
    }

    @Test
    public void nameWithRegionNumer() throws WrongAddressFormatException {
        StreetAddress addr = parser.parse("Tejny 621");

        Assert.assertNull(addr.getTownName());
        Assert.assertNull(addr.getHouseNumber());
        Assert.assertEquals("621", addr.getLandRegistryNumber());
        Assert.assertEquals("Tejny", addr.getStreetName());
    }

    @Test
    public void nameWithSpaceAndRegionNumer() throws WrongAddressFormatException {
        StreetAddress addr = parser.parse("T. Bati 1541");

        Assert.assertNull(addr.getTownName());
        Assert.assertNull(addr.getHouseNumber());
        Assert.assertEquals("1541", addr.getLandRegistryNumber());
        Assert.assertEquals("T. Bati", addr.getStreetName());
    }

    @Test
    public void nameWithSpaceRegionAndHouseNumber() throws WrongAddressFormatException {
        StreetAddress addr = parser.parse("28. října 2771/11");

        Assert.assertNull(addr.getTownName());
        Assert.assertEquals("11", addr.getHouseNumber());
        Assert.assertEquals("2771", addr.getLandRegistryNumber());
        Assert.assertEquals("28. října", addr.getStreetName());
    }

    @Test
    public void nameRegionAndHouseNumberWithSpaces() throws WrongAddressFormatException {
        StreetAddress addr = parser.parse("Kubelíkova 604 / 73");

        Assert.assertNull(addr.getTownName());
        Assert.assertEquals("73", addr.getHouseNumber());
        Assert.assertEquals("604", addr.getLandRegistryNumber());
        Assert.assertEquals("Kubelíkova", addr.getStreetName());
    }
    
    @Test
    public void nameWithRegionAndHouseNonNumber() throws WrongAddressFormatException {
        StreetAddress addr = parser.parse("Svornosti 3199/19a");

        Assert.assertNull(addr.getTownName());
        Assert.assertEquals("19a", addr.getHouseNumber());
        Assert.assertEquals("3199", addr.getLandRegistryNumber());
        Assert.assertEquals("Svornosti", addr.getStreetName());
    }

    @Test(expected = WrongAddressFormatException.class)
    public void nameWithPOBox() throws WrongAddressFormatException {
        StreetAddress addr = parser.parse("Vernéřov, POBox 10 188");
    }

    @Test
    public void nameWithHousneNumber() throws WrongAddressFormatException {
        StreetAddress addr = parser.parse("J. M. Marků /92");

        Assert.assertNull(addr.getTownName());
        Assert.assertEquals("92", addr.getHouseNumber());
        Assert.assertNull(addr.getLandRegistryNumber());
        Assert.assertEquals("J. M. Marků", addr.getStreetName());
    }

    @Test
    public void nameWithDotAndNumberInside() throws WrongAddressFormatException {
        StreetAddress addr = parser.parse("Náměstí 5. května 19");

        Assert.assertNull(addr.getTownName());
        Assert.assertNull(addr.getHouseNumber());
        Assert.assertEquals("19", addr.getLandRegistryNumber());
        Assert.assertEquals("Náměstí 5. května", addr.getStreetName());
    }

    @Test(expected = WrongAddressFormatException.class)
    public void nameWithSpecialCharacter() throws WrongAddressFormatException {
        StreetAddress addr = parser.parse("Erna-Berger-Strasse 1");
    }

    @Test
    public void nameWithLandAndMultipleHouseNumber() throws WrongAddressFormatException {
        StreetAddress addr = parser.parse("nám. Přemysla Otakara II. 1/1,2");

        Assert.assertNull(addr.getTownName());
        Assert.assertEquals("1,2", addr.getHouseNumber());
        Assert.assertEquals("1", addr.getLandRegistryNumber());
        Assert.assertEquals("nám. Přemysla Otakara II.", addr.getStreetName());
    }

    @Test
    public void nameWithDuplicitLandAndHouseNumber() throws WrongAddressFormatException {
        StreetAddress addr = parser.parse("Jiřího z Poděbrad 2725/21 2725/21");

        Assert.assertNull(addr.getTownName());
        Assert.assertEquals("21", addr.getHouseNumber());
        Assert.assertEquals("2725", addr.getLandRegistryNumber());
        Assert.assertEquals("Jiřího z Poděbrad", addr.getStreetName());
    }

    @Test
    public void landAndHouseNumberSpaceSeparation() throws WrongAddressFormatException {
        StreetAddress addr = parser.parse("435 52");

        Assert.assertNull(addr.getTownName());
        Assert.assertEquals("52", addr.getHouseNumber());
        Assert.assertEquals("435", addr.getLandRegistryNumber());
        Assert.assertNull(addr.getStreetName());
    }
    
    @Test
    public void bracesRemoveTest() throws WrongAddressFormatException {
        StreetAddress addr = parser.parse("Olomoucká 90 (Olympia)");
        
        Assert.assertNull(addr.getTownName());
        Assert.assertNull(addr.getHouseNumber());
        Assert.assertEquals("90", addr.getLandRegistryNumber());
        Assert.assertEquals("Olomoucká", addr.getStreetName());        
    }

    @Test
    public void townTest0() throws WrongAddressFormatException {
        StreetAddress addr = parser.parse("Kladruby-Vojenice 80");
        
        Assert.assertEquals("Kladruby", addr.getTownName());
        Assert.assertNull(addr.getHouseNumber());
        Assert.assertEquals("80", addr.getLandRegistryNumber());
        Assert.assertEquals("Vojenice", addr.getStreetName());
    }

    @Test
    public void townTestWithSpaces() throws WrongAddressFormatException {
        StreetAddress addr = parser.parse("Petrovice - zámek 26");
        
        Assert.assertEquals("Petrovice", addr.getTownName());
        Assert.assertNull(addr.getHouseNumber());
        Assert.assertEquals("26", addr.getLandRegistryNumber());
        Assert.assertEquals("zámek", addr.getStreetName());        
    }
    
    @Test
    public void genericTest0() throws WrongAddressFormatException {
        StreetAddress addr = parser.parse("Vrchlického (pošt.přihr.35) 1009/6");
        
        Assert.assertNull(addr.getTownName());
        Assert.assertEquals("6", addr.getHouseNumber());
        Assert.assertEquals("1009", addr.getLandRegistryNumber());
        Assert.assertEquals("Vrchlického", addr.getStreetName());
    }
    
}

// TODO: Update for
// Jana Žižky83
// Generála Richarda Tesaříka125
// Českolipská 864.                 // remove .
// Josefská 427/14 14/427
// U Tří mostů 844/2 2
// Komenského 384 (hotel Centrum)"  // remove "
// Brněnec, Moravská Chrastová 10