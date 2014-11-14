package cz.cuni.mff.xrg.uv.boost.dpu.addon.impl;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonException;
import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AddonVaadinDialogBase;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AdvancedVaadinDialogBase;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.ConfigurableAddon;
import eu.unifiedviews.dpu.config.DPUConfigException;

/**
 * Hold and show help in configuration dialog.
 *
 * DPU can provide it's own help in HTML format. This add-on should be used only in configuration dialogs.
 *
 * @author Škoda Petr
 */
public class HelpHolder implements ConfigurableAddon<HelpHolder.Configuration_V1> {

    public static final String USED_CONFIG_NAME = "addon/helpHolder";

    public static final String ADDON_NAME = "Help";

    public static class Configuration_V1 {

    }

    public class VaadinDialog extends AddonVaadinDialogBase<Configuration_V1> {

        public VaadinDialog() {
            super(Configuration_V1.class);
        }

        @Override
        public void buildLayout() {
            final Label label = new Label(dpuHelpHtml, ContentMode.HTML);

            final Panel panel = new Panel();
            panel.setSizeFull();
            panel.setContent(label);

            final VerticalLayout layout = new VerticalLayout();
            layout.setSizeFull();
            layout.setMargin(true);
            layout.addComponent(panel);

            setCompositionRoot(layout);
        }

        @Override
        protected String getConfigClassName() {
            return USED_CONFIG_NAME;
        }

        @Override
        protected void setConfiguration(Configuration_V1 conf) throws DPUConfigException {
            // No operation here.
        }

        @Override
        protected Configuration_V1 getConfiguration() throws DPUConfigException {
            return new Configuration_V1();
        }

    }

    /**
     * Help from user, in HTML format.
     */
    private final String dpuHelpHtml;

    /**
     *
     * @param dpuHelpHtml DPU help in HTMl format.
     */
    public HelpHolder(String dpuHelpHtml) {
        this.dpuHelpHtml = dpuHelpHtml;
    }

    @Override
    public void init(AdvancedVaadinDialogBase.Context context) {
        // No operation here.

        // TODO Examine other addons and get their help too.
    }

    @Override
    public Class<Configuration_V1> getConfigClass() {
        return Configuration_V1.class;
    }

    @Override
    public String getDialogCaption() {
        return ADDON_NAME;
    }

    @Override
    public AddonVaadinDialogBase<Configuration_V1> getDialog() {
        return new VaadinDialog();
    }

    @Override
    public void init(DpuAdvancedBase.Context context) throws AddonException {
        // No operation here.
    }

}
