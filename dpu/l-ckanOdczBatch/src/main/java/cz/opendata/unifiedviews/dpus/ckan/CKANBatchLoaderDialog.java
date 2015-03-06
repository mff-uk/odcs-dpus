package cz.opendata.unifiedviews.dpus.ckan;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

/**
 * DPU's configuration dialog. User can use this dialog to configure DPU configuration.
 *
 */
public class CKANBatchLoaderDialog extends AbstractDialog<CKANBatchLoaderConfig> {

	private static final long serialVersionUID = -1989608763609859477L;
	
	private VerticalLayout mainLayout;
    private TextField tfRestApiUrl;
	private Label lblRestApiUrl;
    private PasswordField tfApiKey;
    
    public CKANBatchLoaderDialog() {
        super(CKANBatchLoader.class);
    }  
    
    @Override
	protected void buildDialogLayout() {
        // common part: create layout
        mainLayout = new VerticalLayout();
        mainLayout.setImmediate(true);
        mainLayout.setWidth("100%");
        mainLayout.setHeight(null);
        mainLayout.setMargin(false);

        // top-level component properties
        setWidth("100%");
        setHeight("100%");
        
        tfRestApiUrl = new TextField();
        tfRestApiUrl.setWidth("100%");
        tfRestApiUrl.setCaption("CKAN Rest API URL");
        tfRestApiUrl.setInputPrompt("http://datahub.io/api/rest/dataset");
        mainLayout.addComponent(tfRestApiUrl);

        tfApiKey = new PasswordField();
        tfApiKey.setWidth("100%");
        tfApiKey.setCaption("CKAN API Key");
        tfApiKey.setDescription("CKAN API Key");
        tfApiKey.setInputPrompt("00000000-0000-0000-0000-000000000000");
        mainLayout.addComponent(tfApiKey);
        
        lblRestApiUrl = new Label();
        lblRestApiUrl.setContentMode(ContentMode.HTML);
        mainLayout.addComponent(lblRestApiUrl);
        
        Panel panel = new Panel();
        panel.setSizeFull();
        panel.setContent(mainLayout);
        setCompositionRoot(panel);
    }    
     
    @Override
    public void setConfiguration(CKANBatchLoaderConfig conf) throws DPUConfigException {
    	tfApiKey.setValue(conf.getApiKey());
    	tfRestApiUrl.setValue(conf.getApiUri());
    }

	@Override
    public CKANBatchLoaderConfig getConfiguration() throws DPUConfigException {
    	CKANBatchLoaderConfig conf = new CKANBatchLoaderConfig();
        conf.setApiKey(tfApiKey.getValue());
        conf.setApiUri(tfRestApiUrl.getValue());
        return conf;
    }

}
