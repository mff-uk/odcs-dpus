package cz.cuni.mff.xrg.uv.addressmapper.objects;

import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 *
 * @author Škoda Petr
 */
public class RuianEntityTest {

    protected static final ValueFactory valueFactory = ValueFactoryImpl.getInstance();

    @Test
    public void toQuery_psc() {
        RuianEntity entity = new RuianEntity(valueFactory.createURI("http://localhost/resource/sample/00"));

        entity.setPsc(14800);

        String expected = ""
                + "SELECT ?s ?type WHERE { \n"
                + "  ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ruian.linked.opendata.cz/ontology/AdresniMisto> .\n"
                + "  ?s <http://ruian.linked.opendata.cz/ontology/psc> 14800 .\n"
                + "\n"
                + "  ?s a ?type .\n"
                + "}";
        Assert.assertEquals(expected, entity.asRuianQuery());
    }

    @Test
    public void toQuery_cisloDomovni_psc() {
        RuianEntity entity = new RuianEntity(valueFactory.createURI("http://localhost/resource/sample/00"));

        entity.setCisloDomovni(22);
        entity.setPsc(14800);

        String expected = ""
                + "SELECT ?s ?type WHERE { \n"
                + "  ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ruian.linked.opendata.cz/ontology/AdresniMisto> .\n"
                + "  ?s <http://ruian.linked.opendata.cz/ontology/cisloDomovni> 22 .\n"
                + "  ?s <http://ruian.linked.opendata.cz/ontology/psc> 14800 .\n"
                + "\n"
                + "  ?s a ?type .\n"
                + "}";
        Assert.assertEquals(expected, entity.asRuianQuery());
    }

    @Test
    public void toQuery_cisloDomovni_obec() {
        RuianEntity entity = new RuianEntity(valueFactory.createURI("http://localhost/resource/sample/00"));

        entity.setCisloDomovni(22);
        entity.setObec("Pardubice");

        String expected = ""
                + "SELECT ?s ?type WHERE { \n"
                + "  ?obec <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ruian.linked.opendata.cz/ontology/Obec> .\n"
                + "  ?obec <http://schema.org/name> \"Pardubice\" .\n"
                + "  ?ulice <http://ruian.linked.opendata.cz/ontology/obec> ?obec .\n"
                + "\n"
                + "  ?ulice <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ruian.linked.opendata.cz/ontology/Ulice> .\n"
                + "  ?s <http://ruian.linked.opendata.cz/ontology/ulice> ?ulice .\n"
                + "\n"
                + "  ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ruian.linked.opendata.cz/ontology/AdresniMisto> .\n"
                + "  ?s <http://ruian.linked.opendata.cz/ontology/cisloDomovni> 22 .\n"
                + "\n"
                + "  ?s a ?type .\n"
                + "}";
        Assert.assertEquals(expected, entity.asRuianQuery());
    }

    @Test
    public void toQuery_psc_castObce_vusc() {
        RuianEntity entity = new RuianEntity(valueFactory.createURI("http://localhost/resource/sample/00"));

        entity.setPsc(14800);
        entity.setCastObce("Jinonice");
        entity.setVusc("Jihovýchod");

        String expected = ""
                + "SELECT ?s ?type WHERE { \n"
                + "  ?vusc <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ruian.linked.opendata.cz/ontology/Vusc> .\n"
                + "  ?vusc <http://schema.org/name> \"Jihovýchod\" .\n"
                + "  ?orp <http://ruian.linked.opendata.cz/ontology/vusc> ?vusc .\n"
                + "\n"
                + "  ?orp <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ruian.linked.opendata.cz/ontology/Orp> .\n"
                + "  ?pou <http://ruian.linked.opendata.cz/ontology/orp> ?orp .\n"
                + "\n"
                + "  ?pou <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ruian.linked.opendata.cz/ontology/Pou> .\n"
                + "  ?obec <http://ruian.linked.opendata.cz/ontology/pou> ?pou .\n"
                + "\n"
                + "  ?obec <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ruian.linked.opendata.cz/ontology/Obec> .\n"
                + "  ?castObce <http://ruian.linked.opendata.cz/ontology/obec> ?obec .\n"
                + "\n"
                + "  ?castObce <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ruian.linked.opendata.cz/ontology/CastObce> .\n"
                + "  ?castObce <http://schema.org/name> \"Jinonice\" .\n"
                + "  ?stavebniObjekt <http://ruian.linked.opendata.cz/ontology/castObce> ?castObce .\n"
                + "\n"
                + "  ?stavebniObjekt <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ruian.linked.opendata.cz/ontology/StavebniObjekt> .\n"
                + "  ?s <http://ruian.linked.opendata.cz/ontology/stavebniObjekt> ?stavebniObjekt .\n"
                + "\n"
                + "  ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ruian.linked.opendata.cz/ontology/AdresniMisto> .\n"
                + "  ?s <http://ruian.linked.opendata.cz/ontology/psc> 14800 .\n"
                + "\n"
                + "  ?s a ?type .\n"
                + "}";
        Assert.assertEquals(expected, entity.asRuianQuery());
    }

}
