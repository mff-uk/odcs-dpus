package cz.cuni.mff.xrg.uv.addressmapper.address.unstructured;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.uv.addressmapper.AddressMapperOntology;
import cz.cuni.mff.xrg.uv.addressmapper.knowledgebase.KnowledgeBase;
import cz.cuni.mff.xrg.uv.addressmapper.objects.Report;
import cz.cuni.mff.xrg.uv.addressmapper.address.StringAddress;
import cz.cuni.mff.xrg.uv.addressmapper.knowledgebase.KnowledgeBaseException;
import cz.cuni.mff.xrg.uv.addressmapper.ruian.RuianEntity;
import cz.cuni.mff.xrg.uv.addressmapper.utils.Utils;

/**
 *
 * @author Škoda Petr
 */
public class UnstructuredFacade {

    private static final Logger LOG = LoggerFactory.getLogger(UnstructuredFacade.class);

    private final KnowledgeBase knowledgeBase;
    
    public UnstructuredFacade(KnowledgeBase knowledgeBase) {
        this.knowledgeBase = knowledgeBase;
    }

    /**
     * Parse given unstructured address to entity of given source URI.
     *
     * @param uri
     * @param address
     * @return
     */
    public List<RuianEntity> parse(URI uri, String address) throws KnowledgeBaseException {
        return map(new RuianEntity(uri), address);
    }

    /**
     * Try to get 'okres' or 'vusc' and then continue mapping in this regions. If not 'okres' or 'vusc' is not
     * found, then we try to map without them.
     *
     * @param inputEntity     This can be used only to construct other entities!
     * @param addressAsString
     * @return
     */
    public List<RuianEntity> map(RuianEntity inputEntity, String addressAsString) throws KnowledgeBaseException {
        // Prepare working data in entity.
        final StringAddress address = new StringAddress(addressAsString);
        //
        final Set<RuianEntity> start = new HashSet<>();
        start.add(inputEntity);
        // VUSC
        final Set<RuianEntity> vusc = new HashSet<>();
        for (RuianEntity entity : start) {
            vusc.addAll(fillVusc(entity, address));
        }
        // Okres - there is depedency on VUSC, but we ignore that as both of these values are used a top 
        // region. It's more a question of habbit in specifing only one of those values.
        final Set<RuianEntity> okres = new HashSet<>();
        for (RuianEntity entity : vusc) {
            okres.addAll(fillOkres(entity, address));
        }
        // Map the rest.
        final Set<RuianEntity> output = new HashSet<>();
        for (RuianEntity entity : okres) {
            output.addAll(mapFromObec(entity, addressAsString));
        }        
        return Arrays.asList(output.toArray(new RuianEntity[0]));
    }

    /**
     * Like {@link #map(cz.cuni.mff.xrg.uv.addressmapper.ruian.RuianEntity, java.lang.String)} but start
     * mapping from 'obec'.
     * 
     * @param inputEntity
     * @param addressAsString
     * @return
     */
    public List<RuianEntity> mapFromObec(RuianEntity inputEntity, String addressAsString) throws KnowledgeBaseException {
        // Prepare working data in entity.
        final StringAddress address = new StringAddress(addressAsString);
        //
        final Set<RuianEntity> start = new HashSet<>();
        start.add(inputEntity);
        // Obec
        final Set<RuianEntity> obec = new HashSet<>();
        for (RuianEntity entity : start) {
            obec.addAll(fillObec(entity, address));
        }
        // Ulice
        final Set<RuianEntity> ulice = new HashSet<>();
        for (RuianEntity entity : obec) {
            ulice.addAll(fillUlice(entity, address));
        }
        // Cast obce
        final Set<RuianEntity> castObce = new HashSet<>();
        for (RuianEntity entity : ulice) {
            castObce.addAll(fillCastObce(entity, address));
        }
        // Cislo orientacni a popisen
        final Set<RuianEntity> output = new HashSet<>();
        for (RuianEntity entity : castObce) {
            output.addAll(fillCisla(entity, address));
        }
        // Return as list.
        return Arrays.asList(output.toArray(new RuianEntity[0]));
    }

