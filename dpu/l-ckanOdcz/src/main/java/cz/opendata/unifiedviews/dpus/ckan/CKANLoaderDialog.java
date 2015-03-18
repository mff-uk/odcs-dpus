package cz.opendata.unifiedviews.dpus.ckan;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;
import eu.unifiedviews.helpers.dpu.vaadin.tabs.ConfigCopyPaste;

/**
 * DPU's configuration dialog. User can use this dialog to configure DPU configuration.
 *
 */
public class CKANLoaderDialog extends AbstractDialog<CKANLoaderConfig> {

	private static final long serialVersionUID = -1989608763609859477L;
	
	private VerticalLayout mainLayout;
    private TextField tfRestApiUrl;
    private TextField tfFileName;
    private CheckBox chkLoad;
    private TextField tfDatasetID;
    private PasswordField tfApiKey;
    private TextField tfOwnerOrg;
    
    public CKANLoaderDialog() {
        super(CKANLoader.class);
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
        
        chkLoad = new CheckBox();
        chkLoad.setWidth("100%");
        chkLoad.setImmediate(true);
        chkLoad.setCaption("Load to CKAN (if disabled, only generates the JSON file)");
        chkLoad.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 7348068985822592639L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				tfRestApiUrl.setEnabled(chkLoad.getValue());
				tfApiKey.setEnabled(chkLoad.getValue());
		}});
        mainLayout.addComponent(chkLoad);

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
        
        tfDatasetID = new TextField();
        tfDatasetID.setImmediate(true);
        tfDatasetID.setWidth("100%");
        tfDatasetID.setTextChangeEventMode(TextChangeEventMode.EAGER);
        tfDatasetID.setCaption("Dataset ID");
        tfDatasetID.setDescription("CKAN Dataset Name used in CKAN Dataset URL");
        tfDatasetID.setInputPrompt("cz-test");
        mainLayout.addComponent(tfDatasetID);
        
        tfOwnerOrg = new TextField();
        tfOwnerOrg.setWidth("100%");
        tfOwnerOrg.setCaption("Owner CKAN organization ID");
        tfOwnerOrg.setInputPrompt("00000000-0000-0000-0000-000000000000");
        mainLayout.addComponent(tfOwnerOrg);

        tfFileName = new TextField();
        tfFileName.setWidth("100%");
        tfFileName.setCaption("Output filename");
        tfFileName.setInputPrompt("ckan.json");
        mainLayout.addComponent(tfFileName);

        Panel panel = new Panel();
        panel.setSizeFull();
        panel.setContent(mainLayout);
        setCompositionRoot(panel);

        // Tabs.
        this.addTab(ConfigCopyPaste.create(ctx), "Copy&Paste");
    }    
     
    @Override
    public void setConfiguration(CKANLoaderConfig conf) throws DPUConfigException {
    	tfApiKey.setValue(conf.getApiKey());
    	tfDatasetID.setValue(conf.getDatasetID());
    	tfOwnerOrg.setValue(conf.getOrgID());
    	tfRestApiUrl.setValue(conf.getApiUri());
    	chkLoad.setValue(conf.isLoadToCKAN());
    }

	@Override
    public CKANLoaderConfig getConfiguration() throws DPUConfigException {
    	CKANLoaderConfig conf = new CKANLoaderConfig();
        conf.setApiKey(tfApiKey.getValue());
        conf.setApiUri(tfRestApiUrl.getValue());
        conf.setDatasetID(tfDatasetID.getValue());
        conf.setOrgID(tfOwnerOrg.getValue());
        conf.setLoadToCKAN(chkLoad.getValue());
        return conf;
    }

}
