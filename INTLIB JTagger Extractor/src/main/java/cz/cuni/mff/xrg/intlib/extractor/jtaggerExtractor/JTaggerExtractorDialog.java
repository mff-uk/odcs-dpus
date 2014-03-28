package cz.cuni.mff.xrg.intlib.extractor.jtaggerExtractor;

import com.vaadin.data.Validator;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import cz.cuni.xrg.intlib.commons.configuration.ConfigException;
import cz.cuni.xrg.intlib.commons.module.dialog.BaseConfigDialog;

/**
 * DPU's configuration dialog. User can use this dialog to configure DPU
 * configuration.
 *
 */
public class JTaggerExtractorDialog extends BaseConfigDialog<JTaggerExtractorConfig> {

    private GridLayout mainLayout;
    private TextField dateTo; //Path
    private TextField dateFrom; //Path

    public JTaggerExtractorDialog() {
        super(JTaggerExtractorConfig.class);
        buildMainLayout();
		Panel panel = new Panel();
		panel.setSizeFull();
		panel.setContent(mainLayout);
		setCompositionRoot(panel);
    }
    
   
    private GridLayout buildMainLayout() {
        // common part: create layout
        mainLayout = new GridLayout(1, 2);
        mainLayout.setImmediate(false);
        mainLayout.setWidth("100%");
        mainLayout.setHeight("100%");
        mainLayout.setMargin(false);
        //mainLayout.setSpacing(true);

        // top-level component properties
        setWidth("100%");
        setHeight("100%");

        // textFieldPath
        dateFrom = new TextField();
        dateFrom.setNullRepresentation("");
        dateFrom.setCaption("Date From (DD/MM/YYYY):");
        dateFrom.setImmediate(false);
        dateFrom.setWidth("100%");
        dateFrom.setHeight("-1px");
        dateFrom.setInputPrompt("09/07/2013");
        dateFrom.addValidator(new Validator() {
            @Override
            public void validate(Object value) throws Validator.InvalidValueException {
                if (value.getClass() == String.class && !((String) value).isEmpty()) {
                    return;
                }
                throw new Validator.InvalidValueException("Date from must be filled!");
            }
        });
        mainLayout.addComponent(dateFrom);
        
        dateTo = new TextField();
        dateTo.setNullRepresentation("");
        dateTo.setCaption("Date To (DD/MM/YYYY):");
        dateTo.setImmediate(false);
        dateTo.setWidth("100%");
        dateTo.setHeight("-1px");
        dateTo.setInputPrompt("10/07/2013");
//        dateTo.addValidator(new Validator() {
//            @Override
//            public void validate(Object value) throws Validator.InvalidValueException {
//                if (value.getClass() == String.class && !((String) value).isEmpty()) {
//                    return;
//                }
//                throw new Validator.InvalidValueException("Path must be filled!");
//            }
//        });
        mainLayout.addComponent(dateTo) ;


        return mainLayout;
    }

    @Override
    public void setConfiguration(JTaggerExtractorConfig conf) throws ConfigException {
        try {
            dateTo.setValue(conf.getDateTO());
            dateFrom.setValue(conf.getDateFrom());
        } catch (Exception ex) {
            // throw setting exception
            throw new ConfigException();
        }

    }

    @Override
    public JTaggerExtractorConfig getConfiguration() throws ConfigException {
        if (!dateTo.isValid()) {
            throw new ConfigException();
        } else {
            JTaggerExtractorConfig conf = new JTaggerExtractorConfig(dateTo.getValue().trim(), dateFrom.getValue().trim());
            return conf;
        }
    }

//    @Override
//    public void setConfig(byte[] conf) throws ConfigException {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    @Override
//    public byte[] getConfig() throws ConfigException {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
}
