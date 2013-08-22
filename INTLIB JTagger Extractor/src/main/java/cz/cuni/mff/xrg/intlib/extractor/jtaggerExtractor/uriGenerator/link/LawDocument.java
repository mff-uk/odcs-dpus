package cz.cuni.mff.xrg.intlib.extractor.jtaggerExtractor.uriGenerator.link;

import cz.cuni.mff.xrg.intlib.extractor.jtaggerExtractor.uriGenerator.IntLibLink;
import cz.cuni.mff.xrg.intlib.extractor.jtaggerExtractor.uriGenerator.shortcut.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import org.slf4j.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Jakub Starka
 */
public class LawDocument extends Work {
    
    private static org.slf4j.Logger logger;

    public LawDocument(Logger l) {
            logger = l;
    }
       
    // -------------------------------------------------------------------------
    // Fields
    
    // default values for the work
    protected Integer tempYear = 0;
    protected String tempNumber = "";
    protected String tempCountry = "cz";
    protected WorkType tempType = WorkType.ACT;
    
    protected AbbreviationMap abbreviationMap = new AbbreviationMap();
    
    protected Document inputDocument;
    
    protected static ShortcutMap genericShortcuts = new ShortcutMap();
    protected ShortcutMap customShortcuts = new ShortcutMap();

        
    // -------------------------------------------------------------------------
    // Document transformation
    
