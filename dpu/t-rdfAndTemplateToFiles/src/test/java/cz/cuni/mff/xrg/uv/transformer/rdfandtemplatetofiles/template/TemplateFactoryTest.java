package cz.cuni.mff.xrg.uv.transformer.rdfandtemplatetofiles.template;

import org.junit.Assert;
import org.junit.Test;

import eu.unifiedviews.dpu.DPUException;

/**
 *
 * @author Škoda Petr
 */
public class TemplateFactoryTest {

    @Test
    public void onlyText() throws DPUException {
        CompositeTemplate root = (CompositeTemplate)TemplateFactory.create(
                "AA$AA");
        Assert.assertEquals(1, root.templates.size());
        Assert.assertEquals("AA$AA", ((ConstantTemplate)root.templates.get(0)).value);
    }

    @Test
    public void textWithEscapedTemplate() throws DPUException {
        CompositeTemplate root = (CompositeTemplate)TemplateFactory.create(
                "AA\\${A\\}A");
        Assert.assertEquals(1, root.templates.size());
        Assert.assertEquals("AA${A}A", ((ConstantTemplate)root.templates.get(0)).value);
    }

    @Test
    public void singleValueTemplate() throws DPUException {
        CompositeTemplate root = (CompositeTemplate)TemplateFactory.create(
                "AAAA${http://localhost/value}BBBB");
        Assert.assertEquals(3, root.templates.size());
        Assert.assertEquals("AAAA", ((ConstantTemplate)root.templates.get(0)).value);
        Assert.assertEquals("http://localhost/value", ((ValueTemplate)root.templates.get(1)).predicate.stringValue());
        Assert.assertEquals("BBBB", ((ConstantTemplate)root.templates.get(2)).value);
    }

    @Test
    public void threeValueTemplates() throws DPUException {
        CompositeTemplate root = (CompositeTemplate)TemplateFactory.create(
                "AAAA${http://localhost/0}BBBB${http://localhost/1}${http://localhost/2}");
        Assert.assertEquals(5, root.templates.size());
        Assert.assertEquals("AAAA", ((ConstantTemplate)root.templates.get(0)).value);
        Assert.assertEquals("http://localhost/0", ((ValueTemplate)root.templates.get(1)).predicate.stringValue());
        Assert.assertEquals("BBBB", ((ConstantTemplate)root.templates.get(2)).value);
        Assert.assertEquals("http://localhost/1", ((ValueTemplate)root.templates.get(3)).predicate.stringValue());
        Assert.assertEquals("http://localhost/2", ((ValueTemplate)root.templates.get(4)).predicate.stringValue());
    }

    @Test
    public void compositeTemplate() throws DPUException {
        CompositeTemplate root = (CompositeTemplate)TemplateFactory.create(
                "AAAA${http://localhost/c|BBBB${http://localhost/1}}CCCC");
        Assert.assertEquals(3, root.templates.size());
        Assert.assertEquals("AAAA", ((ConstantTemplate)root.templates.get(0)).value);
        Assert.assertEquals("http://localhost/c", ((CompositeTemplate)root.templates.get(1)).predicate.stringValue());
        Assert.assertEquals("CCCC", ((ConstantTemplate)root.templates.get(2)).value);

        CompositeTemplate composite = ((CompositeTemplate)root.templates.get(1));
        Assert.assertEquals(2, composite.templates.size());        
        Assert.assertEquals("BBBB", ((ConstantTemplate)composite.templates.get(0)).value);
        Assert.assertEquals("http://localhost/1", ((ValueTemplate)composite.templates.get(1)).predicate.stringValue());
    }

}
