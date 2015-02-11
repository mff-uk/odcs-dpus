package cz.cuni.mff.xrg.uv.boost.serialization.rdf;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;

import cz.cuni.mff.xrg.uv.boost.ontology.Ontology;
import cz.cuni.mff.xrg.uv.test.boost.rdf.RdfDataUnitFactory;
import cz.cuni.mff.xrg.uv.test.boost.rdf.TripleInserter;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import junit.framework.Assert;

/**
 * TODO Petr: Test for enum!
 *
 * @author Škoda Petr
 */
public class SerializationRdfTest {

    @Ontology.Entity(type = "http://localhost/ontology/SecondLevel")
    public static class SecondLevel {

        @Ontology.Property(uri = "http://localhost/ontology/value")
        private String value;

        public SecondLevel() {
        }

        public SecondLevel(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

    }

    @Ontology.Entity(type = "http://localhost/ontology/FirstLevel")
    public static class FirstLevel {

        private int value;

        private List<SecondLevel> collection = new LinkedList<>();

        public FirstLevel() {
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        public List<SecondLevel> getCollection() {
            return collection;
        }

        public void setCollection(List<SecondLevel> collection) {
            this.collection = collection;
        }

    }

    @Test
    public void deserialization() throws Exception {
        // Prepare rdf data.
        final WritableRDFDataUnit dataUnit = RdfDataUnitFactory.createInMemory();
        final URI graphUri = ValueFactoryImpl.getInstance().createURI("http://localhost/testGraph");
        try (TripleInserter ins = TripleInserter.create(dataUnit, graphUri)) {
            ins.subject("http://localhost/root")
                    .add("http://localhost/ontology/FirstLevel/value", 12)
                    .add("http://localhost/ontology/FirstLevel/collection", 12)
                    .addUri("http://localhost/ontology/FirstLevel/collection",
                            "http://localhost/resource/object");
            ins.subject("http://localhost/resource/object")
                    // Utilize property mapping.
                    .add("http://localhost/ontology/value", "hi");
        }
        // Prepare context object.
        RDFDataUnit.Entry entry = Mockito.mock(RDFDataUnit.Entry.class);
        Mockito.when(entry.getDataGraphURI()).thenReturn(graphUri);
        // Deserialize object.
        SerializationRdf serilizer = SerializationRdfFactory.rdfSimple();
        FirstLevel object = new FirstLevel();
        serilizer.convert(dataUnit.getConnection(),
                ValueFactoryImpl.getInstance().createURI("http://localhost/root"),
                Arrays.asList(entry), object, null);
        // Test.
        Assert.assertEquals(12, object.value);
        Assert.assertEquals(2, object.collection.size());
        for (SecondLevel item :object.collection) {
            Assert.assertTrue(item.value.equals("12") || item.value.equals("hi"));
        }
    }

}
