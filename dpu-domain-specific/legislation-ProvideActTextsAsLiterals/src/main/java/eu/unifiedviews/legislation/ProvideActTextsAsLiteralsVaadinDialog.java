package eu.unifiedviews.legislation;

import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AdvancedVaadinDialogBase;
import eu.unifiedviews.dpu.config.DPUConfigException;

public class ProvideActTextsAsLiteralsVaadinDialog extends AdvancedVaadinDialogBase<ProvideActTextsAsLiteralsConfig_V1> {

    private TextField tfPredicateURL; //Path
    
    public ProvideActTextsAsLiteralsVaadinDialog() {
        super(ProvideActTextsAsLiteralsConfig_V1.class, AddonInitializer.noAddons());

        buildLayout();
    }

    @Override
    public void setConfiguration(ProvideActTextsAsLiteralsConfig_V1 c) throws DPUConfigException {
        
         if(!c.getPredicateURL().isEmpty()) { 
                tfPredicateURL.setValue(c.getPredicateURL());
       }

    }

    @Override
    public ProvideActTextsAsLiteralsConfig_V1 getConfiguration() throws DPUConfigException {
        final ProvideActTextsAsLiteralsConfig_V1 c = new ProvideActTextsAsLiteralsConfig_V1();
        

          c.setPredicateURL(tfPredicateURL.getValue().trim());
        
        return c;

    }
    
    private void buildLayout() {
		final VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setWidth("100%");
		mainLayout.setHeight("-1px");

          tfPredicateURL = new TextField();
        tfPredicateURL.setNullRepresentation("");
        tfPredicateURL.setCaption("Predicate URL:");
        tfPredicateURL.setImmediate(false);
        tfPredicateURL.setWidth("100%");
        tfPredicateURL.setHeight("-1px");
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
        mainLayout.addComponent(tfPredicateURL) ;

        setCompositionRoot(mainLayout);
    }
}
