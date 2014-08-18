package cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple;

import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 * Add write functionality to {@link SimpleRdfRead} by wrapping
 * {@link WritableRDFDataUnit}.
 *
 * <pre>
 * {@code
 * SimpleRdfWrite rdf = SimpleRdfFactory(rdfDataUnit, dpuContext);
 * rdf.setPolicy(AddPolicy.BUFFERED);
 * rdf.setOutputGraph("myOutputGraph");
 * // add triples
 * rdf.add(subject, predicate, object);
 * // write from buffer into database
 * rdf.flushBuffer();
 * }
 * </pre>
 *
 * @author Škoda Petr
 */
public interface SimpleRdfWrite extends SimpleRdfRead {

	/**
	 * Add triple into repository. Based on current {@link AddPolicy} can add 
	 * triple in immediate or lazy way.
	 * 
	 * In the second case the {@link #flushBuffer()} method must be called in 
	 * order to add triples into used repository, until that the triples are
	 * stored in inner buffer - the triples are not visible in any read function.
	 * 
	 * @param s
	 * @param p
	 * @param o
	 * @throws OperationFailedException 
	 */
	public void add(Resource s, URI p, Value o) throws OperationFailedException;

	/**
	 * Set policy that determines how the
	 * {@link #add(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value)}
	 * behaves.
	 *
	 * @param policy Add policy.
	 */
	void setPolicy(AddPolicy policy);

	/**
	 * Immediately store buffered triples into repository. The inner buffer is
	 * cleared only if all the triples are added successfully. If throws
	 * exception then the state of repository is undefined.
	 *
	 * If
	 * {@link #add(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value)}
	 * is called with {@link AddPolicy#BUFFERED} this method must be called in
	 * order to save added statements into repository.
	 *
	 * @throws OperationFailedException
	 */
	void flushBuffer() throws OperationFailedException;

    /**
     * Set given graph as current output. If existing graph is set then the
     * existing graph is used.
     *
     * @param symbolicName
     * @throws OperationFailedException
     */
    void setOutputGraph(String symbolicName) throws OperationFailedException;

}
