package cz.cuni.mff.xrg.uv.addressmapper.streetAddress;

/**
 *
 * @author Škoda Petr
 */
public class StreetAddress {

    private final String townName;
    
    private final String streetName;
    
    private final String landRegistryNumber;
    
    /**
     * Can contains multiple house numbers separated by ','. Also can contains
     * non number character as a part of number (mostly at the end).
     */
    private final String houseNumber;

    public StreetAddress() {
        this.townName = null;
        this.streetName = null;
        this.landRegistryNumber = null;
        this.houseNumber = null;
    }
    
    public StreetAddress(String name, String landRegistryNumber, String houseNumber) {
        this.townName = null;
        this.streetName = name;
        this.landRegistryNumber = landRegistryNumber;
        this.houseNumber = houseNumber;
    }

    public StreetAddress(String townName, String streetName, String landRegistryNumber, String houseNumber) {
        this.townName = townName;
        this.streetName = streetName;
        this.landRegistryNumber = landRegistryNumber;
        this.houseNumber = houseNumber;
    }
    
    public String getTownName() {
        return townName;
    }
    
    
    public String getStreetName() {
        return streetName;
    }

    public String getLandRegistryNumber() {
        return landRegistryNumber;
    }

    public String getHouseNumber() {
        return houseNumber;
    }
    
}
