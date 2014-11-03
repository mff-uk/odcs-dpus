package cz.cuni.mff.xrg.uv.transformer;

import eu.unifiedviews.dpu.DPUException;

/**
 * Used to report wrong arguments for action.
 *
 * @author Škoda Petr
 */
public class WrongActionArgs extends DPUException {

    public WrongActionArgs(String message) {
        super(message);
    }

}
