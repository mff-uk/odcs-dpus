package cz.cuni.mff.xrg.uv.addressmapper.knowledgebase;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Škoda Petr
 */
public class KnowledgeBaseTest {

    KnowledgeBase knowledge = new KnowledgeBase("http://ruian.linked.opendata.cz/solr/ruian/query");

    @Test
    public void testVusc_00() throws KnowledgeBaseException {
        final List<String> values = knowledge.getVusc("Praha");
        
        Assert.assertEquals(1, values.size());
        Assert.assertEquals("Hlavní město Praha", values.get(0));        
    }

    @Test
    public void testVusc_01() throws KnowledgeBaseException {
        final List<String> values = knowledge.getOkres("České Budějovice");

        Assert.assertEquals(1, values.size());
        Assert.assertEquals("České Budějovice", values.get(0));
    }


    @Test
    public void testObec() throws KnowledgeBaseException {
        final List<String> values = knowledge.getObec("Libušín");

        Assert.assertEquals(1, values.size());
        Assert.assertEquals("Libušín", values.get(0));
    }

    @Test
    public void testObecInVusc_nonConflict() throws KnowledgeBaseException {
        final List<String> values = knowledge.getObecInVusc("Libušín", "Středočeský kraj");

        Assert.assertEquals(1, values.size());
        Assert.assertEquals("Libušín", values.get(0));
    }

    @Test
    public void testObecInVusc_null() throws KnowledgeBaseException {
        final List<String> values = knowledge.getObecInVusc("Libušín", null);

        Assert.assertEquals(1, values.size());
        Assert.assertEquals("Libušín", values.get(0));
    }

    @Test
    public void testUliceInObec_nonConflict() throws KnowledgeBaseException {
        final List<String> values = knowledge.getUliceInObec("Písečná", "Hlučín");

        Assert.assertEquals(1, values.size());
        Assert.assertEquals("Písečná", values.get(0));
    }

    @Test
    public void testUliceInObec_null() throws KnowledgeBaseException {
        final List<String> values = knowledge.getUliceInObec("Písečná", null);

        Assert.assertEquals(1, values.size());
        Assert.assertEquals("Písečná", values.get(0));
    }


    @Test
    public void testCastObceInObec_nonConflict() throws KnowledgeBaseException {
        final List<String> values = knowledge.getCastObceInObec("Dožice", "Mladý Smolivec");

        Assert.assertEquals(1, values.size());
        Assert.assertEquals("Dožice", values.get(0));
    }

    @Test
    public void testCastObceInObec_null() throws KnowledgeBaseException {
        final List<String> values = knowledge.getCastObceInObec("Dožice", null);

        Assert.assertEquals(1, values.size());
        Assert.assertEquals("Dožice", values.get(0));
    }


}

