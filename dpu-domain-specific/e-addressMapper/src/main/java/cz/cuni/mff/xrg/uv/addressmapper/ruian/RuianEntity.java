package cz.cuni.mff.xrg.uv.addressmapper.ruian;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.uv.addressmapper.AddressMapperOntology;
import cz.cuni.mff.xrg.uv.addressmapper.objects.Report;
import cz.cuni.mff.xrg.uv.addressmapper.address.StringAddress;
import eu.unifiedviews.helpers.dpu.rdf.EntityBuilder;

/**
 *
 * @author Škoda Petr
 */
public class RuianEntity {

    private static final Logger LOG = LoggerFactory.getLogger(RuianEntity.class);

    private static final String ADRESNI_MISTO = "?adrMisto";

    private static final String ULICE = "?ulice";

    private static final String STAVEBNI_OBJEKT = "?stavebniObjekt";

    private static final String CAST_OBCE = "?castObce";

    private static final String OBEC = "?obec";

    private static final String POU = "?pou";

    private static final String ORP = "?orp";

    private static final String OKRES = "?okres";

    private static final String VUSC = "?vusc";

    public static final String ENTITY_BINDING = "s";

    public static final String ENTITY_TYPE_BINDING = "type";

    // Adresni misto
    private Integer cisloDomovni; // xsd:integer

    private Integer cisloOrientancni; // xsd:integer

    private String cisloOrientancniPismeno;

    private Boolean forceMissingPismeno = true;

    private Integer psc; // xsd:integer

    // Adresni misto -> Ulice
    private String ulice;

    // Stavební objekt -> Cast obce
    private String castObce;

    // Cast obce, Ulice -> Obec
    private String obec;

    // Obec -> Okres -> VUSC (Kraj)
    private String okres;

    private String vusc;

    // Working data.
    private final Map<Integer, List<StringAddress.Meaning>> meanings = new HashMap<>();

    // Metadata
    private final URI originalStructured;

    private final String originalUnStructured;

    private final List<Report> reports = new LinkedList<>();

    public List<Report> getReports() {
        return reports;
    }

    public RuianEntity(URI postalAddress) {
        this.originalStructured = postalAddress;
        this.originalUnStructured = null;
    }

    public RuianEntity(String addressString) {
        this.originalStructured = null;
        this.originalUnStructured = addressString;
    }

    public RuianEntity(RuianEntity other) {
        this.cisloDomovni = other.cisloDomovni;
        this.cisloOrientancni = other.cisloOrientancni;
        this.psc = other.psc;
        this.ulice = other.ulice;
        this.castObce = other.castObce;
        this.obec = other.obec;
        this.okres = other.okres;
        this.vusc = other.vusc;
        // Working data.
        this.meanings.putAll(other.meanings);
        // Metadata
        this.originalStructured = other.originalStructured;
        this.originalUnStructured = other.originalUnStructured;
        this.reports.addAll(other.reports);
    }

    public Integer getCisloDomovni() {
        return cisloDomovni;
    }

    public void setCisloDomovni(Integer cisloDomovni) {
        this.cisloDomovni = cisloDomovni;
    }

    public Integer getCisloOrientancni() {
        return cisloOrientancni;
    }

    public void setCisloOrientancni(Integer cisloOrientancni) {
        this.cisloOrientancni = cisloOrientancni;
    }

    public String getCisloOrientancniPismeno() {
        return cisloOrientancniPismeno;
    }

    public void setCisloOrientancniPismeno(String cisloOrientancniPismeno) {
        this.cisloOrientancniPismeno = cisloOrientancniPismeno;
    }

    public Boolean getForceMissingPismeno() {
        return forceMissingPismeno;
    }

    public void setForceMissingPismeno(Boolean forceMissingPismeno) {
        this.forceMissingPismeno = forceMissingPismeno;
    }

    public Integer getPsc() {
        return psc;
    }

    public void setPsc(Integer psc) {
        this.psc = psc;
    }

