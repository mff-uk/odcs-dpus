package cz.cuni.mff.xrg.uv.addressmapper.knowledgebase;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Simple mock for testing.
 *
 * @author Škoda Petr
 */
public class KnowledgeBaseMock extends KnowledgeBase {

    private final List<String> okresList = Arrays.asList("Olomouc", "Tábor", "České Budějovice");
    
    private final List<String> vuscList = Arrays.asList("Olomoucký kraj", "Jihočeský kraj");
    
    private final List<String> obecList = Arrays.asList("Olomouc", "Adamov", "Praha", "Petrovice");
    
    private final List<String> castObceList = Arrays.asList("Holšiny", "Nusle");
    
    private final List<String> uliceList = Arrays.asList("Malostranaské náměstí", "Stejskalova", "Dělnická",
            "Písečná", "Nuselská", "Boží Dar", "1. května", "Tejny", "T. Bati", "28. října", "Kubelíkova",
            "Svornosti", "Vernéřov", "J. M. Marků", "Náměstí 5. května", "nám. Přemysla Otakara II.",
            "Jiřího z Poděbrad", "Olomoucká", "Vrchlického", "zámek", "Křižíkova");

    @Override
    public List<String> getOkres(String okres) {
        if (okresList.contains(okres)) {
            return Arrays.asList(okres);
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    @Override
    public List<String> getVusc(String vusc) {
        if (vuscList.contains(vusc)) {
            return Arrays.asList(vusc);
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    @Override
    public List<String> getObec(String obec) {
        if (obecList.contains(obec)) {
            return Arrays.asList(obec);
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    @Override
    public List<String> getObecInOkres(String obec, String okres) {
        if (obecList.contains(obec)) {
            return Arrays.asList(obec);
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    @Override
    public List<String> getObecInVusc(String obec, String vusc) {
        if (obecList.contains(obec)) {
            return Arrays.asList(obec);
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    @Override
    public List<String> getUliceInObec(String ulice, String obec) {
        if (uliceList.contains(ulice)) {
            return Arrays.asList(ulice);
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    @Override
    public List<String> getCastObceInObec(String castObce, String obec) {
        if (castObceList.contains(castObce)) {
            return Arrays.asList(castObce);
        } else {
            return Collections.EMPTY_LIST;
        }
    }

}
