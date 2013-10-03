package cz.cuni.mff.xrg.intlib.rdfUtils;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import cz.cuni.mff.xrg.odcs.commons.configuration.ConfigException;
import cz.cuni.mff.xrg.odcs.commons.module.dialog.BaseConfigDialog;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;






/**
 * DPU's configuration dialog. User can use this dialog to configure DPU
 * configuration.
 * 
 * 
 *
 */
public class RDFDistillerDialog extends BaseConfigDialog<RDFaDistillerConfig> {

   private static final Logger log = LoggerFactory.getLogger(RDFDistillerDialog.class);
   

 

    public RDFDistillerDialog() {
        super(RDFaDistillerConfig.class);
//        buildMainLayout();
//        setCompositionRoot(mainLayout);
    }

   @Override
    public void setConfiguration(RDFaDistillerConfig conf) throws ConfigException {
        
           
         
     
         
  

    }

    @Override
    public RDFaDistillerConfig getConfiguration() throws ConfigException {
        return new RDFaDistillerConfig();
                
       

        
        
        
        
    }
    
}