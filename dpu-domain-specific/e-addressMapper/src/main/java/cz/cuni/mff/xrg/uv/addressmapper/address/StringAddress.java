package cz.cuni.mff.xrg.uv.addressmapper.address;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.uv.addressmapper.ruian.RuianEntity;
import cz.cuni.mff.xrg.uv.addressmapper.utils.Utils;

/**
 * Represents an unstructured address in string form.
 *
 * @author Škoda Petr
 */
public class StringAddress implements Iterable<StringAddress.Token> {

    private static final Logger LOG = LoggerFactory.getLogger(StringAddress.class);

    /**
     * We can store multiple meanings to a single address.
     */
    public static enum Meaning {
        VUCS,
        OKRES,
        OBEC,
        CASTOBCE,
        ULICE,
        CISLODOMOVNI,
        CISLOORIENTACNI,
        CISLOORIENTACNI_PISMENO
    }

    public class Token {
        
        private final String value;
        
        private final Integer start;
        
        private final Integer end;

        private Token(Integer start, Integer end) {
            this.start = start;
            this.end = end;
            // Create reprensentation.
            StringBuilder strBuilder = new StringBuilder();
            strBuilder.append(parts[start]);
            for (int i = start + 1; i < end; ++i) {
                strBuilder.append(" ");
                strBuilder.append(parts[i]);
            }
            this.value = strBuilder.toString();
        }

        @Override
        public String toString() {
            return value;
        }

        /**
         *
         * @param entity
         * @param meaning
         * @param realValue The "real" and right value of current token.
         * @throws NumberFormatException
         */
        public void addMeaning(RuianEntity entity, Meaning meaning, String realValue) throws NumberFormatException {
            for (int i = start; i < end; ++i) {
                if (!entity.getMeanings().containsKey(i)) {
                    entity.getMeanings().put(i, new LinkedList<Meaning>());
                }
                entity.getMeanings().get(i).add(meaning);
            }
//            LOG.debug("Adding meaning '{}' to '{}' with target value '{}'", meaning, value, realValue);
            // And set to entity as well.
            switch (meaning) {
                case CASTOBCE:
                    entity.setCastObce(realValue);
                    break;
                case OBEC:
                    entity.setObec(realValue);
                    break;
                case OKRES:
                    entity.setOkres(realValue);
                    break;
                case ULICE:
                    entity.setUlice(realValue);
                    break;
                case VUCS:
                    entity.setVusc(realValue);
                    break;
                case CISLODOMOVNI:
                    entity.setCisloDomovni(Integer.parseInt(realValue));
                    break;
                case CISLOORIENTACNI:
                    entity.setCisloOrientancni(Integer.parseInt(realValue));
                    break;
                case CISLOORIENTACNI_PISMENO:
                    entity.setCisloOrientancniPismeno(realValue);
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
        }

    }

    private final String[] parts;

    private final List<Token> tokens = new LinkedList<>();

    public StringAddress(String address) {
        address = Utils.normalizeSpaces(address);
        this.parts = address.split(" ");
        // Create tokens.
        for (int len = 1; len <= parts.length; ++len) {
            for (int start = 0; start + len <= parts.length; ++start) {
                tokens.add(new Token(start, start + len));
            }
        }
        // We revert token order to go from biggest to smallest.
        Collections.reverse(tokens);
    }
    
    public String[] getParts() {
        return parts;
    }

    /**
     * Iterate token from longest to shortest.
     *
     * @return
     */
    @Override
    public Iterator<Token> iterator() {
        return tokens.iterator();
    }

}
