package eu.unifiedviews.cssz.cz.komix.xls2csv;

import eu.unifiedviews.cssz.Xls2csv;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by hugo on 29.5.14.
 */
public class Fakt {

    public int poradi;
    public List<String> data;
    public List<Integer> filtr;
    public DimenzeBox dimenze;
    
    public Demo01 demo;

    private static final Logger log = LoggerFactory.getLogger(Fakt.class);
    
    public Fakt(int poradi, Demo01 demo) {
        this.poradi = poradi;
        this.data = new ArrayList<String>();
        this.dimenze = new DimenzeBox();
        this.filtr = null;
        this.demo = demo;
    }

    public void saveDimenze(List<Dimenze> dimenze) {
        if (filtr == null) throw new RuntimeException("Ukladani dimenzi faktu driv nez filtru");
        if (filtr.size() > dimenze.size()) throw new RuntimeException("Nedostatek dimenzi u faktu");
        if (! this.dimenze.box.isEmpty()) return;
//        log.debug("Ukladam dimenze k faktu " + this.poradi + ": " + dimenze);
        for (Dimenze d: dimenze) {
            this.dimenze.saveDimenze(d);
        }
    }

    public String saveToFile(String path, String baseName) {
        // path + Fxx + baseName + ".csv"
        String finalOutputFilePath = null;
        try {
            
            OutputStreamWriter csv;            
            if (demo.souborNames.containsKey(this.poradi)) {
                finalOutputFilePath = path + demo.souborNames.get(this.poradi);
                finalOutputFilePath = finalOutputFilePath.replaceAll(" ", "");
                csv = new OutputStreamWriter(
                    new FileOutputStream(finalOutputFilePath),
                    Charset.forName("UTF-8").newEncoder() 
                );
                
            } else {
                finalOutputFilePath = path + "F"
                    + String.format("%02d",this.poradi) + "_" + baseName + ".csv";
                finalOutputFilePath = finalOutputFilePath.replaceAll(" ", "");
                csv = new OutputStreamWriter(
                    new FileOutputStream(finalOutputFilePath),
                    Charset.forName("UTF-8").newEncoder() 
                );
            }
            
//            FileWriter csv;
//            if (Demo01.souborNames.containsKey(this.poradi)) {
//                finalOutputFilePath = path + Demo01.souborNames.get(this.poradi);
//                csv = new FileWriter(finalOutputFilePath);
//                
//            } else {
//                finalOutputFilePath = path + "F"
//                    + String.format("%02d",this.poradi) + "_" + baseName + ".csv";
//                csv = new FileWriter(finalOutputFilePath);
//            }
//            csv.append(baseName);
//            csv.append("\r\n");
            List<Dimenze> dims = dimenze.getSortedDimenze();
            csv.append("\"Fakt\"");
            for (Dimenze d: dims) {
                if (filtr.contains(d.poradi)) {
                    csv.append(",\"" + d.popis.trim()+"\"");
                }
            }
            csv.append(",\"target_data_cube\",\"source_file\"");
            csv.append("\r\n");
            String cubeName = "MISSING-CUBE-NAME";
            log.debug("KOSTKY: " + demo.faktCubeNames + " / " + this.poradi);
            if (demo.faktCubeNames.containsKey(this.poradi)) {
                cubeName = demo.faktCubeNames.get(this.poradi);
                log.debug("cubeName : " + cubeName);
            }
            for (String line: data) {
                csv.append(line);
                csv.append(",\"" + cubeName + "\",\"" + baseName + ".xls\"");
                csv.append("\r\n");
            }
            csv.flush();
            csv.close();
        } catch (IOException e) {
            log.error("Problem creating output csv file {}: {}", finalOutputFilePath, e.getLocalizedMessage());
        }
        return finalOutputFilePath;
    }
}
