package cz.cuni.mff.xrg.uv.boost.dpu.vaadin;

import cz.cuni.mff.xrg.uv.boost.dpu.context.UserContext;
import cz.cuni.mff.xrg.uv.boost.ontology.OntologyDefinition;

/**
 * User version of dialog context.
 *
 * @author Škoda Petr
 */
public class UserDialogContext<ONTOLOGY extends OntologyDefinition> extends UserContext<ONTOLOGY> {

    protected final DialogContext<?, ONTOLOGY> dialogMasterContext;

    public UserDialogContext(DialogContext<?, ONTOLOGY> dialogContext) {
        super(dialogContext);
        this.dialogMasterContext = dialogContext;
    }

    /**
     *
     * @return True if dialog is used as a template.
     */
    public boolean isTemplate() {
        return this.dialogMasterContext.getDialogContext().isTemplate();
    }

    public DialogContext<?, ONTOLOGY> getDialogMasterContext() {
        return dialogMasterContext;
    }

}
