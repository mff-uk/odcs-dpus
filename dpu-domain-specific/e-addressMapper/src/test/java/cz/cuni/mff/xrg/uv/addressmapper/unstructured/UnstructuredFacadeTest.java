package cz.cuni.mff.xrg.uv.addressmapper.unstructured;

import cz.cuni.mff.xrg.uv.addressmapper.address.unstructured.UnstructuredFacade;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

import cz.cuni.mff.xrg.uv.addressmapper.knowledgebase.KnowledgeBaseException;
import cz.cuni.mff.xrg.uv.addressmapper.knowledgebase.KnowledgeBaseMock;
import cz.cuni.mff.xrg.uv.addressmapper.ruian.RuianEntity;

/**
 *
 * @author Škoda Petr
 */
public class UnstructuredFacadeTest {

    final ValueFactory valueFactory = ValueFactoryImpl.getInstance();

    final UnstructuredFacade facade = new UnstructuredFacade(new KnowledgeBaseMock());

    /**
     * Parse simple address.
     */
    @Test
    public void mapTastCase_00() throws KnowledgeBaseException {
        String input = "Dělnická 12 Adamov České Budějovice";
        
        List<RuianEntity> output = facade.parse(valueFactory.createURI("http:://resource/address"), input);
        Assert.assertEquals(1, output.size());
        final RuianEntity entity = output.get(0);
                
        Assert.assertEquals("České Budějovice", entity.getOkres());
        Assert.assertEquals("Adamov", entity.getObec());
        Assert.assertEquals("Dělnická", entity.getUlice());
        Assert.assertEquals((Integer)12, entity.getCisloDomovni());
    }

    /**
     * In address we use "Praha 8". "Praha" is obec and "Praha 8" is MOMC, MOMC is actually not used
     * so this should test if we can still map this address.
     */
    @Test
    public void mapTastCase_01() throws KnowledgeBaseException {
        String input = "Písečná 2091/12 Praha 8 14200";

        List<RuianEntity> output = facade.parse(valueFactory.createURI("http:://resource/address"), input);
        Assert.assertEquals(1, output.size());
        final RuianEntity entity = output.get(0);

        Assert.assertEquals("Praha", entity.getObec());
        Assert.assertEquals("Písečná", entity.getUlice());

        Assert.assertEquals((Integer)2091, entity.getCisloDomovni());
        Assert.assertEquals((Integer)12, entity.getCisloOrientancni());
        //Assert.assertEquals((Integer)14200, entity.getPsc());
    }

    /**
     * "Olomouc" is okres as well as obec. In this case we expect "Olomouc to be used as a obec.
     */
    @Test
    public void mapTastCase_04() throws KnowledgeBaseException {
        String input = "Olomouc Holšiny 2091/12 14200";

        List<RuianEntity> output = facade.parse(valueFactory.createURI("http:://resource/address"), input);
        Assert.assertEquals(1, output.size());
        final RuianEntity entity = output.get(0);


        Assert.assertEquals("Olomouc", entity.getOkres());
        Assert.assertEquals("Olomouc", entity.getObec());
        Assert.assertEquals("Holšiny", entity.getCastObce());

        Assert.assertEquals((Integer)2091, entity.getCisloDomovni());
        Assert.assertEquals((Integer)12, entity.getCisloOrientancni());
//        Assert.assertEquals((Integer)14200, entity.getPsc());
    }

   /**
     * "Olomouc" is okres as well as obec. In this case we expect "Olomouc to be used as a okres as 
     * the obec is presented in form of "Adamov".
     */
    @Test
    public void mapTastCase_05() throws KnowledgeBaseException {
        String input = "Olomouc Adamov Holšiny 2091/12 14200";

        List<RuianEntity> output = facade.parse(valueFactory.createURI("http:://resource/address"), input);
        Assert.assertEquals(1, output.size());
        final RuianEntity entity = output.get(0);

        Assert.assertEquals("Olomouc", entity.getOkres());
        Assert.assertEquals("Adamov", entity.getObec());
        Assert.assertEquals("Holšiny", entity.getCastObce());

        Assert.assertEquals((Integer)2091, entity.getCisloDomovni());
        Assert.assertEquals((Integer)12, entity.getCisloOrientancni());
//        Assert.assertEquals((Integer)14200, entity.getPsc());
    }

