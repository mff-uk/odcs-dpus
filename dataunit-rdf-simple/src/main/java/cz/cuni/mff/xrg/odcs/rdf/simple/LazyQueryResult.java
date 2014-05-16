package cz.cuni.mff.xrg.odcs.rdf.simple;

import cz.cuni.mff.xrg.odcs.rdf.RDFDataUnit;
import org.openrdf.query.BindingSet;

/**
 * Lazy query fetch a certain amount of triples once a time.
 * 
 * @author Škoda Petr
 */
public class LazyQueryResult {
	
	private final RDFDataUnit dataUnit;
	
	private final String orderSelectQuery;
	
	LazyQueryResult(RDFDataUnit dataUnit, String orderSelectQuery) {
		this.dataUnit = dataUnit;
		this.orderSelectQuery = orderSelectQuery;
	}
	
	public boolean hasNext() throws OperationFailedException {
		return false;
	}
	
	public BindingSet next() throws OperationFailedException {
		return null;
	}
	
}
