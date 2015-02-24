package eu.unifiedviews.intlib.senddatatorextractor;

import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AdvancedVaadinDialogBase;
import eu.unifiedviews.dpu.config.DPUConfigException;

public class IntlibSendDataToRextractorVaadinDialog extends AdvancedVaadinDialogBase<IntlibSendDataToRextractorConfig_V1> {

     private TextField tfTargetRextractorServerURL; //Path
     
    public IntlibSendDataToRextractorVaadinDialog() {
        super(IntlibSendDataToRextractorConfig_V1.class, AddonInitializer.noAddons());

        buildLayout();
    }

    @Override
    public void setConfiguration(IntlibSendDataToRextractorConfig_V1 c) throws DPUConfigException {

         if(!c.getTargetRextractorServer().isEmpty()) { 
                tfTargetRextractorServerURL.setValue(c.getTargetRextractorServer());
       }
        
    }

    @Override
    public IntlibSendDataToRextractorConfig_V1 getConfiguration() throws DPUConfigException {
        final IntlibSendDataToRextractorConfig_V1 c = new IntlibSendDataToRextractorConfig_V1();

           c.setTargetRextractorServer(tfTargetRextractorServerURL.getValue().trim());
        
        return c;
    }

    private void buildLayout() {
		final VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setWidth("100%");
		mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);

        tfTargetRextractorServerURL = new TextField();
        tfTargetRextractorServerURL.setNullRepresentation("");
        tfTargetRextractorServerURL.setCaption("Predicate URL:");
        tfTargetRextractorServerURL.setImmediate(false);
        tfTargetRextractorServerURL.setWidth("100%");
        tfTargetRextractorServerURL.setHeight("-1px");
//        tfPredicateURL.setInputPrompt("2014-11-13");
//        dateTo.addValidator(new Validator() {
//            @Override
//            public void validate(Object value) throws Validator.InvalidValueException {
//                if (value.getClass() == String.class && !((String) value).isEmpty()) {
//                    return;
//                }
//                throw new Validator.InvalidValueException("Path must be filled!");
//            }
//        });
        mainLayout.addComponent(tfTargetRextractorServerURL) ;

        setCompositionRoot(mainLayout);
    }
}
