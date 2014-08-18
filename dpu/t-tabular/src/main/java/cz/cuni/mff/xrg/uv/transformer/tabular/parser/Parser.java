package cz.cuni.mff.xrg.uv.transformer.tabular.parser;

import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.OperationFailedException;
import java.io.File;

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
     * @throws ParseFailed
     */
    void parse(File inFile) throws OperationFailedException, ParseFailed;

}
