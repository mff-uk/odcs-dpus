package cz.cuni.mff.xrg.uv.addressmapper.streetAddress;

/**
 * Apply format modification on given streetAddress.
 * 
 * @author Škoda Petr
 */
interface Formatter {
    
    public String format(String streetAddress);
    
}