    /**
     * Transform given document. It comprises:
     *  - add URI to all institutions
     *  - add URI to all refered documents
     *  - add relative URI to all document parts
     * 
     * @param doc
     * @return 
     */
    public Document transform(Document doc) {
        
        doc.getDocumentElement().setAttribute("xmlns:rdf","http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        
        NodeList metadataList = doc.getElementsByTagName("metadata");
        
        abbreviationMap.parse(this);
        try {
            checkData(doc);
        } catch (FileNotFoundException ex) {
            logger.error(ex.getLocalizedMessage());
        } catch (IOException ex) {
           logger.error(ex.getLocalizedMessage());
        }

        
        NodeList vlist = doc.getElementsByTagName("valid");

        if (vlist.getLength() != 0) {
            version = vlist.item(0).getTextContent();
        }
        
        if (metadataList.getLength() != 0) {
            Element metadata = (Element)metadataList.item(0);
            
            NodeList metadataNumberList = metadata.getElementsByTagName("number");
            NodeList metadataYearList = metadata.getElementsByTagName("year");
            NodeList metadataTypeList = metadata.getElementsByTagName("type_of_work");
            NodeList metadataCountryList = metadata.getElementsByTagName("country_of_issue");
            
            if (metadataNumberList.getLength() != 0 && metadataTypeList.getLength() != 0) { 
                Element metadataNumber = (Element)metadataNumberList.item(0);
                Element metadataType = (Element)metadataTypeList.item(0);

                tempType = WorkType.getEnum(metadataType.getTextContent());
                
                if (metadataYearList.getLength() != 0) {
                    tempNumber = metadataNumber.getTextContent();
                    try {
                        tempYear = Integer.parseInt(((Element)metadataYearList.item(0)).getTextContent());
                    } catch (NumberFormatException ex) {
                        logger.error(ex.getLocalizedMessage());
                    }
                } else {
                    LinkedList<Work> thisWork = Work.parse(metadataNumber.getTextContent(), tempType, this);
                    
                    if (!thisWork.isEmpty()) {
                        Work firstWork = thisWork.get(0);
                        tempNumber = firstWork.number;
                        tempYear = firstWork.year;
                    }
                }
                
                if (metadataCountryList.getLength() != 0) {
                    tempCountry = metadataCountryList.item(0).getTextContent();
                }
                
                if (tempYear != 0) {
                    Element uri = doc.createElement("workURI");
                    Element euri = doc.createElement("expressionURI");
                    this.setValues("", tempCountry, tempType, tempYear, tempNumber, null, null, null, null, tempCountry, version);

                    uri.setTextContent(this.toString());
                    euri.setTextContent(this.toExpressionString());
                    metadata.appendChild(uri);
                    metadata.appendChild(euri);
                }
            }
        }
        
        
        for (WorkType w: WorkType.values()) {
            NodeList links = doc.getElementsByTagName(w.toString());
            
            //Pry se ma pouzivat decision
            if (w == w.JUDGMENT) w = w.DECISION;

            for (int i = 0; i < links.getLength(); i ++) {
                String link = links.item(i).getTextContent();
                String refersToAttribute = ((Element)links.item(i)).getAttribute("refers_to");
                Integer refersTo = null;
                try {
                    if (refersToAttribute != null && !refersToAttribute.equals("")) {
                        refersTo = Integer.parseInt(refersToAttribute);
                    }
                } catch (NumberFormatException ex) {
                    
                }
                
                LinkedList<Work> candidates = Work.parse(link, w, this, refersTo);
                if (candidates.size() == 1) {
                    URL url = null;
                    try {
                        url = new URL(candidates.get(0).toString());
                    } catch (Exception ex) {
                        logger.error(ex.getLocalizedMessage());
                    }
                    if (url != null) {
                        ((Element)links.item(i)).setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#","rdf:about", url.toString());
                        Work.recognized++;
                    }
                } else if (candidates.size() > 1) {
                    //Logger.getLogger("intlib").log(Level.INFO, "Multiple links available: {0}", link);
                    //for (int j = 0; j < candidates.size(); j ++) {
                    //    Logger.getLogger("intlib").log(Level.INFO, "Link {1}/{2}: {3}", new Object[]{link, Integer.toString(j+1), Integer.toString(candidates.size()), candidates.get(j)});
                    //}
                    StringBuilder concat = new StringBuilder();
                    for (int j = 0; j < candidates.size(); j ++) {
                        URL uri = null;
                        try {
                            uri = new URL(candidates.get(j).toString());
                        } catch (MalformedURLException ex) {
                            logger.error(ex.getLocalizedMessage());
                        }
                        if (uri != null) {
                            if (concat.length() > 0) concat.append(" ");
                            concat.append(uri.toString());
                            Work.recognized++;
                        }
                    }
                   ((Element)links.item(i)).setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#","rdf:about", concat.toString());
                } else {
                    Work.unrecognized ++;
                   logger.error("Link {} not recognized", link );
                }
            }
        }
        
        NodeList links = doc.getElementsByTagName("institution");

        for (int i = 0; i < links.getLength(); i ++) {
            Element in = (Element)links.item(i);
            String inst = in.getTextContent().toLowerCase();
            //Logger.getLogger("intlib").log(Level.INFO, "Institution original: {0}", inst);
            inst = inst.
                    replaceAll("ého", "ý").
                    replaceAll("ího", "í").
                    replaceAll("ímu", "í").
                    replaceAll("soudem", "soud").
                    replaceAll("ím", "í").
                    replaceAll("ým", "ý").
                    replaceAll("soudu", "soud").
                    replaceAll("soudu", "soud").
                    replaceAll("pobočky", "pobočka").
                    replaceAll("ministerstva", "ministerstvo").
                    replaceAll("[Čč]eské", "").
                    replaceAll("[rR]epubliky", "").
                    replaceAll(" [čČcC]r", " ").
                    replaceAll("  ", " ").
                    replaceAll(" soud pro mládež", "").
                    replaceAll("–", "-").trim();
            URL url = null;
            try {
                url = new URL(Configuration.getPrefixMap().get("institution") + clean(inst));
            } catch (Exception ex) {
                logger.error(ex.getLocalizedMessage());
            }
            if (url != null && 
                    (//url.toString().contains("odvolaci") 
                    //|| url.toString().contains("dovolaci") 
                    //|| url.toString().contains("stupne") 
                    //|| url.toString().contains("zemsky") 
                     url.toString().endsWith("/soud") 
                    //|| url.toString().endsWith("/krajsky-soud")
                    //|| url.toString().endsWith("/obvodni-soud")
                    ))
            {
                //Logger.getLogger("intlib").log(Level.INFO, "Invalid institution");                
            }
            else if (url == null)
            {
                
            }
            else
            {
                in.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#","rdf:about", url.toString());
                //Logger.getLogger("intlib").log(Level.INFO, "Institution URI: {0}", url.toString());
                Work.institutions++;
            }
        }
        
        addLinks(doc);
        try {
            addExpressions(doc, version);
        } catch (ParseException ex) {
            logger.error(ex.getLocalizedMessage());
        }
        
        return doc;
    }
    
    
    /**
     * Execute transformation on given input and output files.
     * 
     * @param input
     * @param output
     * @return 
     */
    public Document transform(String input, String output) {
        
        Document doc = null;
        
        String[] parts = input.split("_");
        
        //tempNumber = parts[0].replaceAll("in/", "");
        //tempYear = parts[1].replaceAll(".xml", "");
        
        try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                dbf.setValidating(false);
                DocumentBuilder builder = dbf.newDocumentBuilder();
                doc = builder.parse(input);
                this.inputDocument = doc;
                
                transform(doc);
                TransformerFactory tf = TransformerFactory.newInstance();
                Transformer writer = tf.newTransformer();
                writer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
                writer.transform(new DOMSource(doc), new StreamResult(new File(output)));
        } catch (TransformerException ex) {
            logger.error(ex.getLocalizedMessage());
        } catch (SAXException ex) {
            logger.error(ex.getLocalizedMessage());
        } catch (IOException ex) {
            logger.error(ex.getLocalizedMessage());
        } catch (ParserConfigurationException ex) {
            logger.error(ex.getLocalizedMessage());
        }

        return doc;
    }