    @Test
    public void mapTestCase_06() throws KnowledgeBaseException {
        String input = "Nuselská 1419/54 Praha 4 - Nusle";

        List<RuianEntity> output = facade.parse(valueFactory.createURI("http:://resource/address"), input);
        Assert.assertEquals(1, output.size());
        final RuianEntity entity = output.get(0);

        Assert.assertEquals("Praha", entity.getObec());
        Assert.assertEquals("Nusle", entity.getCastObce());
        Assert.assertEquals("Nuselská", entity.getUlice());

        Assert.assertEquals((Integer)1419, entity.getCisloDomovni());
        Assert.assertEquals((Integer)54, entity.getCisloOrientancni());
    }

//    @Test
//    public void streetAddress_00() {
//        String input = "466";
//
//        RuianEntity inputEntity = new RuianEntity(valueFactory.createURI("http:://resource/address"));
//        List<RuianEntity> output = facade.mapFromObec(inputEntity, input);
//        Assert.assertEquals(1, output.size());
//        final RuianEntity entity = output.get(0);
//
//        Assert.assertNull(entity.getObec());
//        Assert.assertNull(entity.getCisloOrientancni());
//        Assert.assertEquals((Integer)466, entity.getCisloDomovni());
//        Assert.assertNull(entity.getUlice());
//    }

    @Test
    public void streetAddress_01() throws KnowledgeBaseException {
        String input = "Boží Dar";

        RuianEntity inputEntity = new RuianEntity(valueFactory.createURI("http:://resource/address"));
        List<RuianEntity> output = facade.mapFromObec(inputEntity, input);
        Assert.assertEquals(1, output.size());
        final RuianEntity entity = output.get(0);

        Assert.assertNull(entity.getObec());
        Assert.assertNull(entity.getCisloOrientancni());
        Assert.assertNull(entity.getCisloDomovni());
        Assert.assertEquals("Boží Dar", entity.getUlice());
    }

    @Test
    public void streetAddress_02() throws KnowledgeBaseException {
        String input = "1. května";

        RuianEntity inputEntity = new RuianEntity(valueFactory.createURI("http:://resource/address"));
        List<RuianEntity> output = facade.mapFromObec(inputEntity, input);
        Assert.assertEquals(1, output.size());
        final RuianEntity entity = output.get(0);

        Assert.assertNull(entity.getObec());
        Assert.assertNull(entity.getCisloOrientancni());
        Assert.assertNull(entity.getCisloDomovni());
        Assert.assertEquals("1. května", entity.getUlice());
    }

    @Test
    public void streetAddress_03() throws KnowledgeBaseException {
        String input = "Tejny 621";

        RuianEntity inputEntity = new RuianEntity(valueFactory.createURI("http:://resource/address"));
        List<RuianEntity> output = facade.mapFromObec(inputEntity, input);
        Assert.assertEquals(1, output.size());
        final RuianEntity entity = output.get(0);

        Assert.assertNull(entity.getObec());
        Assert.assertNull(entity.getCisloOrientancni());
        Assert.assertEquals((Integer)621, entity.getCisloDomovni());
        Assert.assertEquals("Tejny", entity.getUlice());
    }

    @Test
    public void streetAddress_04() throws KnowledgeBaseException {
        String input = "T. Bati 1541";

        RuianEntity inputEntity = new RuianEntity(valueFactory.createURI("http:://resource/address"));
        List<RuianEntity> output = facade.mapFromObec(inputEntity, input);
        Assert.assertEquals(1, output.size());
        final RuianEntity entity = output.get(0);

        Assert.assertNull(entity.getCisloOrientancni());
        Assert.assertEquals((Integer)1541, entity.getCisloDomovni());
        Assert.assertEquals("T. Bati", entity.getUlice());
    }

