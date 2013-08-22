package cz.cuni.mff.scraper.lib.generator;

import cz.cuni.mff.css_parser.database.Journal;
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.UUID;

/**
 * Business Entity repository.
 *
 * @author Jakub Starka
 */
public class BEDirectory {

    private static BEDirectory instance = null;
    private HashMap<String, HashMap<String, String>> dir = new HashMap<String, HashMap<String, String>>();
    private HashSet<String> reserved = new HashSet<String>();

    private HashSet<String> dereferenced = new HashSet<String>();
    
    public static void dereference(String uri) {
        getDirectory().dereferenced.add(uri);
    }
    
    public static boolean isDereferenced(String uri) {
        return getDirectory().dereferenced.contains(uri);
    }
    
    private static BEDirectory getDirectory() {
        if (instance == null) {
            instance = new BEDirectory();
        }
        return instance;
    }
    private static int matchCount = 0;

    public static int getMatchCount() {
        return BEDirectory.matchCount;
    }

    @Deprecated
    private String getIdentifier(String name, String postal, String id) {
        String uri = Journal.getBusinessEntity(name, postal);
        postal = postal.replaceAll(" ", "");
        name = name.replace(";", "");
        
        if (uri == null) {
            matchCount ++;
            uri = generateUri(id);
            Journal.insertBusinessEntity(uri, name, postal);
        }
        
        return uri;
        
        /*if (dir.containsKey(name)) {
            HashMap<String, String> found = dir.get(name);
            if (found.containsKey(postal)) {
                BEDirectory.matchCount++;
                uri = found.get(postal);
            } else {
                uri = generateUri(id);
                found.put(postal, uri);
                Journal.insertBusinessEntity(uri, name, postal);
            }
        } else {
            HashMap<String, String> newList = new HashMap<String, String>();
            uri = generateUri(id);
            newList.put(postal, uri);
            dir.put(name, newList);
            Journal.insertBusinessEntity(uri, name, postal);
        }
        return uri;*/
    }

    @Deprecated
    public static synchronized String GetIdentifier(String name, String postal, String id) {
        return BEDirectory.getDirectory().getIdentifier(name, postal, id);
    }
    
    public static synchronized String GetIdentifier(String id, String documentId) {
        return BEDirectory.getDirectory().getIdentifier(id, documentId);
    }
    
    private boolean hasIdentifier(String name, String postal, String id) {
        postal = postal.replaceAll(" ", "");
        //return (this.dir.containsKey(name) && (this.dir.get(name).containsKey(postal)));
        return (
                (id != null && Journal.containsBusinessEntity(generateUri(id))) || 
                Journal.getBusinessEntity(name, postal) != null
        );
    }

    public static boolean HasIdentifier(String name, String postal, String id) {
        return BEDirectory.getDirectory().hasIdentifier(name, postal, id);
    }

    private String generateUri(String defaultId, String documentId) {
        String id = null;
        if (defaultId == null || defaultId.equals("")) {
            id = UUID.randomUUID().toString();
        } else {
            getDirectory().reserved.add(defaultId);
            id = "CZ" + defaultId;
        }
        return "http://ld.opendata.cz/resource/business-entity/" + id + "-" + documentId;
    }
    
    private String generateUri(String defaultId) {
        String id = null;
        if (defaultId == null || defaultId.equals("")) {
            id = UUID.randomUUID().toString();
        } else {
            getDirectory().reserved.add(defaultId);
            id = "CZ" + defaultId;
        }
        return "http://ld.opendata.cz/resource/business-entity/" + id;
    }

    public static synchronized void store(String file) throws IOException {
        /*
         * FileWriter fw = new FileWriter(file); OutputStream os = new FileOutputStream(file);
         */
        PrintStream ps = new PrintStream(file, "UTF-8");
        for (Entry<String, HashMap<String, String>> name : getDirectory().dir.entrySet()) {
            for (Entry<String, String> postal : name.getValue().entrySet()) {
                ps.println(name.getKey() + ";" + postal.getKey() + ";" + postal.getValue());
            }
        }
        
        PrintStream ps2 = new PrintStream("deref.txt", "UTF-8");
        for (String name : getDirectory().dereferenced) {
            ps2.println(name);
        }
        
        
    }
    private HashSet<String> used;

    private void load(String file, String usedFile) throws FileNotFoundException, IOException {
        
        File d = new File("deref.txt");
        if (d.exists()) {
            dereferenced = new HashSet<String>();
            BufferedReader brd = new BufferedReader(new FileReader("deref.txt"));
            String line = null;
            while ((line = brd.readLine()) != null) {
                dereferenced.add(line.replaceAll("isvzus.cz/business-entity/", "business-entity/CZ"));
            }
        }
        
        used = new HashSet<String>();
        BufferedReader bru = new BufferedReader(new FileReader(usedFile));
        String line = null;
        while ((line = bru.readLine()) != null) {
            used.add(line);
        }

        //BufferedReader br = new BufferedReader(new FileReader(file));
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        line = null;
        while ((line = br.readLine()) != null) {
            String[] values = line.split(";");
            if (values.length != 3 || values[0].trim().equals("") || values[1].trim().equals("") || values[2].trim().equals("")) {
                System.out.println("Unexpected line: " + line);
                continue;
            }
            if (!used.contains(values[2])) {
                //System.out.println("Skipped:" + values[2]);
                continue;
            } else {
                System.out.println("URI used: " + values[2]);
            }

            if (dir.containsKey(values[0])) {
                if (!dir.get(values[0]).containsKey(values[1])) {
                    dir.get(values[0]).put(values[1], values[2]);
                }
            } else {
                HashMap<String, String> p = new HashMap<String, String>();
                p.put(values[1], values[2]);
                dir.put(values[0], p);
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        this.store("bed.txt");
    }

    public BEDirectory() {
        try {
            load("bed.txt", "used.txt");
        } catch (FileNotFoundException ex) {
            //Logger.getLogger(BEDirectory.class.getName()).log(Level.INFO, null, "BED file not found");
        } catch (IOException ex) {
            //Logger.getLogger(BEDirectory.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String getIdentifier(String id, String documentId) {
        return generateUri(id, documentId);
    }
}