    public String getUlice() {
        return ulice;
    }

    public void setUlice(String ulice) {
        this.ulice = ulice;
    }

    public String getCastObce() {
        return castObce;
    }

    public void setCastObce(String castObce) {
        this.castObce = castObce;
    }

    public String getObec() {
        return obec;
    }

    public void setObec(String obec) {
        this.obec = obec;
    }

    public String getOkres() {
        return okres;
    }

    public void setOkres(String okres) {
        this.okres = okres;
    }

    public Map<Integer, List<StringAddress.Meaning>> getMeanings() {
        return meanings;
    }

    public String getVusc() {
        return vusc;
    }

    public void setVusc(String vusc) {
        this.vusc = vusc;
    }

    /**
     *
     * @return Query that can be asked in RUIAN.
     */
    public String asRuianQuery() {
        // Name of main property to select.
        String minProperty = null;
        String maxProperty = null;

        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        // Adresni misto
        boolean adresniMistoSet = false;
        final StringBuilder sparqlAdresniMisto = new StringBuilder();

        addType(sparqlAdresniMisto, ADRESNI_MISTO, RDF.TYPE, AddressMapperOntology.RUAIN_ADRESNI_MISTO);
        adresniMistoSet |= addProperty(sparqlAdresniMisto, ADRESNI_MISTO,
                AddressMapperOntology.RUAIN_CISLO_DOMOVNI, cisloDomovni);
        adresniMistoSet |= addProperty(sparqlAdresniMisto, ADRESNI_MISTO,
                AddressMapperOntology.RUAIN_CISLO_ORIENTACNI, cisloOrientancni);
        adresniMistoSet |= addProperty(sparqlAdresniMisto, ADRESNI_MISTO,
                AddressMapperOntology.RUAIN_CISLO_ORIENTACNI_PISMENO, cisloOrientancniPismeno);

        // 'PSC' alone can not determine 'AdresniMisto'.
        addProperty(sparqlAdresniMisto, ADRESNI_MISTO,
                AddressMapperOntology.RUAIN_PSC, psc);

        if (adresniMistoSet) {
            if (minProperty == null) {
                minProperty = ADRESNI_MISTO;
            }
            maxProperty = ADRESNI_MISTO;
        }
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        // Ulice
        boolean uliceSet = false;
        final StringBuilder sparqlUlice = new StringBuilder();

        addType(sparqlUlice, ULICE, RDF.TYPE, AddressMapperOntology.RUAIN_ULICE);
        if (ulice != null) {
            uliceSet |= addProperty(sparqlUlice, ULICE,
                    AddressMapperOntology.SCHEMA_NAME, ulice);
        }
        // For linking - add empty part, this can bethen used to link over this entity.
        if (adresniMistoSet) {
            // ADRESNI_MISTO -> ULICE
            addLink(sparqlUlice, ADRESNI_MISTO,
                    AddressMapperOntology.RUAIN_HAS_ULICE, ULICE);
        }

        if (uliceSet) {
            if (minProperty == null) {
                minProperty = ULICE;
            }
            maxProperty = ULICE;
        }
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        // CastObce (Stavebni objekt)
        boolean castObceSet = false;
        final StringBuilder sparqlCastObce = new StringBuilder();

        addType(sparqlCastObce, CAST_OBCE, RDF.TYPE, AddressMapperOntology.RUIAN_CAST_OBCE);
        if (castObce != null) {
            castObceSet |= addProperty(sparqlCastObce, CAST_OBCE,
                    AddressMapperOntology.SCHEMA_NAME, castObce);
        }
        // For linking.
        if (adresniMistoSet) {
            // ADRESNI_MISTO -> STAVEBNI_OBJEKT || ULICE
            addLink(sparqlCastObce, STAVEBNI_OBJEKT,
                    AddressMapperOntology.RUIAN_HAS_CAST_OBCE, CAST_OBCE);
            sparqlCastObce.append("\n");
            addType(sparqlCastObce, STAVEBNI_OBJEKT,
                    RDF.TYPE, AddressMapperOntology.RUIAN_STAVEBNI_OBJEKT);
            addLink(sparqlCastObce, ADRESNI_MISTO,
                    AddressMapperOntology.RUIAN_HAS_STAVEBNI_OBJEKT, STAVEBNI_OBJEKT);
        }

        if (castObceSet) {
            if (minProperty == null) {
                minProperty = CAST_OBCE;
            }
            maxProperty = CAST_OBCE;
        }
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        // Obec
        boolean obecSet = false;
        final StringBuilder sparqlObec = new StringBuilder();

        addType(sparqlObec, OBEC, RDF.TYPE, AddressMapperOntology.RUIAN_OBEC);
        if (obec != null) {
            obecSet |= addProperty(sparqlObec, OBEC,
                    AddressMapperOntology.SCHEMA_NAME, obec);
        }
        // For linking.
        if (castObceSet) {
            // CAST_OBCE -> OBEC
            addLink(sparqlObec, CAST_OBCE,
                    AddressMapperOntology.RUIAN_HAS_OBEC, OBEC);
        }
        if (uliceSet) {
            // ULICE -> OBEC
            addLink(sparqlObec, ULICE,
                    AddressMapperOntology.RUIAN_HAS_OBEC, OBEC);
        }
        if (!castObceSet && !uliceSet && adresniMistoSet) {
            // We use CAST_OBCE and STAVEBNI_OBJEKT as a default connection as it's more likely to exist then
            // 'ulice' if 'ulice' is not provided in address.
            castObceSet = true;
            addLink(sparqlObec, CAST_OBCE,
                    AddressMapperOntology.RUIAN_HAS_OBEC, OBEC);
        }


        if (obecSet) {
            if (minProperty == null) {
                minProperty = OBEC;
            }
            maxProperty = OBEC;
        }
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        // POU
        final StringBuilder sparqlPOU = new StringBuilder();

        addType(sparqlPOU, POU,
                RDF.TYPE, AddressMapperOntology.RUIAN_POU);
        addLink(sparqlPOU, OBEC,
                AddressMapperOntology.RUIAN_HAS_POU, POU);

        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        // ORP
        final StringBuilder sparqlORP = new StringBuilder();

        addType(sparqlORP, ORP,
                RDF.TYPE, AddressMapperOntology.RUIAN_ORP);
        addLink(sparqlORP, POU,
                AddressMapperOntology.RUIAN_HAS_ORP, ORP);

        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        // VUCS
        boolean vucsSet = false;
        final StringBuilder sparqlVUCS = new StringBuilder();

        addType(sparqlVUCS, VUSC, RDF.TYPE, AddressMapperOntology.RUIAN_VUSC);
        if (vusc != null) {
            vucsSet |= addProperty(sparqlVUCS, VUSC,
                    AddressMapperOntology.SCHEMA_NAME, vusc);
        }
        // For linking.
        if (obecSet || uliceSet || castObceSet || adresniMistoSet) {
            // ORP -> VUSC
            addLink(sparqlVUCS, ORP,
                    AddressMapperOntology.RUIAN_HAS_VUSC, VUSC);
        }

        if (vucsSet) {
            if (minProperty == null) {
                minProperty = VUSC;
            }
            maxProperty = VUSC;
        }

        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        // OKRES ( OBEC -> OKRES )
        boolean okresSet = false;
        final StringBuilder sparqlOkres = new StringBuilder();

        if (vucsSet) {
            if (okres != null) {
                // We ignore okres.
                //LOG.warn("Okres ignored for: <{}>, '{}'", originalStructured, originalUnStructured);
            }
        } else {
            if (okres != null) {
                // We can use OKRES.
                okresSet = true;
                addType(sparqlOkres, OKRES, RDF.TYPE, AddressMapperOntology.RUIAN_OKRES);
                addProperty(sparqlOkres, OKRES,
                        AddressMapperOntology.SCHEMA_NAME, okres);
                // okresSet == true
                if (minProperty == null) {
                    minProperty = OKRES;
                }
                maxProperty = OKRES;
            }
        }
        // For linking.
        if (obecSet || uliceSet || castObceSet || adresniMistoSet) {
            addLink(sparqlOkres, OBEC,
                    AddressMapperOntology.RUIAN_HAS_OKRES, OKRES);
        }

        if (minProperty == null || maxProperty == null) {
            LOG.error("No value for entity: <{}> '{}'", this.originalStructured, this.originalUnStructured);
            return "";
        }

        final StringBuilder query = new StringBuilder();
        switch (maxProperty) {
            case VUSC:
            case OKRES:
                if (vucsSet) {
                    query.append("\n");
                    query.append(sparqlVUCS);
                    query.append("\n");
                    query.append(sparqlORP);
                    query.append("\n");
                    query.append(sparqlPOU);
                    if (VUSC.equals(minProperty)) {
                        break;
                    }
                } else if (okresSet) {
                    query.append("\n");
                    query.append(sparqlOkres);
                    if (VUSC.equals(minProperty)) {
                        break;
                    }
                }
            case OBEC:
                query.append("\n");
                query.append(sparqlObec);
                if (OBEC.equals(minProperty)) {
                    break;
                }
            case CAST_OBCE:
            case ULICE:
                // Use both values if possible.
                if (uliceSet) {
                    query.append("\n");
                    query.append(sparqlUlice);
                }
                if (castObceSet) {
                    query.append("\n");
                    query.append(sparqlCastObce);
                }
                if (CAST_OBCE.equals(minProperty) || ULICE.equals(minProperty)) {
                    break;
                }
            case ADRESNI_MISTO:
                query.append("\n");
                query.append(sparqlAdresniMisto);
                // For some ADRESNI_MISTO 'cisloDomovni' may be 'cislo evidencni' this information is stored
                // in 'stavebniObjekt'. We need to omit entities with 'cislo evidencni' from results.
                query.append("\n");
                query.append("  MINUS {\n");
                query.append("    ");
                query.append(ADRESNI_MISTO);
                query.append("<http://ruian.linked.opendata.cz/ontology/stavebniObjekt> ?obj.\n");
                query.append("    ?obj <http://ruian.linked.opendata.cz/ontology/typStavebnihoObjektu> <http://ruian.linked.opendata.cz/ontology/stavebni-objekty/TypStavebnihoObjektu#2>.\n");
                query.append("  }\n");
                //
//                if (cisloOrientancniPismeno == null && forceMissingPismeno) {
//                    query.append("\n");
//                    query.append("  MINUS {\n");
//                    query.append("    ");
//                    query.append(ADRESNI_MISTO);
//                    query.append(" <");
//                    query.append(AddressMapperOntology.RUAIN_CISLO_ORIENTACNI_PISMENO);
//                    query.append("> [].\n");
//                    query.append("  }\n");
//                }
        }
        // Add class.
        query.append("\n");
        query.append("  ");
        query.append(minProperty);
        query.append(" a ?");
        query.append(ENTITY_TYPE_BINDING);
        query.append(" .");
        query.append("\n");

        // Assemble query - we limit number of result to 16
        String queryStr = "SELECT distinct " + minProperty + " WHERE { " + query
                .toString() + "} LIMIT 16";
        // Replace minProperty with ENTITY_BINDING, so we have fixed result structure.
        queryStr = queryStr.replaceAll("\\" + minProperty, "?" + ENTITY_BINDING);
        return queryStr;
    }

