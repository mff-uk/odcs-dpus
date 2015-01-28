package cz.cuni.mff.xrg.uv.boost.extensions;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;

import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigHistory;
import cz.cuni.mff.xrg.uv.boost.dpu.context.Context;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AbstractAddonVaadinDialog;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AbstractVaadinDialog;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.Configurable;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.dpu.config.DPUConfigException;

/**
 * Add-on for copy & past work with configuration.
 *
 * @author Škoda Petr
 */
public class ConfigCopyPaste implements Configurable<ConfigCopyPaste.Configuration_V1> {

    public static final String USED_CONFIG_NAME = "addon/configurationCopyPaste";

    public static final String ADDON_NAME = "Configuration copy&paste";

    public static class Configuration_V1 {
        
    }

    public class VaadinDialog extends AbstractAddonVaadinDialog<Configuration_V1> {

        private TextArea txtConfiguration;

        public VaadinDialog() {
            super(ConfigHistory.noHistory(Configuration_V1.class));
        }

        @Override
        public void buildLayout() {
            setSizeFull();

            final HorizontalLayout buttonLayout = new HorizontalLayout();
            buttonLayout.setSpacing(true);
            
            final Button btnImport = new Button("Import configuration");
            buttonLayout.addComponent(btnImport);
            buttonLayout.setComponentAlignment(btnImport, Alignment.MIDDLE_LEFT);

            final Button btnExport = new Button("Export configuration");
            buttonLayout.addComponent(btnExport);
            buttonLayout.setComponentAlignment(btnExport, Alignment.MIDDLE_RIGHT);

            final VerticalLayout mainLayout = new VerticalLayout();
            final Label label = new Label("Can be used to import export DPU's configuration as string. Use with caution! </br>"
                    + "In case of import configuration is not saved, only loaded into dialog.</br>"
                    + "Exports corrent configuration from dialog, current configuration msut be valid.", ContentMode.HTML);
            mainLayout.addComponent(label);

            mainLayout.setSizeFull();
            mainLayout.setMargin(true);
            mainLayout.setSpacing(true);
            mainLayout.addComponent(buttonLayout);

            txtConfiguration = new TextArea("Configuration");
            txtConfiguration.setSizeFull();

            mainLayout.addComponent(txtConfiguration);
            mainLayout.setExpandRatio(txtConfiguration, 1.0f);

            setCompositionRoot(mainLayout);

            // Bind functionality to the buttons.
            btnImport.addClickListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    try {
                        dialogContext.getDialog().setConfig(txtConfiguration.getValue());
                        Notification.show("Configuration has been imported.", Notification.Type.HUMANIZED_MESSAGE);
                    } catch (DPUConfigException ex) {
                        Notification.show("Import failed", ex.fillInStackTrace().toString(), Notification.Type.ERROR_MESSAGE);
                    }
                }
            });

            btnExport.addClickListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    try {
                        final String configuration = dialogContext.getDialog().getConfig();
                        txtConfiguration.setValue(configuration);
                        Notification.show("Configuration has been exported.", Notification.Type.HUMANIZED_MESSAGE);
                    } catch (DPUConfigException ex) {
                        txtConfiguration.setValue("");
                        Notification.show("Export failed", ex.fillInStackTrace().toString(), Notification.Type.ERROR_MESSAGE);
                    }
                }
            });


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
     * Dialog context.
     */
    private AbstractVaadinDialog.DialogContext dialogContext = null;

    @Override
    public void preInit(String param) throws DPUException {
        // No-op.
    }

    @Override
    public void afterInit(Context context) {
        if (context instanceof AbstractVaadinDialog.DialogContext) {
            this.dialogContext = (AbstractVaadinDialog.DialogContext)context;
        }
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
    public AbstractAddonVaadinDialog<Configuration_V1> getDialog() {
        return new VaadinDialog();
    }

}
