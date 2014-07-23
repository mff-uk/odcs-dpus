package cz.cuni.mff.xrg.uv.transformer.filter.file;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.SimpleConfigDialogBase;
import eu.unifiedviews.dpu.config.DPUConfigException;

public class Dialog extends SimpleConfigDialogBase<Configuration> {
	
	private VerticalLayout mainLayout;
	
	private TextField txtFilter;
	
	public Dialog() {
		super(Configuration.class);
		buildMainLayout();
	}
	
	private void buildMainLayout() {
		setWidth("100%");
		setHeight("100%");

		mainLayout = new VerticalLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("100%");
		mainLayout.setHeight("-1px");
		
		txtFilter = new TextField("File filter (regexp):");
		txtFilter.setRequired(true);
		txtFilter.setWidth("100%");
		mainLayout.addComponent(txtFilter);
		
		Label label = new Label();
		label.setContentMode(ContentMode.HTML);
		label.setValue("Given regular expression is used to match rooted file path.");
		mainLayout.addComponent(label);
		
		setCompositionRoot(mainLayout);
	}

	@Override
	protected void setConfiguration(Configuration c) throws DPUConfigException {
		txtFilter.setValue(c.getFilter());
	}

	@Override
	protected Configuration getConfiguration() throws DPUConfigException {
		if (!txtFilter.isValid()) {
			throw new DPUConfigException("Filter must be set.");
		}		
		Configuration cnf = new Configuration();
		cnf.setFilter(txtFilter.getValue());
		return cnf;
	}

	@Override
	public String getDescription() {
		StringBuilder desc = new StringBuilder();
		
		desc.append(txtFilter.getValue());
		
		return desc.toString();
	}

}
