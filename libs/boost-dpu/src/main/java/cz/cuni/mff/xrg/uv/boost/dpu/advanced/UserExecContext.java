package cz.cuni.mff.xrg.uv.boost.dpu.advanced;

import cz.cuni.mff.xrg.uv.boost.dpu.context.UserContext;
import cz.cuni.mff.xrg.uv.boost.ontology.OntologyDefinition;

/**
 * User version of {@link ExecContext}.
 *
 * @author Škoda Petr
 */
public class UserExecContext<ONTOLOGY extends OntologyDefinition> extends UserContext<ONTOLOGY> {

    protected final ExecContext<?, ONTOLOGY> execMasterContext;

    public UserExecContext(ExecContext<?, ONTOLOGY> execContext) {
        super(execContext);
        this.execMasterContext = execContext;
    }

    /**
     *
     * @return True if DPU is cancelled.
     */
    public boolean canceled() {
        return execMasterContext.getDpuContext().canceled();
    }

    public ExecContext<?, ONTOLOGY> getExecMasterContext() {
        return execMasterContext;
    }

}
