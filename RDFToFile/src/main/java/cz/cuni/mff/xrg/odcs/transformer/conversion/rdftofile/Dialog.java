package cz.cuni.mff.xrg.odcs.transformer.conversion.rdftofile;

import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import cz.cuni.mff.xrg.odcs.commons.configuration.ConfigException;
import cz.cuni.mff.xrg.odcs.commons.module.dialog.BaseConfigDialog;
import cz.cuni.mff.xrg.odcs.rdf.enums.RDFFormatType;
import java.util.List;

/**
 * Configuration dialog.
 * 
 * @author Škoda Petr
 */
public class Dialog extends BaseConfigDialog<Configuration> {
	
	private VerticalLayout mainLayout;
	
	private ComboBox cmbFormat;

	private TextField txtFileName;
	
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
		
		cmbFormat = new ComboBox();
		cmbFormat.setCaption("Output RDF format:");
		cmbFormat.setImmediate(true);
		cmbFormat.setWidth("-1px");
		cmbFormat.setHeight("-1px");
		cmbFormat.setNewItemsAllowed(false);
		cmbFormat.setNullSelectionAllowed(false);
		mainLayout.addComponent(cmbFormat);
		
		txtFileName = new TextField();
		txtFileName.setWidth("100%");
		txtFileName.setHeight("-1px");
		txtFileName.setCaption("Output file name: (without extension)");
		txtFileName.setRequired(true);
		mainLayout.addComponent(txtFileName);
		
		mainLayout.addComponent(new Label("Use '/' in file name to denote directory."));
		
		List<RDFFormatType> formatTypes = RDFFormatType.getListOfRDFType();
		for (RDFFormatType next : formatTypes) {
			if (next != RDFFormatType.AUTO) {
				final String formatValue = RDFFormatType.getStringValue(next);
				cmbFormat.addItem(formatValue);
			}
		}
		
		setCompositionRoot(mainLayout);
	}

	@Override
	protected void setConfiguration(Configuration c) throws ConfigException {
		cmbFormat.setValue(RDFFormatType.getStringValue(c.getRDFFileFormat()));
		txtFileName.setValue(c.getFileName());
	}

	@Override
	protected Configuration getConfiguration() throws ConfigException {
		if (!txtFileName.isValid()) {
			throw new ConfigException("Output file name must be specified!");
		}
		
		final String formatValue = (String) cmbFormat.getValue();
		RDFFormatType RDFFileFormat = RDFFormatType.getTypeByString(
				formatValue);

		Configuration cnf = new Configuration();
		cnf.setRDFFileFormat(RDFFileFormat);
		cnf.setFileName(getFilePath());
		return cnf;
	}

	@Override
	public String getDescription() {
		StringBuilder desc = new StringBuilder();
		
		desc.append("Dump into '");
		desc.append(getFilePath());
		desc.append("' in format '");
		desc.append((String) cmbFormat.getValue());
		desc.append("'");
		
		return desc.toString();
	}
	
	/**
	 * Return file path with '/' as separators.
	 * @return 
	 */
	private String getFilePath() {
		return txtFileName.getValue().replaceAll("\\\\", "/");
	}	
	
}
