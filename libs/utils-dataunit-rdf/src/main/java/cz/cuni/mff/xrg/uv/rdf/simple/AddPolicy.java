package cz.cuni.mff.xrg.uv.rdf.simple;

/**
 * Add policy for {@link SimpleRdfWrite}.
 *
 * @author Škoda Petr
 */
public enum AddPolicy {

	/**
	 * Triples are added into repository immediately. For each addition new
	 * connection is created. This approach provide immediate reaction on
	 * possible problem (repository is offline) but in case of saving greater
	 * number of triples can be computationally demanding.
	 */
	IMMEDIATE,
	/**
	 * Triples are stored in in memory buffer and added into repository once
	 * upon a time. This option should be used if larger number of statements
	 * should be added into repository. There is a disadvantage in current
	 * implementation where a single failure will cause failure there is no
	 * guaranteed state of used repository. The buffer is cleared only if all
	 * the triples are successfully added into repository.
	 */
	BUFFERED
}
