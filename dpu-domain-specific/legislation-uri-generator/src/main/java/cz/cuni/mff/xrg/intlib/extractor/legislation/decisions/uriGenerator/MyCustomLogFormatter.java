package cz.cuni.mff.xrg.intlib.extractor.legislation.decisions.uriGenerator;


import java.text.MessageFormat;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author Jakub Starka
 */
public class MyCustomLogFormatter extends SimpleFormatter {
    
    public MyCustomLogFormatter() {
        super();
    }

    @Override
    public String format(LogRecord event) {
        StringBuilder message = new StringBuilder();
        message.append(event.getLevel().getLocalizedName() + " : ");
        message.append(MessageFormat.format(event.getMessage(),event.getParameters()));
        message.append("\n");
        return message.toString();
    }
    
}
