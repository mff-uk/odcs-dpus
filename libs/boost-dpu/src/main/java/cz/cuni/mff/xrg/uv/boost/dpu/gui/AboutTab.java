package cz.cuni.mff.xrg.uv.boost.dpu.gui;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

/**
 * About page for DPU. Content should be generated based on context.
 * @author Škoda Petr
 */
public class AboutTab extends CustomComponent {

    /**
     * Dialog context.
     */
    private AbstractVaadinDialog.DialogContext context = null;

    public AboutTab() {
        // No-op here.
    }

    @Override
    public String getCaption() {
        return "About";
    }

    public void buildLayout(AbstractVaadinDialog.DialogContext context) {
        this.context = context;

        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);

        mainLayout.addComponent(
                new Label("Powered by <a href=\"https://github.com/mff-uk\">CUNI helpers</a>.",
                        ContentMode.HTML));

        final Panel panel = new Panel();
        panel.setSizeFull();
        panel.setContent(mainLayout);
        setCompositionRoot(panel);
    }

}