    /**
     * Try to set "vusc" to given entity.
     *
     * @param entity  Source entity, can be consumed.
     * @param address
     * @return
     */
    public Set<RuianEntity> fillVusc(RuianEntity entity, StringAddress address) throws KnowledgeBaseException {
        final Set<RuianEntity> output = new HashSet<>();
        for (StringAddress.Token token : address) {
            for (String vusc : knowledgeBase.getVusc(token.toString())) {
                // Create copy and set new value.
                final RuianEntity newEntity = new RuianEntity(entity);
                token.addMeaning(newEntity, StringAddress.Meaning.VUCS, vusc);
                output.add(newEntity);
            }
        }
        // We may not have any mapping, but there mith be some later, so pass the input.
        if (output.isEmpty()) {
            output.add(entity);
        }
        return output;
    }

    /**
     * Try to set "okres" to given entity.
     *
     * @param entity  Source entity, can be consumed.
     * @param address
     * @return
     */
    public Set<RuianEntity> fillOkres(RuianEntity entity, StringAddress address) throws KnowledgeBaseException {
        final Set<RuianEntity> output = new HashSet<>();
        for (StringAddress.Token token : address) {
            // For each mapped value.
            for (String okres : knowledgeBase.getOkres(token.toString())) {
                // Create copy and set new value.
                final RuianEntity newEntity = new RuianEntity(entity);
                token.addMeaning(newEntity, StringAddress.Meaning.OKRES, okres);
                output.add(newEntity);
            }
        }
        // We may not have any mapping, but there mith be some later, so pass the input.
        if (output.isEmpty()) {
            output.add(entity);
        }
        return output;
    }

    /**
     *
     * @param entity  Source entity, can be consumed.
     * @param address
     * @return
     */
    public Set<RuianEntity> fillObec(RuianEntity entity, StringAddress address) throws KnowledgeBaseException {
        final Set<RuianEntity> output = new HashSet<>();
        final Set<RuianEntity> outputAlternatives = new HashSet<>();
        // Used to store used 'obce' to not use
        final Set<String> usedObce = new HashSet<>();
        for (StringAddress.Token token : address) {
            final String tokenStr = token.toString();
            // For each mapped value.
            final List<String> obceInOkres = knowledgeBase.getObecInOkres(tokenStr, entity.getOkres());
            final List<String> obceInVusc = knowledgeBase.getObecInVusc(tokenStr, entity.getVusc());
            // If entity.okres and entity.vusc are null, then same value is returned.
            final List<String> obce;
            // If not empty them ve need intersection of this values.
            if (!obceInOkres.isEmpty() && !obceInVusc.isEmpty()) {
                // We hava data in both - do the intersection.
                obce = new LinkedList<>();
                for (String obecFromOkres : obceInOkres) {
                    if (obceInVusc.contains(obecFromOkres)) {
                        obce.add(obecFromOkres);
                    }
                }
                // Check for non empty intersection.
                if (obce.isEmpty()) {
                    // TODO Log some message here?
                    //  This migth not be the only way to go, so we it could be a false alarm.
                    LOG.warn("Empty intersection of non empty 'obec' has been ignored.");
                    continue;
                }
            } else if (!obceInOkres.isEmpty()) {
                obce = obceInOkres;
            } else if (!obceInVusc.isEmpty()) {
                obce = obceInVusc;
            } else {
                // Both are empty, try to use 'obec' directly.
                obce = knowledgeBase.getObec(tokenStr);
                // Add report if both values were not empty.
                if (entity.getOkres() != null && entity.getVusc() != null) {
                    final Report report = new Report(AddressMapperOntology.UNSTRUCTURED_FACADE,
                            "Skipping 'okres' and 'vusc'.");
                    entity.getReports().add(report);
                }
            }
            // Set retrieved values.
//            LOG.info("obec: {}", obce.size());

            // In case of exact match, we return imediately. As we go from longer tokens
            // to shorter this should be ok.
            if (obce.contains(tokenStr)) {
                // We have an exact match!
                final RuianEntity newEntity = new RuianEntity(entity);
                token.addMeaning(newEntity, StringAddress.Meaning.OBEC, tokenStr);
                //
                output.clear();
                output.add(newEntity);
                return output;
            }

            for (String obec : obce) {
                // Test if given values has not been used alredy.
                if (usedObce.contains(obec)) {
                    // Already used.
                    continue;
                } else {
                    usedObce.add(obec);
                }
                // Create copy and set new value.
                final RuianEntity newEntity = new RuianEntity(entity);
                token.addMeaning(newEntity, StringAddress.Meaning.OBEC, obec);
                // In some cases obec can be same as okres.
                if (newEntity.getObec().equals(entity.getOkres())) {
                    // Same values, add as an alternative, we will used this only if there is no other option.
                    outputAlternatives.add(newEntity);
                } else {
                    output.add(newEntity);
                }
            }
        }

        // We may not have any mapping, but there mith be some later, so pass the input.
        if (output.isEmpty()) {
            if (outputAlternatives.isEmpty()) {
                output.add(entity);
            } else {
                // Add alternatives.
                output.addAll(outputAlternatives);
            }
        }
        return output;
    }

