package cz.cuni.mff.scraper.lib.generator;

import java.net.URL;

/**
 *
 * @author Jakub Starka
 */
public abstract class DerivedURIGenerator extends TemplateURIGenerator{
    protected TemplateURIGenerator generator;

    public DerivedURIGenerator(TemplateURIGenerator generator) {
        super();
        this.generator = generator;
    }

    @Override
    protected abstract URL generateUrl();
    
    
}
