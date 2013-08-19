package cz.cuni.mff.css_parser.database;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jakub Starka
 */
public class Journal {
    
    private static final String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    
    private static Connection connection = null;
    
    private static void prepare() throws SQLException {
        connection.createStatement().execute("DELETE FROM document");
        
        insertDocument = connection.prepareStatement("INSERT INTO document(url, scraped) VALUES(?,?)", PreparedStatement.RETURN_GENERATED_KEYS);
        insertBusinessEntity = connection.prepareStatement("INSERT INTO business_entity(uri, name, postal) VALUES(?,?,?)", PreparedStatement.RETURN_GENERATED_KEYS);
        insertHash = connection.prepareStatement("INSERT INTO hash(uri, property, hash, document) VALUES(?,?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
        
        selectDocument = connection.prepareStatement("SELECT url FROM document WHERE url=?");
        selectBusinessEntity = connection.prepareStatement("SELECT uri FROM business_entity WHERE name=? AND postal=?");
        selectHash = connection.prepareStatement("SELECT hash FROM hash WHERE uri=? AND property=?");
        
        containsBusinessEntity = connection.prepareStatement("SELECT name, postal FROM business_entity WHERE uri=?");
    }
    
    public static Connection getConnection() throws SQLException {
        if (connection == null) {
            try{
                // Load the EmbeddedDriver class
                Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
                System.out.println("Loaded Derby Driver");
            }catch(Exception e){
                e.printStackTrace();
                System.out.println("Error loading Derby Driver.  Shutting down.");
                System.exit(-1);
            }
            
            String dbName="isvzus";
            String connectionURL = "jdbc:derby:" + dbName + ";create=true";
            connection = DriverManager.getConnection(connectionURL);
            
            try {
                createTables();
            } catch (SQLException ex) {
                System.out.println("Database already exists");
            }
            prepare();
        }
        return connection;
    }
    
    public static void createTables() throws SQLException {
        String[] createString = { "CREATE  TABLE document ("
                + "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY ,"
                + "url VARCHAR(255) NOT NULL ,"
                + "scraped TIMESTAMP NOT NULL ,"
                + "PRIMARY KEY (id) ,"
                + "UNIQUE (url) "
                + ")", 

                "CREATE TABLE business_entity ("
                + "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY ,"
                + "uri VARCHAR(255) NOT NULL ,"
                + "name VARCHAR(32) NOT NULL ,"
                + "postal VARCHAR(5) NOT NULL ,"
                + "PRIMARY KEY(id)"
                + ")",

                "CREATE TABLE hash ("
                + "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY,"
                + "uri VARCHAR(255) NOT NULL,"
                + "property VARCHAR(128) NOT NULL,"
                + "hash VARCHAR(32) NOT NULL,"
                + "document INTEGER NOT NULL, "
                + "PRIMARY KEY(id) ,"
                + "UNIQUE (uri, property)"
                + ")"
        };
        
        Statement s = connection.createStatement();
        for (String q: createString) {
            s.execute(q);
        }
    }
    
    private static PreparedStatement insertDocument = null;
    private static PreparedStatement insertBusinessEntity = null;
    private static PreparedStatement insertHash = null;
    private static PreparedStatement selectDocument = null;
    private static PreparedStatement selectBusinessEntity = null;
    private static PreparedStatement selectHash = null;
    
    private static PreparedStatement containsBusinessEntity = null;
    
    public static Integer insertDocument(String url) {
        try {
            insertDocument.setString(1, url);
            insertDocument.setTimestamp(2, new Timestamp(Calendar.getInstance().getTimeInMillis()));
            insertDocument.execute();
            ResultSet keys = insertDocument.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            } else {
                throw new SQLException("Creating user failed, no generated key obtained.");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Journal.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static String getDocument(String id) {
        try {
            selectDocument.setString(1, id);
            ResultSet result = selectDocument.executeQuery();
            
            
            if (result.next()) {
                return result.getString("url");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Journal.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
    
    public static String getDocument(Integer id) {
        try {
            selectDocument.setInt(1, id);
            ResultSet result = selectDocument.executeQuery();
            
            
            if (result.next()) {
                return result.getString("uri");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Journal.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
    
    public static String getHash(String txt) {
        String hash = null;
        try {
            hash = new BigInteger(1, MessageDigest.getInstance("MD5").digest(txt.getBytes())).toString(16);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(HashDirectory.class.getName()).log(Level.SEVERE, null, ex);
        }
        return hash;
    }
    
    public static Integer insertBusinessEntity(String uri, String name, String postal) {
        try {
            insertBusinessEntity.setString(1, uri);
            
            String hash = null;
            try {
                hash = new BigInteger(1, MessageDigest.getInstance("MD5").digest(name.getBytes())).toString(16);
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(HashDirectory.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            insertBusinessEntity.setString(2, hash);
            insertBusinessEntity.setString(3, postal);
            insertBusinessEntity.execute();
            ResultSet keys = insertBusinessEntity.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            } else {
                throw new SQLException("Creating user failed, no generated key obtained.");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Journal.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static Boolean containsBusinessEntity(String url) {
        try {
            containsBusinessEntity.setString(1, url);
            ResultSet result = containsBusinessEntity.executeQuery();
            
            return result.next();
        } catch (SQLException ex) {
            Logger.getLogger(Journal.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public static String getBusinessEntity(String name, String postal) {
        try {
            String hash = null;
            try {
                hash = new BigInteger(1, MessageDigest.getInstance("MD5").digest(name.getBytes())).toString(16);
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(HashDirectory.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            selectBusinessEntity.setString(1, hash);
            selectBusinessEntity.setString(2, postal);
            ResultSet result = selectBusinessEntity.executeQuery();
            
            if (result.next()) {
                return result.getString("uri");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Journal.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static Integer insertHash(String uri, String property, String hash, Integer document) {
        try {
            insertHash.setString(1, uri);
            insertHash.setString(2, property);
            insertHash.setString(3, hash);
            insertHash.setInt(4, document);
            insertHash.execute();
            ResultSet keys = insertHash.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            } else {
                throw new SQLException("Creating user failed, no generated key obtained.");
            }
        } catch (SQLException ex) {
            System.err.println("Given uri " + uri + " with property " + property + " already set.");
        }
        return null;
    }
    
    public static String getHash(String uri, String property) {
        try {
            selectHash.setString(1, uri);
            selectHash.setString(2, property);
            ResultSet result = selectHash.executeQuery();
            
            if (result.next()) {
                return result.getString("hash");
            } 
        } catch (SQLException ex) {
            Logger.getLogger(Journal.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
}
