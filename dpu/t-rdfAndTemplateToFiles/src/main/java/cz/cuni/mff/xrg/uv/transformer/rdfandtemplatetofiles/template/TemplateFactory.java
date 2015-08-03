package cz.cuni.mff.xrg.uv.transformer.rdfandtemplatetofiles.template;

import java.util.Stack;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.dpu.DPUException;

/**
 *
 * @author Škoda Petr
 */
public class TemplateFactory {

    private static final Logger LOG = LoggerFactory.getLogger(TemplateFactory.class);

    private static final ValueFactory valueFactory = ValueFactoryImpl.getInstance();

    private TemplateFactory() {

    }

    /**
     * Parse given input into a template.
     *
     * @param input
     * @return Template parsed from given template string.
     * @throws DPUException
     */
    public static Template create(String input) throws DPUException {
        int position = 0; // Point to next character to read.
        final Stack<CompositeTemplate> templates = new Stack<>();
        templates.add(new RootTemplate());
        // Only CompositeTemplate can be used as current templates.
        int size = input.length();
        // If true next character is just read and not parsed.
        boolean ignoreNext = false;
        // We will read the templates into this object.
        StringBuilder templateContent = new StringBuilder();
        while (position < size) {
            // Move to next characeter and read it.            
            char current = input.charAt(position++);
            if (ignoreNext) {
                templateContent.append(current);
                ignoreNext = false;
            } else if (current == '\\') {
                // Escaping.
                ignoreNext = true;
            } else if (current == '$') {
                // Check for next character.
                if (charAt(input, position) == '{') {
                    // Consume '{'
                    position++;
                    // Before reading new template same the old one, if it's not empty.
                    if (templateContent.length() > 0) {
                        templates.peek().templates.add(new ConstantTemplate(templateContent.toString()));
                        // Now we can start reading new tamplate.
                        templateContent = new StringBuilder();
                    }
                    // Read uri, till the '}' or '|'
                    current = charAt(input, position++);
                    while (current != '}' && current != '|') {
                        templateContent.append(current);
                        // Read next characer.
                        current = charAt(input, position++);
                    }
                    // Now we have read till '|' or '}'. In every case we have predicate
                    // stored in templateContent.
                    final URI predicate = valueFactory.createURI(templateContent.toString());
                    templateContent = new StringBuilder();
                    if (current == '}') {
                        // Simple template.
                        templates.peek().templates.add(new ValueTemplate(predicate));
                    } else if (current == '|') {
                        // Composite template.
                        CompositeTemplate newComposite = new CompositeTemplate(predicate);
                        templates.peek().templates.add(newComposite);
                        templates.add(newComposite);
                    } else {
                        // End of file.
                        LOG.info("Last read template: {}", predicate);
                        throw new DPUException("Unexpected end of file.");
                    }
                } else {
                    // Nothing special, just continue.
                    templateContent.append(current);
                }
            } else if (current == '}') {
                // If we are here, then we are at then end of a composite template.
                // As first we add readed content as a ConstTemplate.
                if (templateContent.length() != 0) {
                    templates.peek().templates.add(new ConstantTemplate(templateContent.toString()));
                    templateContent = new StringBuilder();
                }
                // Then close the template.
                templates.pop();
                if (templates.isEmpty()) {
                    LOG.info("For each  '${' there must be exactly one following '}' presented.");
                    LOG.info("Problem detected on position {}", position);
                    throw new DPUException("Unexpected number of templates! See logs for details.");
                }
            } else {                
                // Just read.
                templateContent.append(current);
            }
        }
        if (templates.size() != 1) {
            LOG.info("Number of templates: {}", templates.size());
            LOG.info("For each  '${' there must be exactly one following '}' presented.");
            throw new DPUException("Unexpected number of templates at the end of file.");
        }
        // Add the rest of the text.
        if (templateContent.length() != 0) {
            templates.peek().templates.add(new ConstantTemplate(templateContent.toString()));
        }
        return templates.peek();
    }

    /**
     * Can be used to read string without the worry for it's end.
     *
     * @param string
     * @param position
     * @return Character on given position or 0 if position is outside the string.
     */
    private static char charAt(String string, int position) {
        if (position < string.length()) {
            return string.charAt(position);
        } else {
            return 0;
        }
    }

}
