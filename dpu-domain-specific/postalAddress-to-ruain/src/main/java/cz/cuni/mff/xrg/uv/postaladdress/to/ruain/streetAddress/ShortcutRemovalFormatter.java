package cz.cuni.mff.xrg.uv.postaladdress.to.ruain.streetAddress;

/**
 *
 * @author Škoda Petr
 */
public class ShortcutRemovalFormatter implements Formatter {

    @Override
    public String format(String streetAddress) {
        return streetAddress.replaceAll("(č\\.?p\\.\\s?)*(č\\.\\s?)*", "");
    }
    
}
