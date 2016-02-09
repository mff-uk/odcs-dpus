package cz.cuni.mff.xrg.uv.loader.deleteDirectory;

import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

public class DeleteDirectoryVaadinDialog extends AbstractDialog<DeleteDirectory_V1> {

    private TextField txtDirectory;

    public DeleteDirectoryVaadinDialog() {
        super(DeleteDirectory.class);
    }

    @Override
    public void setConfiguration(DeleteDirectory_V1 c) throws DPUConfigException {
        txtDirectory.setValue(c.getDirectory());
    }

    @Override
    public DeleteDirectory_V1 getConfiguration() throws DPUConfigException {
        final DeleteDirectory_V1 c = new DeleteDirectory_V1();
        c.setDirectory(txtDirectory.getValue());
        return c;
    }

    @Override
    protected void buildDialogLayout() {
        setSizeFull();

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setMargin(true);

        txtDirectory = new TextField("Directory to delete:");
        txtDirectory.setWidth("100%");
        mainLayout.addComponent(txtDirectory);

        setCompositionRoot(mainLayout);
    }
}
