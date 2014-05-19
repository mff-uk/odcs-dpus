package cz.cuni.mff.xrg.odcs.transformer.conversion.rdftofile;

import com.vaadin.data.Property;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import cz.cuni.mff.xrg.odcs.commons.configuration.ConfigException;
import cz.cuni.mff.xrg.odcs.commons.module.dialog.BaseConfigDialog;
import cz.cuni.mff.xrg.odcs.rdf.enums.RDFFormatType;
import java.util.Collection;
import java.util.List;
import org.openrdf.rio.RDFFormat;

/**
 * Configuration dialog.
 * 
 * @author Škoda Petr
 */
public class Dialog extends BaseConfigDialog<Configuration> {
	
	private VerticalLayout mainLayout;
	
	private ComboBox cmbFormat;

	private TextField txtFileName;
	
	private CheckBox chbGenerateGraphFile;
	
	private TextField txtGraphUri;
	
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
		txtFileName.setCaption("Output file name: (with extension)");
		txtFileName.setRequired(true);
		mainLayout.addComponent(txtFileName);
		
		mainLayout.addComponent(new Label("Use '/' in file name to denote directory."));
		
		Collection<RDFFormat> formatTypes = RDFFormat.values();
        for (RDFFormat formatType : formatTypes) {
            String formatValue = formatType.getName();
			cmbFormat.addItem(formatValue);
		}
		
		chbGenerateGraphFile = new CheckBox();
		chbGenerateGraphFile.setCaption("Generate .graph file");
		mainLayout.addComponent(chbGenerateGraphFile);
		
		chbGenerateGraphFile.addValueChangeListener(new Property.ValueChangeListener() {

			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				if ((Boolean)event.getProperty().getValue() == true) {
					txtGraphUri.setEnabled(true);
				} else {
					txtGraphUri.setEnabled(false);
				}
			}
		});
		
		txtGraphUri = new TextField();
		txtGraphUri.setWidth("100%");
		txtGraphUri.setHeight("-1px");
		txtGraphUri.setCaption("Graph URI");
		mainLayout.addComponent(txtGraphUri);
		
		
		setCompositionRoot(mainLayout);
	}

	@Override
	protected void setConfiguration(Configuration c) throws ConfigException {
		cmbFormat.setValue(c.getRDFFileFormat().getName());
		txtFileName.setValue(c.getFileName());
		chbGenerateGraphFile.setValue(c.isGenGraphFile());
		if (c.isGenGraphFile()) {
			txtGraphUri.setEnabled(true);
			txtGraphUri.setValue(c.getGraphUri());
		} else {
			txtGraphUri.setEnabled(false);
		}
	}

	@Override
	protected Configuration getConfiguration() throws ConfigException {
		if (!txtFileName.isValid()) {
			throw new ConfigException("Output file name must be specified!");
		}
		
		Configuration cnf = new Configuration();
		
		String formatValue = (String) cmbFormat.getValue();
		cnf.setRDFFileFormat(RDFFormat.valueOf(formatValue));
		cnf.setFileName(getFilePath());
		
		if (chbGenerateGraphFile.getValue()) {
			cnf.setGenGraphFile(true);
			cnf.setGraphUri(txtGraphUri.getValue());
		} else {
			cnf.setGenGraphFile(false);
		}
		
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
