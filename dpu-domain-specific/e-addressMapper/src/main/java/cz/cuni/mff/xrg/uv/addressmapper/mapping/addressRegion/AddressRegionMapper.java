package cz.cuni.mff.xrg.uv.addressmapper.mapping.addressRegion;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.cuni.mff.xrg.uv.addressmapper.AddressMapperOntology;
import cz.cuni.mff.xrg.uv.addressmapper.mapping.AbstractMapper;
import cz.cuni.mff.xrg.uv.addressmapper.objects.PostalAddress;
import cz.cuni.mff.xrg.uv.addressmapper.objects.Report;
import cz.cuni.mff.xrg.uv.addressmapper.objects.ReportSubstitution;
import cz.cuni.mff.xrg.uv.addressmapper.objects.RuianEntity;
import eu.unifiedviews.dpu.DPUException;

/**
 * Set: vusc
 *
 * @author Škoda Petr
 */
public class AddressRegionMapper extends AbstractMapper {

    private static final String[] VUSC_VALUES = {
        "Kraj Vysočina",
        "Jihomoravský kraj",
        "Olomoucký kraj",
        "Moravskoslezský kraj",
        "Zlínský kraj",
        "Hlavní město Praha",
        "Středočeský kraj",
        "Jihočeský kraj",
        "Plzeňský kraj",
        "Karlovarský kraj",
        "Ústecký kraj",
        "Liberecký kraj",
        "Královéhradecký kraj",
        "Pardubický kraj"
    };

    private static final String[] OKRES_VALUES = {
        "Zlín",
        "Kroměříž",
        "Jičín",
        "Znojmo",
        "Praha-východ",
        "Mladá Boleslav",
        "Plzeň-jih",
        "Pardubice",
        "Prostějov",
        "Brno-venkov",
        "Písek",
        "Rakovník",
        "Žďár nad Sázavou",
        "Přerov",
        "Beroun",
        "Strakonice",
        "Příbram",
        "Ústí nad Orlicí",
        "Šumperk",
        "Tachov",
        "Opava",
        "Tábor",
        "Louny",
        "Ostrava-město",
        "Kolín",
        "Liberec",
        "Ústí nad Labem",
        "Bruntál",
        "Nový Jičín",
        "Vsetín",
        "Olomouc",
        "Chrudim",
        "Náchod",
        "Svitavy",
        "Klatovy",
        "Jablonec nad Nisou",
        "Trutnov",
        "Kutná Hora",
        "Vyškov",
        "Nymburk",
        "Karlovy Vary",
        "Jihlava",
        "Třebíč",
        "Benešov",
        "Hradec Králové",
        "Český Krumlov",
        "Cheb",
        "Semily",
        "Rokycany",
        "Frýdek-Místek",
        "Kladno",
        "Mělník",
        "Prachatice",
        "Břeclav",
        "Litoměřice",
        "Praha-západ",
        "Blansko",
        "České Budějovice",
        "Děčín",
        "Havlíčkův Brod",
        "Domažlice",
        "Sokolov",
        "Česká Lípa",
        "Jeseník",
        "Jindřichův Hradec",
        "Pelhřimov",
        "Chomutov",
        "Most",
        "Plzeň-sever",
        "Plzeň-město",
        "Rychnov nad Kněžnou",
        "Uherské Hradiště",
        "Teplice",
        "Karviná",
        "Hlavní město Praha",
        "Brno-město",
        "Hodonín"
    };

    private static final Map<String, String> VUSC_MAP;

    private static final Map<String, String> OKRES_MAP;

    static {
        VUSC_MAP = new HashMap<>();
        for (String item : VUSC_VALUES) {
            VUSC_MAP.put(item.toLowerCase(), item);
        }
        // - - - - -
        OKRES_MAP = new HashMap<>();
        for (String item : OKRES_VALUES) {
            OKRES_MAP.put(item.toLowerCase(), item);
        }
    }

    @Override
    public List<RuianEntity> map(PostalAddress address, RuianEntity entity) throws DPUException {
        final RuianEntity outputEntity = new RuianEntity(entity);

        if (address.getAddressRegion() == null) {
            return Arrays.asList(outputEntity);
        }

        if (VUSC_MAP.values().contains(address.getAddressRegion())) {
            outputEntity.setVusc(address.getAddressRegion());
        } else if (OKRES_MAP.values().contains(address.getAddressRegion())) {
            outputEntity.setOkres(address.getAddressRegion());
        } else {
            final String addressRegion = address.getAddressRegion().toLowerCase();
            if (VUSC_MAP.containsKey(addressRegion)) {
                final String addressRegionNew = VUSC_MAP.get(addressRegion);
                outputEntity.setVusc(addressRegionNew);
                // Create report about substitution.
                Report report = new ReportSubstitution(AddressMapperOntology.MAPPER_ADDRESS_REGION,
                        addressRegion, addressRegionNew);
                outputEntity.getReports().add(report);
            } else if (OKRES_MAP.containsKey(addressRegion)) {
                final String addressRegionNew = OKRES_MAP.get(addressRegion);
                outputEntity.setOkres(addressRegionNew);
                // Create report about substitution.
                Report report = new ReportSubstitution(AddressMapperOntology.MAPPER_ADDRESS_REGION,
                        addressRegion, addressRegionNew);
                outputEntity.getReports().add(report);
            } else {
                // Uknown value.
                Report report = new Report(
                        AddressMapperOntology.MAPPER_ADDRESS_REGION,
                        String.format("Neplatný AddressRegion '%s'",
                                address.getAddressRegion()));
                outputEntity.getReports().add(report);
            }
        }
        return Arrays.asList(outputEntity);
    }

}