    /**
     *
     * @param entity  Source entity, can be consumed.
     * @param address
     * @return
     */
    public Set<RuianEntity> fillUlice(RuianEntity entity, StringAddress address) throws KnowledgeBaseException {
        final Set<RuianEntity> output = new HashSet<>();
        for (StringAddress.Token token : address) {
            // For each mapped value.
            for (String ulice : knowledgeBase.getUliceInObec(token.toString(), entity.getObec())) {
                // Create copy and set new value.
                final RuianEntity newEntity = new RuianEntity(entity);
                token.addMeaning(newEntity, StringAddress.Meaning.ULICE, ulice);
                output.add(newEntity);
            }
        }
        // We may not have any mapping, but there mith be some later, so pass the input.
        if (output.isEmpty()) {
            output.add(entity);
        }
        return output;
    }

    /**
     *
     * @param entity  Source entity, can be consumed.
     * @param address
     * @return
     */
    public Set<RuianEntity> fillCastObce(RuianEntity entity, StringAddress address) throws KnowledgeBaseException {
        final Set<RuianEntity> output = new HashSet<>();
        for (StringAddress.Token token : address) {
            // For each mapped value.
            for (String castObce : knowledgeBase.getCastObceInObec(token.toString(), entity.getObec())) {
                // Create copy and set new value.
                final RuianEntity newEntity = new RuianEntity(entity);
                token.addMeaning(newEntity, StringAddress.Meaning.CASTOBCE, castObce);
                output.add(newEntity);
            }
        }
        // We may not have any mapping, but there mith be some later, so pass the input.
        if (output.isEmpty()) {
            output.add(entity);
        }
        return output;
    }