    /**
     *
     * @param subject
     * @return This entity as a list of statements together with all metadata stored.
     */
    public List<Statement> asStatements(URI subject) {
        final ValueFactory valueFactory = ValueFactoryImpl.getInstance();
        final EntityBuilder entityBuilder = new EntityBuilder(subject, valueFactory);
        // Add type and reference entity.
        entityBuilder.property(RDF.TYPE, AddressMapperOntology.ENTITY_RUIAN);

        if (originalStructured != null) {
            entityBuilder.property(AddressMapperOntology.HAS_SOURCE_ADDRESS, originalStructured);
        }
        if (originalUnStructured != null) {
            entityBuilder.property(AddressMapperOntology.HAS_SOURCE_ADDRESS, originalUnStructured);
        }

        // Add properties.
        if (this.cisloDomovni != null) {
            entityBuilder.property(AddressMapperOntology.CISLO_DOMOVNI,
                    valueFactory.createLiteral(this.cisloDomovni));
        }
        if (this.cisloOrientancni != null) {
            entityBuilder.property(AddressMapperOntology.CISLO_ORIENTACNI,
                    valueFactory.createLiteral(this.cisloOrientancni));
        }
        if (this.cisloOrientancniPismeno != null) {
            entityBuilder.property(AddressMapperOntology.CISLO_ORIENTACNI_PISMENO,
                    this.cisloOrientancniPismeno);
        }
        if (this.psc != null) {
            entityBuilder.property(AddressMapperOntology.PSC, valueFactory.createLiteral(this.psc));
        }
        if (this.ulice != null) {
            entityBuilder.property(AddressMapperOntology.ULICE_NAME, this.ulice);
        }
        if (this.castObce != null) {
            entityBuilder.property(AddressMapperOntology.CAST_OBECT_NAME, this.castObce);
        }
        if (this.obec != null) {
            entityBuilder.property(AddressMapperOntology.OBEC_NAME, this.obec);
        }
        if (this.okres != null) {
            entityBuilder.property(AddressMapperOntology.OKRES_NAME, this.okres);
        }
        if (this.vusc != null) {
            entityBuilder.property(AddressMapperOntology.VUSC_NAME, this.vusc);
        }
        // Add reports.
        List<Statement> results = new LinkedList<>(); // Uset to store statements from reports.
        for (int index = 0; index < this.reports.size(); ++index) {
            final URI reportSubject = valueFactory.createURI(subject.stringValue() + "/report/" + Integer
                    .toString(index));
            entityBuilder.property(AddressMapperOntology.HAS_REPORT, reportSubject);
            results.addAll(this.reports.get(index).asStatements(reportSubject));
        }
        // Add statements from main entity.
        results.addAll(entityBuilder.asStatements());
        return results;
    }

