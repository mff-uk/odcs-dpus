package cz.cuni.mff.xrg.odcs.transformer.converter.rdf.csv;

import com.vaadin.ui.TextArea;
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
	
	private TextField txtTarget;
	
	private TextArea txtQuery;
	
	public Dialog() {
		super(Configuration.class);
		buildMainLayout();
	}
	
	private void buildMainLayout() {
		mainLayout = new VerticalLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("100%");
		mainLayout.setHeight("100%");
		
		// top-level component properties
		setWidth("100%");
		setHeight("100%");

		txtTarget = new TextField();
		txtTarget.setWidth("100%");
		txtTarget.setHeight("-1px");
		txtTarget.setCaption("Target path:");
		txtTarget.setRequired(true);
		mainLayout.addComponent(txtTarget);	
		mainLayout.setExpandRatio(txtTarget, 0);
		
		txtQuery = new TextArea();
		txtQuery.setWidth("100%");
		txtQuery.setHeight("100%");
		txtQuery.setCaption("SPARQL query:");
		txtTarget.setRequired(true);
		mainLayout.addComponent(txtQuery);
		mainLayout.setExpandRatio(txtQuery, 1);	
		
		setCompositionRoot(mainLayout);
	}

	@Override
	protected void setConfiguration(Configuration conf) throws ConfigException {
		txtTarget.setValue(conf.getTargetPath());
		txtQuery.setValue(conf.getQuery());
	}

	@Override
	protected Configuration getConfiguration() throws ConfigException {
		if (!txtTarget.isValid()) {
			throw new ConfigException("Target path must be filled.");
		}
		if (!txtQuery.isValid()) {
			throw new ConfigException("SPARQL query must be filled.");
		}
		
		Configuration conf = new Configuration();
		conf.setTargetPath(txtTarget.getValue());
		conf.setQuery(txtQuery.getValue());
		return conf;		
	}

	@Override
	public String getDescription() {
		StringBuilder desc = new StringBuilder();
		
		desc.append("Export data as csv into ");
		desc.append(txtTarget.getValue());
		
		return desc.toString();
	}
	
}
