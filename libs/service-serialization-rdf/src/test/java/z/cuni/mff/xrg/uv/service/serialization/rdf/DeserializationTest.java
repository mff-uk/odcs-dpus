package z.cuni.mff.xrg.uv.service.serialization.rdf;

import cz.cuni.mff.xrg.uv.service.serialization.rdf.SerializationRdf;
import cz.cuni.mff.xrg.uv.service.serialization.rdf.SerializationRdfFactory;
import cz.cuni.mff.xrg.uv.test.boost.rdf.RDFDataUnitFactory;
import cz.cuni.mff.xrg.uv.test.boost.rdf.TripleInserter;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import java.util.LinkedList;
import java.util.List;
import junit.framework.Assert;
import org.junit.Test;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 *
 * @author Škoda Petr
 */
public class DeserializationTest {

    public static class SecondLevel {

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
        // prepare configuration
        SerializationRdf.Configuration configuration = new SerializationRdf.Configuration();
        final String onto = "http://ontology/";
        final String res = "http://resource/";
        configuration.setOntologyPrefix(onto);
        // add property mapping
        configuration.getPropertyMap().put(onto + "text", "collection.value");
        // prepare rdf data
        WritableRDFDataUnit dataUnit = RDFDataUnitFactory.createInMemory();

        try (TripleInserter ins = TripleInserter.create(dataUnit)) {
            ins.subject("http://localhost/root")
                    .add(onto + "value", 12)
                    .add(onto + "collection", 12)
                    .addUri(onto + "collection", res + "object");
            ins.subject(res + "object")
                    // utilize property mapping
                    .add(onto + "text", "hi");
        }

        // prepare serialization class
        SerializationRdf<FirstLevel> serilizer = SerializationRdfFactory.rdfSimple(FirstLevel.class);
        // deserialize
        FirstLevel object = new FirstLevel();
        serilizer.rdfToObject(dataUnit, ValueFactoryImpl.getInstance().createURI("http://localhost/root"),
                object, configuration);
        // valrify the result
        Assert.assertEquals(12, object.value);
        Assert.assertEquals(2, object.collection.size());
        for (SecondLevel item :object.collection) {
            Assert.assertTrue(item.value.equals("12") || item.value.equals("hi"));
        }
    }

}
