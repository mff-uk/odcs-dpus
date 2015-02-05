package cz.cuni.mff.xrg.uv.boost.dpu.vaadin;

import cz.cuni.mff.xrg.uv.boost.dpu.context.UserContext;

/**
 * User version of dialog context.
 *
 * @author Škoda Petr
 */
public class UserDialogContext extends UserContext {

    protected final DialogContext<?> dialogMasterContext;

    public UserDialogContext(DialogContext<?> dialogContext) {
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

    public DialogContext<?> getDialogMasterContext() {
        return dialogMasterContext;
    }

}
