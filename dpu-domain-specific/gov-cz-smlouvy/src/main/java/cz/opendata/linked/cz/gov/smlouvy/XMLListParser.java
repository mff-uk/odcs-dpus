package cz.opendata.linked.cz.gov.smlouvy;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import cz.cuni.mff.xrg.scraper.lib.template.ParseEntry;

public class XMLListParser extends DefaultHandler {
    private String type;
    private boolean addr = false;
    private StringBuilder sb;
    private LinkedList<ParseEntry> out;
    
    public XMLListParser(LinkedList<ParseEntry> o, String t) {
        out = o;
        type = t;
    }
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts)
            throws SAXException {
        //System.out.println("Start: " + qName);
        if (qName.equals("SeznamURL")) {
            addr = true;
            sb = new StringBuilder();
        }
        super.startElement(uri, localName, qName, atts);
    }
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        //System.out.println("End: " + qName);
        if (qName.equals("SeznamURL")) {
            addr = false;
            try {
                out.add(new ParseEntry(new URL(sb.toString()), "seznamrok-"+ type, "xml"));
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        super.endElement(uri, localName, qName);
    }
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        //System.out.println("Chars: " + new String(ch, start, length));
        if (addr) {
            sb.append(new String(ch, start, length));
        }
        super.characters(ch, start, length);
    }

}
