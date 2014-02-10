package cz.opendata.linked.cz.gov.smlouvy;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import cz.cuni.mff.xrg.scraper.lib.template.ParseEntry;

public class XMLYearListParser extends DefaultHandler {
    private String type;
    private boolean addr = false;
    private boolean zneplatneno = false;
    private String currentAdr;
    private StringBuilder sb;
    private LinkedList<ParseEntry> out;
    
    public XMLYearListParser(LinkedList<ParseEntry> o, String t) {
    	out = o;
    	type = t;
    }
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts)
            throws SAXException {
    	//System.out.println("Start: " + qName);
    	if (qName.equals("PolozkaURL")) {
    		addr = true;
    		sb = new StringBuilder();
    	}
    	else if (qName.equals("Zneplatneny")) {
    		sb = new StringBuilder();
    		zneplatneno = true;
    	}
        super.startElement(uri, localName, qName, atts);
    }
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
    	//System.out.println("End: " + qName);
    	if (qName.equals("PolozkaURL")) {
        	addr = false;
			currentAdr = sb.toString();
        }
    	else if (qName.equals("Zneplatneny")) {
    		zneplatneno = false;
    		String znepl = sb.toString();
    		if (znepl.equals("false"))
    		{
	    		try {
	        		out.add(new ParseEntry(new URL(currentAdr), "detail-" + type, "xml"));
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
    	if (zneplatneno) {
    		sb.append(new String(ch, start, length));
    	}
        super.characters(ch, start, length);
    }

}
