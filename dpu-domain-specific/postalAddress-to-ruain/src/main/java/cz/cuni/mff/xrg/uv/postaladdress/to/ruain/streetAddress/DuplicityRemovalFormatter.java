package cz.cuni.mff.xrg.uv.postaladdress.to.ruain.streetAddress;

/**
 * Split string and then remove duplicity entries. After that reassemble the
 * string.
 *
 * @author Škoda Petr
 */
public class DuplicityRemovalFormatter implements Formatter {

    @Override
    public String format(String streetAddress) {
        final StringBuilder result = new StringBuilder(streetAddress.length());
        final String[] split = streetAddress.split(" ");
        result.append(split[0]);
        for (int i = 1; i < split.length; ++i) {
            boolean collision = false;
            for (int j = i + 1; j < split.length; ++j) {
                if (split[i].compareTo(split[j]) == 0) {
                    collision = true;
                    break;
                }
            }
            if (!collision) {
                result.append(" ");
                result.append(split[i]);
            }
        }

        return result.toString();
    }

}
