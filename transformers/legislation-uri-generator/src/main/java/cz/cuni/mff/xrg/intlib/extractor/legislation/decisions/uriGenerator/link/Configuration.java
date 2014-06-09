package cz.cuni.mff.xrg.intlib.extractor.legislation.decisions.uriGenerator.link;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Static class with general settings.
 * 
 * @author Jakub Starka
 */
public class Configuration {
    
    // -------------------------------------------------------------------------
    // Fields
    
    // List of available templates
    private static LinkedList<Pattern> templateList = new LinkedList<>();
    
    // List of static alias values.
    private static HashMap<String, Shortcut> aliasMap = new HashMap<>();
    
    // List of URI prefixes
    private static HashMap<String, String> prefixMap = new HashMap<>();

    // -------------------------------------------------------------------------
    // Setters, getters
    
    public static HashMap<String, Shortcut> getAliasMap() {
        return aliasMap;
    }

    public static void setAliasMap(HashMap<String, Shortcut> aliasMap) {
        Configuration.aliasMap = aliasMap;
    }

    public static HashMap<String, String> getPrefixMap() {
        return prefixMap;
    }

    public static void setPrefixMap(HashMap<String, String> prefixMap) {
        Configuration.prefixMap = prefixMap;
    }

    public static LinkedList<Pattern> getTemplateList() {
        return templateList;
    }

    public static void setTemplateList(LinkedList<Pattern> templateList) {
        Configuration.templateList = templateList;
    }
    
    // -------------------------------------------------------------------------
    // Loading
    
    /**
     * Loads configuration file.
     * 
     * @param config 
     */
    public static void load(String config) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setValidating(false);
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = builder.parse(config);

            processTree(doc);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }
    
    
    /**
     * Process configuration XML tree
     */
    private static void processTree(Document doc) {
        
        // load templates
        NodeList templates = doc.getElementsByTagName("template");
        
        for (int i = 0; i < templates.getLength(); i++) {
            Element curTemplate = (Element)templates.item(i);
            Pattern p = Pattern.compile(curTemplate.getTextContent());
            templateList.add(p);
        }
        
        // load documents
        NodeList shortcutList = doc.getElementsByTagName("document");
        
        for (int i = 0; i < shortcutList.getLength(); i++) {
            Element curShortcut = (Element)shortcutList.item(i);
            Shortcut sc = new Shortcut(
                    curShortcut.getAttribute("country"),
                    curShortcut.getAttribute("typeOfWork"),
                    curShortcut.getAttribute("year"), 
                    curShortcut.getAttribute("number"), 
                    curShortcut.getAttribute("section"), 
                    curShortcut.getAttribute("subsection") ,
                    curShortcut.getAttribute("para")
            );
            NodeList aliases = curShortcut.getElementsByTagName("alias");
            for (int a = 0; a < aliases.getLength(); a++) {
                aliasMap.put(aliases.item(a).getTextContent(), sc);
            }
        }        		
                
        // load URI prefixes (institution, work, ...)
        NodeList prefixList = doc.getElementsByTagName("prefix");
        
        for (int i = 0; i < prefixList.getLength(); i++) {
            Element e = (Element)prefixList.item(i);
            prefixMap.put(e.getAttribute("name"), e.getAttribute("value"));
        }     
        
    }
    
    // -------------------------------------------------------------------------
    
}
