package cz.cuni.mff.css_parser.database;

import cz.mff.cuni.scraper.lib.triple.Triple;
import cz.cuni.mff.css_parser.database.Journal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *  Cache for triples. Stores triple (and nested triples) associated with subject URI and property.
 *
 *  @author Jakub Starka
 */
public class HashDirectory {
    
    private HashMap<String, HashMap<String, String>> dir = new HashMap<String, HashMap<String, String>>();
    
    private static HashDirectory instance;
    
    private static HashDirectory getDirectory() {
            if (instance == null) {
                    instance = new HashDirectory();
            }
            return instance;
    }
 
    public static boolean contains(String uri, String property, String  triple) {
        String t = triple;
        String hash = null;
        try {
            hash = new BigInteger(1, MessageDigest.getInstance("MD5").digest(t.getBytes())).toString(16);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(HashDirectory.class.getName()).log(Level.SEVERE, null, ex);
        }
        String storedHash = Journal.getHash(uri, property);
        return (storedHash != null && storedHash.equals(hash));
    }
    
    public static boolean containsKey(String uri) {
        return getDirectory().dir.containsKey(uri);
    }
    
    public static void put(String uri, String property, String triple) {
        
        String t = triple;
        String hash = null;
        try {
            hash = new BigInteger(1, MessageDigest.getInstance("MD5").digest(t.getBytes())).toString(16);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(HashDirectory.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Journal.insertHash(uri, property, hash, 0);
        
        /*if (!getDirectory().dir.containsKey(uri) || !getDirectory().dir.get(uri).containsKey(property)) {
            String t = triple;
            String hash = null;
            try {
                hash = new BigInteger(1, MessageDigest.getInstance("MD5").digest(t.getBytes())).toString(16);
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(HashDirectory.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (getDirectory().dir.containsKey(uri)) {
                getDirectory().dir.get(uri).put(property, hash);
            } else {
                HashMap<String, String> newMap = new HashMap<String, String>();
                newMap.put(property, hash);
                getDirectory().dir.put(uri, newMap);
            }
            Journal.insertHash(uri, property, hash, 0);
        }*/
    }
}
