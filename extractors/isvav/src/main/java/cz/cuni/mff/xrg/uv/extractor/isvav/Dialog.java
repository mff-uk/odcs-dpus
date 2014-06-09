package cz.cuni.mff.xrg.uv.extractor.isvav;

import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.VerticalLayout;
import cz.cuni.mff.xrg.odcs.commons.configuration.ConfigException;
import cz.cuni.mff.xrg.odcs.commons.module.dialog.BaseConfigDialog;

public class Dialog extends BaseConfigDialog<Configuration> {

	private ComboBox cmbSource;
	
	public Dialog() {
		super(Configuration.class);
		buildMainLayout();
	}

	private void buildMainLayout() {
        // top-level component properties
        setWidth("100%");
        setHeight("100%");
		
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);
		
		cmbSource = new ComboBox("Source:");
		cmbSource.setNewItemsAllowed(false);
		cmbSource.setNullSelectionAllowed(false);

		cmbSource.setItemCaptionMode(ItemCaptionMode.EXPLICIT);
		
		// add items
		
		cmbSource.addItem(SourceType.Funder);		
		cmbSource.setItemCaption(SourceType.Funder, "Poskytovatelé podpory");
		
		cmbSource.addItem(SourceType.Organization);		
		cmbSource.setItemCaption(SourceType.Organization, "Subjekty ve VaVaI");
		
		cmbSource.addItem(SourceType.Programme);
		cmbSource.setItemCaption(SourceType.Programme, "Programy VaVaI");
		
		cmbSource.addItem(SourceType.Project);
		cmbSource.setItemCaption(SourceType.Project, "Projekty VaVaI");
		
		cmbSource.addItem(SourceType.Research);
		cmbSource.setItemCaption(SourceType.Research, "Výzkumné záměry");
		
		cmbSource.addItem(SourceType.Result);
		cmbSource.setItemCaption(SourceType.Result, "Výsledky");
		
		cmbSource.addItem(SourceType.Tender);
		cmbSource.setItemCaption(SourceType.Tender, "Veřejné soutěže ve VaVaI");
		
		// ...
		
		mainLayout.addComponent(cmbSource);	
		setCompositionRoot(mainLayout);
	}
	
	@Override
	public void setConfiguration(Configuration conf) throws ConfigException {
		cmbSource.setValue(conf.getSourceType());
	}

	@Override
	public Configuration getConfiguration() throws ConfigException {
		Configuration config = new Configuration();		
		config.setSourceType((SourceType)cmbSource.getValue());		
		return config;
	}

	@Override
	public String getDescription() {
		StringBuilder desc = new StringBuilder();
				
		desc.append("Extract ");		
		desc.append(cmbSource.getItemCaption(cmbSource.getValue()));
		desc.append(" from http://www.isvav.cz/");
		
		return desc.toString();
	}
	
}