    /**
     * Adds relative links to document sections.
     * 
     * @param doc 
     */    
    private void addLinks(Document doc) {
        
        if (tempYear == 0 || tempNumber.equals("")) {
            Work.missing ++;
            logger.warn("Document metadata with year and/or number are missing for input {}.", doc.getBaseURI());
            return;
        }
        
        String country = tempCountry;
        WorkType type = tempType;

        Element lastParent = doc.getDocumentElement();
        
        //NodeList section = doc.getElementsByTagName("section");
        NodeList section = getFirstElementsByTagName(doc.getDocumentElement(), "section");

        if (section.getLength() == 0) {
            // No section found
            //getSubsection(lastParent, country, type, EMPTY_SECTION_LABEL);
        } else {
            // All sections are created
            for (int j = 0; j < section.getLength(); j ++) {
                String sectionLabel = clean(((Element)section.item(j)).getAttribute("label").replaceAll("[Čč]l\\.", ""));

                if (!sectionLabel.isEmpty()) {
                    if (sectionLabel.contains("§")) {
                        sectionLabel = sectionLabel.replace("§ ", "");
                    }
                } else {
                    sectionLabel = EMPTY_SECTION_LABEL;
                }
                getSubsection((Element)section.item(j), country, type, sectionLabel);
            }
        }
    }
    
    /**
     * Find sections in a document and add relative uris.
     * 
     * @param section
     * @param country
     * @param type
     * @param sectionLabel 
     */
    
    private void getSubsection(Element section, String country, WorkType type, String sectionLabel) {
        //NodeList subsection = section.getElementsByTagName("subsection");
        NodeList subsection = getFirstElementsByTagName(section, "section");

        if (subsection.getLength() == 0) {
            //getParagraph(section, country, type, sectionLabel, EMPTY_SECTION_LABEL);
        } else {
            for (int k = 0; k < subsection.getLength(); k ++) {

                String subSectionLabel = clean(((Element)subsection.item(k)).getAttribute("label").replaceAll("[Čč]l.", ""));

                if (!subSectionLabel.isEmpty()) {
                    getParagraph((Element)subsection.item(k), country, type, sectionLabel, subSectionLabel);
                } else {
                    getParagraph((Element)subsection.item(k), country, type, sectionLabel, EMPTY_SECTION_LABEL);
                }
            }
        }
        
        if (!sectionLabel.equals(EMPTY_SECTION_LABEL)) {
            Work w = new Work("", country, type, tempYear, tempNumber, sectionLabel, null, null, null, null, null);
            Work.recognized++;
            URL url = null;
            try {
                url = new URL(w.toRelativeString());
            } catch (MalformedURLException ex) {
                logger.error(ex.getLocalizedMessage());
            }
            section.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#","rdf:about", url.toString());
        }
    }
    
    private void getParagraph(Element subsection, String country, WorkType type, String sectionLabel, String subSectionLabel) {
        //NodeList para = subsection.getElementsByTagName("paragraph");
        NodeList para = getFirstElementsByTagName(subsection, "section");

        if (para.getLength() == 0) {
            //getSubparagraph(subsection, country, type, sectionLabel, subSectionLabel, EMPTY_SECTION_LABEL);
        } else {
            for (int l = 0; l < para.getLength(); l ++) {
                String paraLabel = clean(((Element)para.item(l)).getAttribute("label").replaceAll("[Čč]l.", ""));

                if (!paraLabel.isEmpty()) {
                    getSubparagraph((Element)para.item(l), country, type, sectionLabel, subSectionLabel, paraLabel);
                } else {
                    getSubparagraph((Element)para.item(l), country, type, sectionLabel, subSectionLabel, EMPTY_SECTION_LABEL);
                }
            }
         }

        if (!subSectionLabel.equals(EMPTY_SECTION_LABEL)) {
            Work w = new Work("", country, type, tempYear, tempNumber, sectionLabel, subSectionLabel, null, null, null, null);
            Work.recognized++;
            URL url = null;
            try {
                url = new URL(w.toRelativeString());
            } catch (MalformedURLException ex) {
                logger.error(ex.getLocalizedMessage());
            }
            subsection.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#","rdf:about", url.toString());
        }
    }
    
