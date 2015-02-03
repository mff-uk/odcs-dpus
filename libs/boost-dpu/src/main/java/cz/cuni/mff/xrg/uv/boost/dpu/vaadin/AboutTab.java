package cz.cuni.mff.xrg.uv.boost.dpu.vaadin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

/**
 * About page for DPU. Content should be generated based on context.
 *
 * TODO Petr: We should consider localization here too.
 *
 * @author Škoda Petr
 */
public class AboutTab extends CustomComponent {

    private static final Logger LOG = LoggerFactory.getLogger(AboutTab.class);

    private final String BUNDLE_NAME = "build-info";

    private final String HTML_SYSTEM_PROPERTIES = "<b>System properties</b><br>"
            + "<ul>"
            + "<li>Build time: %s</li>"
            + "</ul>"
            + "<br/><hr/>";

    private final String HTML_FOOTER = "<br/><hr/>"
            + "Powered by <a href=\"https://github.com/mff-uk\">CUNI helpers</a>.";

    public AboutTab() {
        // No-op here.
    }

    @Override
    public String getCaption() {
        return "About";
    }

    public void buildLayout(DialogContext context) {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setMargin(true);
        mainLayout.setSpacing(false);

        // Informations from build-info.properties.
        final ResourceBundle buildInfo = ResourceBundle.getBundle(BUNDLE_NAME,
                context.getDialogContext().getLocale(),
                context.getDpuClass().getClassLoader());

        final String buildTime = buildInfo.getString("build.timestamp");
        mainLayout.addComponent(
                new Label(String.format(HTML_SYSTEM_PROPERTIES, buildTime),
                        ContentMode.HTML));

        // Add user provided description if available.
        final String userDescription = loadUserAboutText(
                context);
        if (userDescription != null) {
            mainLayout.addComponent(new Label(userDescription, ContentMode.HTML));
        }

        // Logo at the verz end.
        mainLayout.addComponent(new Label(HTML_FOOTER, ContentMode.HTML));

        // Wrap all into a panel.
        final Panel panel = new Panel();
        panel.setSizeFull();
        panel.setContent(mainLayout);
        setCompositionRoot(panel);
    }

    protected String loadUserAboutText(DialogContext context) {
        final ClassLoader classLoader = context.getDpuClass().getClassLoader();
        final Locale locale = context.getDialogContext().getLocale();
        // TODO Petr: This is probably not a good idea how to do this.
        // Try file based on curent localzation.
        String fileName = "about_" + locale.toLanguageTag() + ".html";
        final String result = loadStringFromResource(classLoader, fileName);
        if (result != null) {
            return result;
        } else {
            // Use fallback.
            fileName = "about.html";
            return loadStringFromResource(classLoader, fileName);
        }
    }

    protected String loadStringFromResource(ClassLoader classLoader, String resourceName) {
        try (InputStream inStream = classLoader.getResourceAsStream(resourceName)) {
            if (inStream == null) {
                // Missing resource.
                return null;
            }
            final BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));
            final StringBuilder builder = new StringBuilder(256);
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        } catch (IOException ex) {
            LOG.error("Failed to load about.html.", ex);
            return null;
        }
    }

}
