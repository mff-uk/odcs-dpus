package cz.cuni.mff.xrg.uv.postaladdress.to.ruain.query;

import java.util.Objects;

/**
 *
 * @author Škoda Petr
 */
public class PredicatObject {

    public String predicate;

    public String object;

    public PredicatObject(String predicate, String object) {
        this.predicate = predicate;
        this.object = object;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PredicatObject) {
            PredicatObject other = (PredicatObject) obj;
            return predicate.equals(other.predicate) && object.equals(
                    other.object);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.predicate);
        hash = 29 * hash + Objects.hashCode(this.object);
        return hash;
    }
}