    private void getSubparagraph(Element para, String country, WorkType type, String sectionLabel, String subSectionLabel, String paraLabel) {
        //NodeList subpara = para.getElementsByTagName("subparagraph");
        NodeList subpara = getFirstElementsByTagName(para, "section");

        for (int m = 0; m < subpara.getLength(); m ++) {
            String subparaLabel = ((Element)subpara.item(m)).getAttribute("label").replaceAll("[Čč]l\\.", "");

            if (!subparaLabel.isEmpty()) {
                Work w = new Work("", country, type, tempYear, tempNumber, sectionLabel, subSectionLabel, paraLabel, subparaLabel, null, null);
                URL url = null;
                try {
                    url = new URL(w.toRelativeString());
                } catch (MalformedURLException ex) {
                   logger.error(ex.getLocalizedMessage());
                }
                ((Element)subpara.item(m)).setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#","rdf:about", url.toString());
                Work.recognized++;
            }
        }

        if (!paraLabel.equals(EMPTY_SECTION_LABEL)) {
            Work w = new Work("", country, type, tempYear, tempNumber, sectionLabel, subSectionLabel, paraLabel, null, null, null);
            URL url = null;
            try {
                url = new URL(w.toRelativeString());
            } catch (MalformedURLException ex) {
                logger.error(ex.getLocalizedMessage());
            }
            para.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#","rdf:about", url.toString());
            Work.recognized++;
        }
    }

    /**
     * Find effectiveness tags in document and try to get effectiveness URI, based on the date of the document.
     * 
     * @param doc
     * @param issued
     * @throws ParseException 
     */
    public void addExpressions(Document doc, String issued) throws ParseException {
        
        if (issued == null) {
            return;
        }
        
        NodeList nodes = doc.getElementsByTagName("effectiveness");
        
        for (int i = 0; i < nodes.getLength(); i ++) {
            
            
            Element e = (Element)nodes.item(i);
            Node n = e.getPreviousSibling();
            while (n != null && n.getNodeType() != Node.ELEMENT_NODE) {
                n = n.getPreviousSibling();
            }
            
            if (n.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            } 
            
            if (!e.getTextContent().contains("znění pozdějších předpisů")) {
                continue;
            }
            
            Element el = (Element)n;
            
            LinkedList<Work> workList = Work.parse(el.getTextContent(), getWorkType(el.getTagName()), this, Integer.parseInt(el.getAttribute("refers_to")));
            
            if (workList.isEmpty()) {
                continue;
            }
            
            Work w = workList.get(0);
            w.setTypeOfWork(WorkType.getEnum(el.getTagName()));
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            
            Date localVersion = ValidityMap.getValidity(w, sdf.parse(issued));
            if (localVersion != null) {
                w.setVersion(sdf.format(localVersion));
                w.setLanguage("cz");
                
                el.setAttribute("expressionURI", w.toExpressionString());
            }
        }
        
    }
    
    /**
     * Find in the document all mentioned laws.
     * 
     * @param doc 
     */    
    private void checkData(Document doc) throws FileNotFoundException, IOException {
        
        String context = doc.getDocumentElement().getTextContent();
        context = context.toLowerCase().replaceAll("[ \t\r\n]+", " ");
        
        customShortcuts.clear();
        
        for (Map.Entry<String, Work> e: genericShortcuts.entrySet()) {
            if ((context.contains(e.getKey().toLowerCase())) || (context.contains(e.getKey().toLowerCase().replaceAll("[Zz]ákon ", "")))) {
                customShortcuts.put(e.getKey(), e.getValue());
            } 
        }
        
    }
    
    

    // -------------------------------------------------------------------------

    public AbbreviationMap getAbbreviationMap() {
        return abbreviationMap;
    }

    public Document getInputDocument() {
        return inputDocument;
    }

    public ShortcutMap getCustomShortcuts() {
        return customShortcuts;
    }

    public static ShortcutMap getGenericShortcuts() {
        return genericShortcuts;
    }
    
    
    
    
    
}
