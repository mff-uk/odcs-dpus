package cz.cuni.mff.xrg.uv.boost.dpu.vaadin;

import java.util.LinkedList;
import java.util.List;

import cz.cuni.mff.xrg.uv.boost.dpu.advanced.AbstractDpu;
import cz.cuni.mff.xrg.uv.boost.dpu.context.Context;
import cz.cuni.mff.xrg.uv.boost.ontology.OntologyDefinition;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.config.ConfigDialogContext;

/**
 *
 * @author Škoda Petr
 */
public class DialogContext<CONFIG, ONTOLOGY extends OntologyDefinition> extends Context<CONFIG, ONTOLOGY> {

    /**
     * Owner dialog.
     */
    protected final AbstractDialog dialog;

    /**
     * Core context.
     */
    protected final ConfigDialogContext dialogContext;

    /**
     * List of add-on dialogs.
     */
    protected final List<AbstractAddonDialog> addonDialogs = new LinkedList<>();

    public DialogContext(AbstractDialog dialog, ConfigDialogContext dialogContext,
            Class<AbstractDpu<CONFIG, ONTOLOGY>> dpuClass, AbstractDpu<CONFIG, ONTOLOGY> dpuInstance)
            throws DPUException {
        super(dpuClass, dpuInstance);
        this.dialog = dialog;
        this.dialogContext = dialogContext;
        // And call init as we hawe all we need. Configuration is going to be set later.
        init(null, dialogContext.getLocale(), dpuClass.getClassLoader());
    }

    public AbstractDialog getDialog() {
        return dialog;
    }

    public ConfigDialogContext getDialogContext() {
        return dialogContext;
    }

    public List<AbstractAddonDialog> getAddonDialogs() {
        return addonDialogs;
    }

}
