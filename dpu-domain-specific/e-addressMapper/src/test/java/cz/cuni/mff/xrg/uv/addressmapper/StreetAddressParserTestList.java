package cz.cuni.mff.xrg.uv.addressmapper;

import cz.cuni.mff.xrg.uv.addressmapper.streetAddress.StreetAddress;
import cz.cuni.mff.xrg.uv.addressmapper.streetAddress.StreetAddressParser;
import cz.cuni.mff.xrg.uv.addressmapper.streetAddress.WrongAddressFormatException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Škoda Petr
 */
public class StreetAddressParserTestList {

    private static final Logger LOG = LoggerFactory.getLogger(
            StreetAddressParserTestList.class);

    private final Set<String> ulice = new HashSet<>();

    private final Set<String> ulice_low = new HashSet<>();

    private final Set<String> obce = new HashSet<>();

    private final StreetAddressParser parser = new StreetAddressParser();

    public StreetAddressParserTestList() {
        // load cache
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream("d:/Temp/02/ruian-ulice.txt"),
                        "UTF-8"))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        ulice.add(line);
                        ulice_low.add(line.toLowerCase());
                    }
                } catch (IOException e) {
                    LOG.error("Failed to load address cache.", e);
                }

                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(
                                new FileInputStream("d:/Temp/02/ruian-obce.txt"),
                                "UTF-8"))) {
                            String line;
                            while ((line = br.readLine()) != null) {
                                obce.add(line);
                            }
                        } catch (IOException e) {
                            LOG.error("Failed to load address cache.", e);
                        }

    }

    //@Test
    public void listTest() throws WrongAddressFormatException {
        int total = 0;
        int ok = 0;
        int ok_low = 0;
        int ok_obec = 0;
        int failed = 0;
        int unknownName = 0;
        int nullName = 0;
        String fileName = 
//                "streetAddress-ares"
                //"streetAddress-cenia.cz"
                "streetAddress-coi.cz"
                //"streetAddress-mzp.cz"
                //"streetAddress-seznam.gov.cz"                
                ;
        
        LOG.info("Start...");
        
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(
                                "d:/Temp/02/" + fileName + ".txt"
                        ), "UTF-8"))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        total++;
                        StreetAddress streetAddress;

                        try {
                            streetAddress = parser.parse(line);
                        } catch (WrongAddressFormatException e) {
                            failed++;
//                    LOG.info("{} -> ?\t{}", line, e.getMessage());
                            //System.out.println("PARSER_FALED: " + line + " -> ?\t" + e.getMessage());
                            continue;
                        }

                        if (streetAddress.getStreetName() == null) {
                            //System.out.println("> " + line);
                            nullName++;
                            continue;
                        }

                        if (ulice.contains(streetAddress.getStreetName())) {
                            // ok
                            ok++;
                        } else if (ulice_low.contains(streetAddress.getStreetName().toLowerCase())) {
                            // ok, for case insensitive
                            ok_low++;
                        } else if (obce.contains(streetAddress.getStreetName())) {
                            ok_obec++;
                        } else {
                            unknownName++;
//                            LOG.info("{} -> {}", line, streetAddress.getStreetName());
                            //System.out.println(line + " -> " + streetAddress.getStreetName());
                        }
                    }
                } catch (IOException e) {
                    LOG.error("Failed to load address list.", e);
                }
                LOG.info("------ {}", fileName);
                LOG.info("Total: {}", total);
                LOG.info("Ok: {}", ok);
                LOG.info("Ok for case insensitive compare: {}", ok_low);
                LOG.info("Ok as 'obce': {}", ok_obec);
                LOG.info("Without name: {}", nullName);
                LOG.info("Uknown name: {}", unknownName);
                LOG.info("Failed: {} ({})", failed, failed + unknownName);
    }

}