    @Test
    public void streetAddress_05() throws KnowledgeBaseException {
        String input = "28. října 2771/11";

        RuianEntity inputEntity = new RuianEntity(valueFactory.createURI("http:://resource/address"));
        List<RuianEntity> output = facade.mapFromObec(inputEntity, input);
        Assert.assertEquals(1, output.size());
        final RuianEntity entity = output.get(0);

        Assert.assertNull(entity.getObec());
        Assert.assertEquals((Integer)11, entity.getCisloOrientancni());
        Assert.assertEquals((Integer)2771, entity.getCisloDomovni());
        Assert.assertEquals("28. října", entity.getUlice());
    }

    @Test
    public void streetAddress_06() throws KnowledgeBaseException {
        String input = "Kubelíkova 604 / 73";

        RuianEntity inputEntity = new RuianEntity(valueFactory.createURI("http:://resource/address"));
        List<RuianEntity> output = facade.mapFromObec(inputEntity, input);
        Assert.assertEquals(1, output.size());
        final RuianEntity entity = output.get(0);

        Assert.assertNull(entity.getObec());
        Assert.assertEquals((Integer)73, entity.getCisloOrientancni());
        Assert.assertEquals((Integer)604, entity.getCisloDomovni());
        Assert.assertEquals("Kubelíkova", entity.getUlice());
    }

    @Test
    public void streetAddress_07() throws KnowledgeBaseException {
        String input = "Svornosti 3199/19a";

        RuianEntity inputEntity = new RuianEntity(valueFactory.createURI("http:://resource/address"));
        List<RuianEntity> output = facade.mapFromObec(inputEntity, input);
        Assert.assertEquals(1, output.size());
        final RuianEntity entity = output.get(0);

        Assert.assertNull(entity.getObec());
        Assert.assertEquals((Integer)19, entity.getCisloOrientancni());
        Assert.assertEquals((Integer)3199, entity.getCisloDomovni());
        Assert.assertEquals("Svornosti", entity.getUlice());
    }

//    public void streetAddress_08() {
//        String input = "Vernéřov, POBox 10 188";
//    }

    @Test
    public void streetAddress_09() throws KnowledgeBaseException {
        String input = "J. M. Marků /92";

        RuianEntity inputEntity = new RuianEntity(valueFactory.createURI("http:://resource/address"));
        List<RuianEntity> output = facade.mapFromObec(inputEntity, input);
        Assert.assertEquals(1, output.size());
        final RuianEntity entity = output.get(0);

        Assert.assertNull(entity.getObec());
        Assert.assertEquals((Integer)92, entity.getCisloOrientancni());
        Assert.assertNull(entity.getCisloDomovni());
        Assert.assertEquals("J. M. Marků", entity.getUlice());
    }

    @Test
    public void streetAddress_10() throws KnowledgeBaseException {
        String input = "Náměstí 5. května 19";

        RuianEntity inputEntity = new RuianEntity(valueFactory.createURI("http:://resource/address"));
        List<RuianEntity> output = facade.mapFromObec(inputEntity, input);
        Assert.assertEquals(1, output.size());
        final RuianEntity entity = output.get(0);

        Assert.assertNull(entity.getObec());
        Assert.assertNull(entity.getCisloOrientancni());
        Assert.assertEquals((Integer)19, entity.getCisloDomovni());
        Assert.assertEquals("Náměstí 5. května", entity.getUlice());
    }

    public void streetAddress_11() {
//        String input = "nám. Přemysla Otakara II. 1/1,2";
//
//        RuianEntity inputEntity = new RuianEntity(valueFactory.createURI("http:://resource/address"));
//        List<RuianEntity> output = facade.mapFromObec(inputEntity, input);
//        Assert.assertEquals(1, output.size());
//        final RuianEntity entity = output.get(0);
//
//        Assert.assertNull(entity.getObec());
//        Assert.assertEquals("1,2", entity.getCisloOrientancni());
//        Assert.assertEquals("1", entity.getCisloDomovni());
//        Assert.assertEquals("nám. Přemysla Otakara II.", entity.getUlice());
    }

