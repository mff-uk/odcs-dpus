package cz.cuni.mff.xrg.uv.transformer.tabular;

import cz.cuni.mff.xrg.uv.rdf.simple.OperationFailedException;
import cz.cuni.mff.xrg.uv.rdf.simple.test.SimpleRdfDummyBase;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 *
 * @author Škoda Petr
 */
public class WriteOutSimpleRdf extends SimpleRdfDummyBase {

    @Override
    public void add(Resource s, URI p, Value o) throws OperationFailedException {
        System.out.print("> " + s.stringValue() + " "
                + p.stringValue() + " ");
        System.out.print(o.stringValue());
        if (o instanceof Literal) {
            Literal l = (Literal) o;
            if (l.getDatatype() != null) {
                System.out.print("^" + l.getDatatype().stringValue());
            }
            if (l.getLanguage() != null) {
                System.out.print("^" + l.getLanguage());
            }
        }
        System.out.println("");
    }
}
