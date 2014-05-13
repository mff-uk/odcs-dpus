package cz.cuni.mff.xrg.odcs.extractor.fdu;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import cz.cuni.mff.xrg.odcs.commons.configuration.ConfigException;
import cz.cuni.mff.xrg.odcs.commons.module.dialog.BaseConfigDialog;

/**
 *
 * @author Škoda Petr
 */
public class Dialog extends BaseConfigDialog <Configuration> {
	
	private VerticalLayout mainLayout;
		
	private TextField txtSource;
	
	private CheckBox chbIncludeSubDirs;
	
	private TextField txtTarget;
	
	private CheckBox chbAsLink;
	
	public Dialog() {
		super(Configuration.class);
		buildMainLayout();
	}
	
	private void buildMainLayout() {
		mainLayout = new VerticalLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("100%");
		mainLayout.setHeight("-1px");
		
		// top-level component properties
		setWidth("100%");
		setHeight("100%");
	
		txtSource = new TextField();
		txtSource.setWidth("100%");
		txtSource.setHeight("100%");
		txtSource.setCaption("Source directory:");
		txtSource.setRequired(true);
		txtSource.setNullRepresentation("");
		mainLayout.addComponent(txtSource);

		chbIncludeSubDirs = new CheckBox();
		chbIncludeSubDirs.setCaption("include subdirectories");
		mainLayout.addComponent(chbIncludeSubDirs);
		
		txtTarget = new TextField();
		txtTarget.setWidth("100%");
		txtTarget.setHeight("100%");
		txtTarget.setCaption("Target:");
		txtTarget.setRequired(true);
		txtTarget.setNullRepresentation("");
		mainLayout.addComponent(txtTarget);
	
		chbAsLink = new CheckBox();
		chbAsLink.setCaption("as link (uncheck to copy file)");
		mainLayout.addComponent(chbAsLink);
		
		setCompositionRoot(mainLayout);
	}

	@Override
	protected void setConfiguration(Configuration conf) throws ConfigException {
		txtSource.setValue(conf.getSource());
		chbIncludeSubDirs.setValue(conf.isIncludeSubDirs());
		txtTarget.setValue(conf.getTarget());
		chbAsLink.setValue(conf.isAsLink());
	}

	@Override
	protected Configuration getConfiguration() throws ConfigException {
		if (!txtSource.isValid()) {
			throw new ConfigException("Target must be filled.");
		}
		if (!txtTarget.isValid()) {
			throw new ConfigException("Target must be filled.");
		}
		
		Configuration conf = new Configuration();
		conf.setSource(txtSource.getValue());
		conf.setIncludeSubDirs(chbIncludeSubDirs.getValue());
		conf.setTarget(txtTarget.getValue());
		conf.setAsLink(chbAsLink.getValue());
		
		return conf;		
	}

	@Override
	public String getDescription() {
		StringBuilder desc = new StringBuilder();
		
		desc.append("Extract data as files from ");
		desc.append(txtSource.getValue());
		desc.append(" into ");
		desc.append(txtTarget.getValue());
		
		return desc.toString();
	}
	
	
}
