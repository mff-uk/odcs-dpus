package cz.cuni.mff.xrg.intlib.extractor.legislation.decisions;

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
 * DPU's configuration dialog. NO Dialog needed for this DPU
 *
 */
public class UriGeneratorDialog extends BaseConfigDialog<UriGeneratorConfig> {

   private static final Logger log = LoggerFactory.getLogger(UriGeneratorDialog.class);

    
      public UriGeneratorDialog() {
        super(UriGeneratorConfig.class);
        //buildMainLayout();
        //setCompositionRoot(mainLayout);
    }

   
    @Override
    public void setConfiguration(UriGeneratorConfig conf) throws ConfigException {
        
           
         
     
         
  

    }

    @Override
    public UriGeneratorConfig getConfiguration() throws ConfigException {
        return new UriGeneratorConfig();
                
       

        
        
        
        
    }
}
