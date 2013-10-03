package cz.cuni.mff.xrg.intlib.extractor.legislation.decisions;


import cz.cuni.mff.xrg.odcs.commons.configuration.ConfigException;
import cz.cuni.mff.xrg.odcs.commons.module.dialog.BaseConfigDialog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;






/**
 * DPU's configuration dialog. NO Dialog needed for this DPU
 *
 */
public class JTaggerAnnotatorDialog extends BaseConfigDialog<JTaggerAnnotatorConfig> {

   private static final Logger log = LoggerFactory.getLogger(JTaggerAnnotatorDialog.class);

    
      public JTaggerAnnotatorDialog() {
        super(JTaggerAnnotatorConfig.class);
        //buildMainLayout();
        //setCompositionRoot(mainLayout);
    }

   
    @Override
    public void setConfiguration(JTaggerAnnotatorConfig conf) throws ConfigException {
        
           
         
     
         
  

    }

    @Override
    public JTaggerAnnotatorConfig getConfiguration() throws ConfigException {
        return new JTaggerAnnotatorConfig();
                
       

        
        
        
        
    }
}
