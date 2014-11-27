package cz.cuni.mff.xrg.intlib.extractor.legislation.decisions.uriGenerator.shortcut;

import cz.cuni.mff.xrg.intlib.extractor.legislation.decisions.uriGenerator.link.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Jakub Starka
 */
public class AbbreviationMap extends ShortcutMap {
    
       private static final org.slf4j.Logger log= LoggerFactory.getLogger(AbbreviationMap.class);
    
    protected HashMap<Integer, Work> definitionMap;
    //protected HashMap<String, Work> abbreviationMap;
    
    {
        definitionMap = new HashMap<>();
    }
    
    public void parse(LawDocument doc) {
        
        NodeList abbDefinitionList = doc.getInputDocument().getElementsByTagName("abbreviation_definition");
        // create abbreviation definition map by their id
        for (int i = 0; i < abbDefinitionList.getLength(); i ++) {
            Integer definitionId = Integer.parseInt(((Element)abbDefinitionList.item(i)).getAttribute("id"));
            LinkedList<Work> definitionWorkList = Work.parse(abbDefinitionList.item(i).getTextContent(), WorkType.ACT, doc, null);
            Work definitionWork = null;
            if (definitionWorkList.size() == 1) {
                definitionWork = definitionWorkList.get(0);
            } else if (definitionWorkList.isEmpty()) {
                Logger.getLogger(AbbreviationMap.class.getName()).log(Level.INFO, "Expression:{0} was not parsed.", abbDefinitionList.item(i).getTextContent());
            } else {
                Logger.getLogger(AbbreviationMap.class.getName()).log(Level.INFO, "Expression:{0} has more than one meaning.", abbDefinitionList.item(i).getTextContent());
            }
            if (definitionWork != null) {
                definitionMap.put(definitionId, definitionWork);
            }
        }
        // create abbreviation definition map by text
        NodeList abbList = doc.getInputDocument().getElementsByTagName("abbreviation_label");
        
        for (int i = 0; i < abbList.getLength(); i ++) {
            String defId = ((Element)abbList.item(i)).getAttribute("refers_to");
            Integer definitionId = null;
            if (defId != null) {
                try {
                    definitionId = Integer.parseInt(defId);
                } catch (NumberFormatException ex) {
                    log.error(ex.getLocalizedMessage());
                }
            }
            Work definitionWork = definitionMap.get(definitionId);
            
            if (definitionWork != null) {
                super.put(abbList.item(i).getTextContent(), definitionWork);
            }
        }
        
    }
    
    public Work getWork(String abbreviation) {
        return super.get(abbreviation);
    }
    
    public Work getWork(Integer referedId) {
        return definitionMap.get(referedId);
    }
    
    
    
}
