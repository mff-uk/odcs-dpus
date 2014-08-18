package cz.cuni.mff.xrg.uv.extractor.isvav;

import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.VerticalLayout;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AdvancedVaadinDialogBase;
import eu.unifiedviews.dpu.config.DPUConfigException;

public class IsvavVaadinDialog extends AdvancedVaadinDialogBase<IsvavConfig_V1> {

	private ComboBox cmbSource;
	
    private OptionGroup optionExportType;
    
	public IsvavVaadinDialog() {
		super(IsvavConfig_V1.class, AddonInitializer.noAddons());

		buildLayout();
	}

	private void buildLayout() {
        // top-level component properties
        setWidth("100%");
        setHeight("100%");
		
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);
		
		this.cmbSource = new ComboBox("Source:");
		this.cmbSource.setNewItemsAllowed(false);
		this.cmbSource.setNullSelectionAllowed(false);

		this.cmbSource.setItemCaptionMode(ItemCaptionMode.EXPLICIT);
		
		// add items
		
		this.cmbSource.addItem(SourceType.Funder);
		this.cmbSource.setItemCaption(SourceType.Funder, "Poskytovatelé podpory");
		
		this.cmbSource.addItem(SourceType.Organization);
		this.cmbSource.setItemCaption(SourceType.Organization, "Subjekty ve VaVaI");
		
		this.cmbSource.addItem(SourceType.Programme);
		this.cmbSource.setItemCaption(SourceType.Programme, "Programy VaVaI");
		
		this.cmbSource.addItem(SourceType.Project);
		this.cmbSource.setItemCaption(SourceType.Project, "Projekty VaVaI");
		
		this.cmbSource.addItem(SourceType.Research);
		this.cmbSource.setItemCaption(SourceType.Research, "Výzkumné záměry");
		
		this.cmbSource.addItem(SourceType.Result);
		this.cmbSource.setItemCaption(SourceType.Result, "Výsledky");
		
		this.cmbSource.addItem(SourceType.Tender);
		this.cmbSource.setItemCaption(SourceType.Tender, "Veřejné soutěže ve VaVaI");
		
		// ...
        mainLayout.addComponent(this.cmbSource);
        
        this.optionExportType = new OptionGroup("Export type:");
		this.optionExportType.addItem(IsvavConfig_V1.EXPORT_TYPE_DBF);
		this.optionExportType.addItem(IsvavConfig_V1.EXPORT_TYPE_XLS);
        this.optionExportType.setNullSelectionAllowed(false);
        this.optionExportType.setValue(IsvavConfig_V1.EXPORT_TYPE_DBF);
                
		mainLayout.addComponent(this.optionExportType);
		setCompositionRoot(mainLayout);
	}
	
	@Override
	public void setConfiguration(IsvavConfig_V1 conf) throws DPUConfigException {
		cmbSource.setValue(conf.getSourceType());
        optionExportType.setValue(conf.getExportType());
	}

	@Override
	public IsvavConfig_V1 getConfiguration() throws DPUConfigException {
		IsvavConfig_V1 config = new IsvavConfig_V1();
		config.setSourceType((SourceType)cmbSource.getValue());
        config.setExportType((String)optionExportType.getValue());
		return config;
	}

	@Override
	public String getDescription() {
		StringBuilder desc = new StringBuilder();
				
		desc.append("Extract ");		
		desc.append(cmbSource.getItemCaption(cmbSource.getValue()));
		desc.append(" from http://www.isvav.cz/ as ");
        desc.append(optionExportType.getValue());
		
		return desc.toString();
	}
	
}
