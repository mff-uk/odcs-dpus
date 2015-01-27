package cz.cuni.mff.xrg.uv.transformer.tabular.parser;

import java.io.File;

import cz.cuni.mff.xrg.uv.boost.serialization.rdf.SimpleRdfException;

/**
 *
 * @author Škoda Petr
 */
public interface Parser {

    /**
     * Parse given file.
     * 
     * @param inFile
     * @throws OperationFailedException
     * @throws SimpleRdfException
     */
    void parse(File inFile) throws ParseFailed, SimpleRdfException;

}
