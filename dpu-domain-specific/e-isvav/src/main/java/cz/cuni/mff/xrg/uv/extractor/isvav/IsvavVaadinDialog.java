package cz.cuni.mff.xrg.uv.extractor.isvav;

import com.vaadin.data.Property;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

public class IsvavVaadinDialog extends AbstractDialog<IsvavConfig_V1> {

	private ComboBox cmbSource;
	
    private OptionGroup optionExportType;
    
    private TextField txtStartYear;
    
    private TextField txtFinalYear;

	public IsvavVaadinDialog() {
		super(Isvav.class);
	}

    @Override
    protected void buildDialogLayout() {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);
		
		this.cmbSource = new ComboBox("Source:");
		this.cmbSource.setNewItemsAllowed(false);
		this.cmbSource.setNullSelectionAllowed(false);
        this.cmbSource.setWidth("20em");

		this.cmbSource.setItemCaptionMode(ItemCaptionMode.EXPLICIT);
		
		// add items
		
		this.cmbSource.addItem(SourceType.Funder);
		this.cmbSource.setItemCaption(SourceType.Funder, "cea - State Funding Providers");
		
		this.cmbSource.addItem(SourceType.Organization);
		this.cmbSource.setItemCaption(SourceType.Organization, "cea - Organization Active in R&D");
		
		this.cmbSource.addItem(SourceType.Programme);
		this.cmbSource.setItemCaption(SourceType.Programme, "cea - R&D programmes");
		
		this.cmbSource.addItem(SourceType.Project);
		this.cmbSource.setItemCaption(SourceType.Project, "cep - Projects");
		
		this.cmbSource.addItem(SourceType.Research);
		this.cmbSource.setItemCaption(SourceType.Research, "cez - Institutional Reserch Plans");
		
		this.cmbSource.addItem(SourceType.Result);
		this.cmbSource.setItemCaption(SourceType.Result, "riv - Results of R&D");
		
		this.cmbSource.addItem(SourceType.Tender);
		this.cmbSource.setItemCaption(SourceType.Tender, "ves - Tenders in R&D");
		
		// ...
        mainLayout.addComponent(this.cmbSource);

        this.txtStartYear = new TextField("Start year:");
        mainLayout.addComponent(txtStartYear);

        this.txtFinalYear = new TextField("Final year:");
        mainLayout.addComponent(txtFinalYear);

        this.optionExportType = new OptionGroup("Export type:");
		this.optionExportType.addItem(IsvavConfig_V1.EXPORT_TYPE_DBF);
		this.optionExportType.addItem(IsvavConfig_V1.EXPORT_TYPE_XLS);
        this.optionExportType.setNullSelectionAllowed(false);
        this.optionExportType.setValue(IsvavConfig_V1.EXPORT_TYPE_DBF);
                
		mainLayout.addComponent(this.optionExportType);
		setCompositionRoot(mainLayout);

        this.cmbSource.addValueChangeListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                boolean enableYearSelection = SourceType.Project.equals(event.getProperty().getValue());
                txtStartYear.setEnabled(enableYearSelection);
                txtFinalYear.setEnabled(enableYearSelection);
            }
        });
	}
	
	@Override
	public void setConfiguration(IsvavConfig_V1 conf) throws DPUConfigException {
		cmbSource.setValue(conf.getSourceType());
        optionExportType.setValue(conf.getExportType());
        txtStartYear.setValue(conf.getStartYear() == null ? "" : conf.getStartYear().toString());
        txtFinalYear.setValue(conf.getFinalYear() == null ? "" : conf.getFinalYear().toString());

	}

	@Override
	public IsvavConfig_V1 getConfiguration() throws DPUConfigException {
		final IsvavConfig_V1 config = new IsvavConfig_V1();
		config.setSourceType((SourceType)cmbSource.getValue());
        config.setExportType((String)optionExportType.getValue());

        try {
            config.setStartYear(Integer.parseInt(txtStartYear.getValue()));
        } catch (NumberFormatException ex) {
            config.setStartYear(null);
        }

        try {
            config.setFinalYear(Integer.parseInt(txtFinalYear.getValue()));
        } catch (NumberFormatException ex) {
            config.setFinalYear(null);
        }

		return config;
	}

	@Override
	public String getDescription() {
		StringBuilder desc = new StringBuilder();
		desc.append(cmbSource.getItemCaption(cmbSource.getValue()));
		return desc.toString();
	}
	
}
