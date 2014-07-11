package cz.cuni.mff.xrg.uv.postaladdress.to.ruain.streetAddress;

/**
 * If there are two number at the end separated with space then, replace the
 * space with slash.
 *
 * @author Škoda Petr
 */
public class NumbersSeparationFormatter implements Formatter {

    @Override
    public String format(String streetAddress) {
        if (streetAddress.contains("/")) {
            // already contains slash, so return original string
            return streetAddress;
        }
        // check which elements are numbers
        String[] split = streetAddress.split(" ");
        boolean[] isNumber = new boolean[split.length];
        for (int index = 0; index < split.length; ++index) {
            try {
                Integer.parseInt(split[index]);
                isNumber[index] = true;
            } catch (NumberFormatException ex) {
                isNumber[index] = false;
            }

        }
        // reassemble the string and put slasth instead of space between numbers
        final StringBuilder result = new StringBuilder(streetAddress.length());
        result.append(split[0]);
        for (int index = 1; index < split.length; ++index) {
            if (isNumber[index - 1] && isNumber[index]) {
                // insert slash
                result.append("/");
                // as we connected to number chanche the type of current number
                isNumber[index] = false;
            } else {
                // insert space
                result.append(" ");
            }
            result.append(split[index]);
        }
        return result.toString();
    }
}