    private boolean addProperty(StringBuilder sparql, String subject, URI predicate, String value) {
        if (value == null) {
            return false;
        }
        sparql.append("  ");
        sparql.append(subject);
        sparql.append(" <");
        sparql.append(predicate.stringValue());
        sparql.append("> ");
        sparql.append("\"");
        sparql.append(value);
        sparql.append("\"");
        sparql.append(" .\n");
        return true;
    }

    private boolean addProperty(StringBuilder sparql, String subject, URI predicate, Integer value) {
        if (value == null) {
            return false;
        }
        sparql.append("  ");
        sparql.append(subject);
        sparql.append(" <");
        sparql.append(predicate.stringValue());
        sparql.append("> ");
        sparql.append(value);
        sparql.append(" .\n");
        return true;
    }

    private void addLink(StringBuilder sparql, String subject, URI predicate, String value) {
        sparql.append("  ");
        sparql.append(subject);
        sparql.append(" ");
        sparql.append("<");
        sparql.append(predicate.stringValue());
        sparql.append("> ");
        sparql.append(value);
        sparql.append(" .\n");
    }

    private void addType(StringBuilder sparql, String subject, URI predicate, URI value) {
        sparql.append("  ");
        sparql.append(subject);
        sparql.append(" ");
        sparql.append("<");
        sparql.append(predicate.stringValue());
        sparql.append("> <");
        sparql.append(value.stringValue());
        sparql.append(">");
        sparql.append(" .\n");
    }

}
