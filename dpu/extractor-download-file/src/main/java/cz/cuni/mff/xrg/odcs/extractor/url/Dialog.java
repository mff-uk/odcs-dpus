package cz.cuni.mff.xrg.odcs.extractor.url;

import com.vaadin.data.Validator;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import cz.cuni.mff.xrg.odcs.commons.configuration.ConfigException;
import cz.cuni.mff.xrg.odcs.commons.module.dialog.BaseConfigDialog;
import java.net.MalformedURLException;
import java.net.URL;

public class Dialog extends BaseConfigDialog<Configuration> {

	private VerticalLayout mainLayout;
	
	private TextField txtURL;
	
	private TextField txtTarget;
	
	private TextField txtRetryCount;
	
	private TextField txtRetryDelay;
	
	public Dialog() {
		super(Configuration.class);
		buildMainLayout();
	}

	private void buildMainLayout() {
		mainLayout = new VerticalLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("100%");
		mainLayout.setHeight("-1px");
		mainLayout.setSpacing(true);
		mainLayout.setMargin(true);
		
		// top-level component properties
		setWidth("100%");
		setHeight("100%");
		
		txtURL = new TextField();
		txtURL.setWidth("100%");
		txtURL.setHeight("-1px");
		txtURL.setCaption("URL:");
		txtURL.setRequired(false);
		txtURL.setNullRepresentation("");
		mainLayout.addComponent(txtURL);
		
		txtTarget = new TextField();
		txtTarget.setWidth("100%");
		txtTarget.setHeight("-1px");
		txtTarget.setCaption("Target - file name and location in output:");
		txtTarget.setRequired(true);
		mainLayout.addComponent(txtTarget);
		
		txtRetryCount = new TextField();
		txtRetryCount.setWidth("100%");
		txtRetryCount.setHeight("-1px");
		txtRetryCount.setCaption("Max attemts at one download:");
		txtRetryCount.setRequired(true);
		mainLayout.addComponent(txtRetryCount);
		
		txtRetryDelay = new TextField();
		txtRetryDelay.setWidth("100%");
		txtRetryDelay.setHeight("-1px");
		txtRetryDelay.setCaption("Interval between downloads:");
		txtRetryDelay.setRequired(true);
		mainLayout.addComponent(txtRetryDelay);
		
		Validator validator = new Validator() {
			@Override
			public void validate(Object value) throws Validator.InvalidValueException {
				try {
					Integer.parseInt(value.toString());
				} catch (NumberFormatException e) {
					throw new Validator.InvalidValueException("Wrong format number.");
				}
			}
		};
		
		txtRetryCount.addValidator(validator);
		txtRetryDelay.addValidator(validator);
		
		setCompositionRoot(mainLayout);
	}
	
	@Override
	public void setConfiguration(Configuration conf) throws ConfigException {
		if (conf.getURL() != null) {
			txtURL.setValue(conf.getURL().toString());
		} else {
			txtURL.setValue(null);
		}
		txtTarget.setValue(conf.getTarget());		
		txtRetryCount.setValue(((Integer)conf.getRetryCount()).toString());
		txtRetryDelay.setValue(((Integer)conf.getRetryDelay()).toString());
	}

	@Override
	public Configuration getConfiguration() throws ConfigException {
		Configuration conf = new Configuration();
		
		final boolean isValid = txtURL.isValid() && txtTarget.isValid() && 
				txtRetryCount.isValid() && txtRetryDelay.isValid();
		
		if (!isValid) {
			throw new ConfigException("Some fields contains invalid value.");
		}
		
		final String stringURL = txtURL.getValue();
		if (stringURL == null) {
			if (getContext().isTemplate()) {
				// ok
			} else {
				throw new ConfigException("URL is not specified.");
			}
		} else {
			try {
				conf.setURL(new URL(txtURL.getValue()));
			} catch (MalformedURLException ex) {
				throw new ConfigException("Wrong URL format", ex);
			}
		}
		
		conf.setTarget(getTarget());
		try {
			conf.setRetryCount(Integer.parseInt(txtRetryCount.getValue()));
			conf.setRetryDelay(Integer.parseInt(txtRetryDelay.getValue()));
		} catch (NumberFormatException ex) {
			throw new ConfigException("Bad number format.");
		}
		return conf;
	}

	@Override
	public String getDescription() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("Download file from ");
		sb.append(txtURL.getValue());
		sb.append(" as ");
		sb.append(getTarget());
		
		return sb.toString();
	}
	
	private String getTarget() {
		return txtTarget.getValue().replaceAll("\\\\", "/");
	}	

}
