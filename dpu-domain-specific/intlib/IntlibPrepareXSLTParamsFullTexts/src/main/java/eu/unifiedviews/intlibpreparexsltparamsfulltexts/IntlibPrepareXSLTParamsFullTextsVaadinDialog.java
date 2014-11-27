package eu.unifiedviews.intlibpreparexsltparamsfulltexts;

import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AdvancedVaadinDialogBase;
import eu.unifiedviews.dpu.config.DPUConfigException;

public class IntlibPrepareXSLTParamsFullTextsVaadinDialog extends AdvancedVaadinDialogBase<IntlibPrepareXSLTParamsFullTextsConfig_V1> {

    private TextField tfRelationID;
    
    public IntlibPrepareXSLTParamsFullTextsVaadinDialog() {
        super(IntlibPrepareXSLTParamsFullTextsConfig_V1.class, AddonInitializer.noAddons());

        buildLayout();
    }

    @Override
    public void setConfiguration(IntlibPrepareXSLTParamsFullTextsConfig_V1 c) throws DPUConfigException {
        tfRelationID.setValue(c.getRelationID());
    }

    @Override
    public IntlibPrepareXSLTParamsFullTextsConfig_V1 getConfiguration() throws DPUConfigException {
        final IntlibPrepareXSLTParamsFullTextsConfig_V1 c = new IntlibPrepareXSLTParamsFullTextsConfig_V1();

//         if (!tfRelationID.isValid()) {
//            throw new DPUConfigException("XLS Template prefix must be specified");
//        }

        c.setRelationID(tfRelationID.getValue().trim());
        
        
        return c;
    }

    private void buildLayout() {
		final VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setWidth("100%");
		mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);

         mainLayout.addComponent(new Label("relation id (if any): "));
        
        tfRelationID = new TextField("");
        tfRelationID.setWidth("100%");
        tfRelationID.setRequired(true);
        tfRelationID.setNullRepresentation("");
        tfRelationID.setNullSettingAllowed(true);
        mainLayout.addComponent(tfRelationID);

        setCompositionRoot(mainLayout);
    }
    
     @Override
    public String getDescription() {
        StringBuilder desc = new StringBuilder();

        desc.append("RelationID: ");
        desc.append(tfRelationID.getValue());

        return desc.toString();
    }
}
