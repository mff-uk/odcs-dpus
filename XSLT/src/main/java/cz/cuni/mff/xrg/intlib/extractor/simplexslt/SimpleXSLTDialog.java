package cz.cuni.mff.xrg.intlib.extractor.simplexslt;

import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import cz.cuni.xrg.intlib.commons.configuration.ConfigException;
import cz.cuni.xrg.intlib.commons.web.AbstractConfigDialog;

/**
 * DPU's configuration dialog. User can use this dialog to configure DPU
 * configuration.
 *
 */
public class SimpleXSLTDialog extends AbstractConfigDialog<SimpleXSLTConfig> {

    private GridLayout mainLayout;
    private TextField xsltPath; //Path
    private TextField xmlPath; //Path

    public SimpleXSLTDialog() {
        buildMainLayout();
        setCompositionRoot(mainLayout);
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
        xmlPath = new TextField();
        xmlPath.setNullRepresentation("");
        xmlPath.setCaption("Path to input XML File:");
        xmlPath.setImmediate(false);
        xmlPath.setWidth("100%");
        xmlPath.setHeight("-1px");
        xmlPath.setInputPrompt("/file/test.xml");
        xmlPath.addValidator(new Validator() {
            @Override
            public void validate(Object value) throws Validator.InvalidValueException {
                if (value.getClass() == String.class && !((String) value).isEmpty()) {
                    return;
                }
                throw new Validator.InvalidValueException("Path must be filled!");
            }
        });
        mainLayout.addComponent(xmlPath);
        
        xsltPath = new TextField();
        xsltPath.setNullRepresentation("");
        xsltPath.setCaption("Path to XSLT:");
        xsltPath.setImmediate(false);
        xsltPath.setWidth("100%");
        xsltPath.setHeight("-1px");
        xsltPath.setInputPrompt("/file/test.xslt");
        xsltPath.addValidator(new Validator() {
            @Override
            public void validate(Object value) throws Validator.InvalidValueException {
                if (value.getClass() == String.class && !((String) value).isEmpty()) {
                    return;
                }
                throw new Validator.InvalidValueException("Path must be filled!");
            }
        });
        mainLayout.addComponent(xsltPath) ;


        return mainLayout;
    }

    @Override
    public void setConfiguration(SimpleXSLTConfig conf) throws ConfigException {
        try {
            xsltPath.setValue(conf.getXslTemplate());
            xmlPath.setValue(conf.getXmlFile());
        } catch (Exception ex) {
            // throw setting exception
            throw new ConfigException();
        }

    }

    @Override
    public SimpleXSLTConfig getConfiguration() throws ConfigException {
        if (!xsltPath.isValid()) {
            throw new ConfigException();
        } else {
            SimpleXSLTConfig conf = new SimpleXSLTConfig(xsltPath.getValue().trim(), xmlPath.getValue().trim());
            return conf;
        }
    }
}