    /**
     * Fill "cislo popisne", "cislo orientacni" and "cislo orientacni pismeno".
     *
     * @param entity Source entity, can be consumed.
     * @param address
     * @return
     */
    public Set<RuianEntity> fillCisla(RuianEntity entity, StringAddress address) {
        final Set<RuianEntity> output = new HashSet<>();
        // We need to find first number after 'ulice', 'cast obce'.
        final List<String> parts;
        if (entity.getUlice() != null) {
            parts = cleanValue(entity, address, StringAddress.Meaning.ULICE);
        } else if (entity.getCastObce() != null) {
            parts = cleanValue(entity, address, StringAddress.Meaning.CASTOBCE);
        } else {
            // Special case, it migth contains just numbers, but for now we quit.
            final Report report = new Report(AddressMapperOntology.UNSTRUCTURED_FACADE,
                "No 'ulice' or 'cast obce' found - can't read 'cislo popisne' not 'cislo orientacni'");
            entity.getReports().add(report);
            output.add(entity);
            return output;
        }
        // Start with finding 'ulice',
//        LOG.info("Used value: {}", Utils.join(parts, " "));

        Integer cisloDomovni = null;
        Integer cisloOrientaceni = null;
        String cisloOrietaceniPismeno = null;
        
        // Try to parse given string.

        for (String item : parts) {
            // Look for separator.
            if ("/".equals(item)) {
                // Just separator.
            } else if (item.contains("/")) {
                String tempLetter = null;
                // Remove last letter character if it's presetned.
                if (Character.isLetter(item.charAt(item.length() -1))) {
                    tempLetter = item.substring(item.length() -1);
                    item = item.substring(0, item.length() - 1);
                }                
                // Two values in a single part.
                final String[] itemSplitted = item.split("/");
                
                // TODO Check if values are not already set!
                try {
                    if (!itemSplitted[0].isEmpty()) {
                        cisloDomovni = Integer.parseInt(itemSplitted[0]);
                    }
                    if (!itemSplitted[1].isEmpty()) {
                        cisloOrientaceni = Integer.parseInt(itemSplitted[1]);
                        cisloOrietaceniPismeno = tempLetter;
                    }
                } catch (NumberFormatException ex) {
                    // Can't parse - ignore.
                }
                if (cisloOrientaceni != null) {
                    // Cislo orietaceni is the second value. If it's set, then end.
                    break;
                }
            } else {
                // Try to parse as integer.
                String tempLetter = null;
                // Remove last letter character if it's presetned.
                if (Character.isLetter(item.charAt(item.length() -1))) {
                    tempLetter = item.substring(item.length() -1);
                    item = item.substring(0, item.length() - 1);
                }
                Integer value;
                try {
                    value = Integer.parseInt(item);
                    // It's number.
                    if (cisloDomovni == null) {
                        if (tempLetter == null) {
                            // First number is 'CisloPopisne'.
                            cisloDomovni = value;
                        } else {
                            // We have first value with letter, this is probably 'CisloOrientaceni'.
                            cisloOrientaceni = value;
                            cisloOrietaceniPismeno = tempLetter;
                            // In that case end iteration.
                            break;
                        }
                    } else {
                        // It's 'CisloOrientaceni'.
                        cisloOrientaceni = value;
                        cisloOrietaceniPismeno = tempLetter;
                        // Both values are set.
                        break;
                    }                    
                } catch (NumberFormatException ex) {
                    // It's not a number - ignore this element.
                }
            }
        }

        //  Set values to entity.
        entity.setCisloDomovni(cisloDomovni);
        entity.setCisloOrientancni(cisloOrientaceni);
        entity.setCisloOrientancniPismeno(cisloOrietaceniPismeno);

        LOG.info("CisloDomovni: '{}'   CisloOrietancni: '{}'   Pismeno: '{}'", cisloDomovni, cisloOrientaceni,
                cisloOrietaceniPismeno);

        output.add(entity);
        return output;
    }

    /**
     * Sample:
     * If ULICE is given as meaning and address has structure: ULICE ULICE NOMEANING NOMEANING OBEC
     * Then parts representing NOMEANING NOMEANING are returned.
     *
     * TODO Should also do some cleaning, like omitting duplicities or removing values in braces.
     *
     * @param entity
     * @param address
     * @param meaning
     * @return List of parts with no meaning assigned. Parts must follow after other parts with given meaning.
     */
    public List<String> cleanValue(RuianEntity entity, StringAddress address, StringAddress.Meaning meaning) {
        int index = -1;
        for (int i = 0; i < address.getParts().length; ++i) {
            if (entity.getMeanings().containsKey(i)) {
                if (entity.getMeanings().get(i).contains(meaning)) {
                    index = i;
                    break;
                }
            }
        }
        if (index == -1) {            
            final Report report = new Report(AddressMapperOntology.UNSTRUCTURED_FACADE,
                            "ERROR: This should not happen!");
            entity.getReports().add(report);
            return new LinkedList<>();
        }
        // Go to first non used part.
        boolean notEmpty = false;
        for (int i = index; i < address.getParts().length; ++i) {
            if (!entity.getMeanings().containsKey(i)) {
                index = i;
                notEmpty = true;
                break;
            }
        }
        if (!notEmpty) {
            // No string left.
            return new LinkedList<>();
        }
        // Load all unused parts - construct possible string with 'cislo popisne' and 'cislo orientacni'.
        final List<String> parts = new LinkedList<>();
        for (int i = index; i < address.getParts().length; ++i) {
            if (entity.getMeanings().containsKey(i)) {
                // If we hit ranked we end.
                break;
            }
            parts.add(address.getParts()[i]);
        }
        // Now try to parse the value.
        LOG.info("Raw value: {}", Utils.join(parts, " "));

        // Remove all parts that does not start with number or /.
        for (int i = parts.size() - 1; i >= 0; --i) {
            final String part = parts.get(i);
            if (Character.isDigit(part.charAt(0)) || part.charAt(0) == '/' || part.charAt(0) == '\\') {
                // Ok, this might be part of an adress.
            } else {
                // Remove this object.
                parts.remove(i);
            }
        }
        return parts;
    }

}