    @Test
    public void streetAddress_12() throws KnowledgeBaseException {
        String input = "Jiřího z Poděbrad 2725/21 2725/21";

        RuianEntity inputEntity = new RuianEntity(valueFactory.createURI("http:://resource/address"));
        List<RuianEntity> output = facade.mapFromObec(inputEntity, input);
        Assert.assertEquals(1, output.size());
        final RuianEntity entity = output.get(0);

        Assert.assertNull(entity.getObec());
        Assert.assertEquals((Integer)21, entity.getCisloOrientancni());
        Assert.assertEquals((Integer)2725, entity.getCisloDomovni());
        Assert.assertEquals("Jiřího z Poděbrad", entity.getUlice());
    }

//    public void streetAddress_13() {
//        String input = "435 52";
//
//        RuianEntity inputEntity = new RuianEntity(valueFactory.createURI("http:://resource/address"));
//        List<RuianEntity> output = facade.mapFromObec(inputEntity, input);
//        Assert.assertEquals(1, output.size());
//        final RuianEntity entity = output.get(0);
//
//        Assert.assertNull(entity.getObec());
//        Assert.assertEquals((Integer)52, entity.getCisloOrientancni());
//        Assert.assertEquals((Integer)435, entity.getCisloDomovni());
//        Assert.assertNull(entity.getUlice());
//    }

    @Test
    public void streetAddress_14() throws KnowledgeBaseException {
        String input = "Olomoucká 90 (Olympia)";

        RuianEntity inputEntity = new RuianEntity(valueFactory.createURI("http:://resource/address"));
        List<RuianEntity> output = facade.mapFromObec(inputEntity, input);
        Assert.assertEquals(1, output.size());
        final RuianEntity entity = output.get(0);

        Assert.assertNull(entity.getObec());
        Assert.assertNull(entity.getCisloOrientancni());
        Assert.assertEquals((Integer)90, entity.getCisloDomovni());
        Assert.assertEquals("Olomoucká", entity.getUlice());
    }

    @Test
    public void streetAddress_15() throws KnowledgeBaseException {
        String input = "Petrovice - zámek 26";

        RuianEntity inputEntity = new RuianEntity(valueFactory.createURI("http:://resource/address"));
        List<RuianEntity> output = facade.mapFromObec(inputEntity, input);
        Assert.assertEquals(1, output.size());
        final RuianEntity entity = output.get(0);

        Assert.assertEquals("Petrovice", entity.getObec());
        Assert.assertNull(entity.getCisloOrientancni());
        Assert.assertEquals((Integer)26, entity.getCisloDomovni());
        Assert.assertEquals("zámek", entity.getUlice());
    }

    @Test
    public void streetAddress_16() throws KnowledgeBaseException {
        String input = "Vrchlického (pošt.přihr.35) 1009/6";

        RuianEntity inputEntity = new RuianEntity(valueFactory.createURI("http:://resource/address"));
        List<RuianEntity> output = facade.mapFromObec(inputEntity, input);
        Assert.assertEquals(1, output.size());
        final RuianEntity entity = output.get(0);

        Assert.assertNull(entity.getObec());
        Assert.assertEquals((Integer)6, entity.getCisloOrientancni());
        Assert.assertEquals((Integer)1009, entity.getCisloDomovni());
        Assert.assertEquals("Vrchlického", entity.getUlice());
    }

    @Test
    public void streetAddress_17() throws KnowledgeBaseException {
        String input = "Křižíkova 23/48b";

        RuianEntity inputEntity = new RuianEntity(valueFactory.createURI("http:://resource/address"));
        List<RuianEntity> output = facade.mapFromObec(inputEntity, input);
        Assert.assertEquals(1, output.size());
        final RuianEntity entity = output.get(0);


        Assert.assertNull(entity.getObec());
        Assert.assertEquals((Integer)48, entity.getCisloOrientancni());
        Assert.assertEquals((Integer)23, entity.getCisloDomovni());
        Assert.assertEquals("Křižíkova", entity.getUlice());
    }

    /**
     * Use to print information about entity.
     *
     * @param entity
     */
    private void printEntity(RuianEntity entity) {
        List<Statement> statements = entity.asStatements(
                valueFactory.createURI("http://localhost/resource/entity"));

        for (Statement s : statements) {
            System.out.println(" <" + s.getSubject() + "> <" + s.getPredicate() + "> " + s.getObject());
        }
        System.out.println();
    }

}
