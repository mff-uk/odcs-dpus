package cz.cuni.mff.xrg.intlib.extractor.jtaggerExtractor.jTagger;

import cz.cuni.mff.xrg.intlib.extractor.jtaggerExtractor.JTaggerExtractor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author vinc
 */
public class JTagger {
     
    private static final Logger logger = LoggerFactory.getLogger(JTaggerExtractor.class);

    
    //private static String path = "src/main/resources/";
    private static String path;
    private static final String pathToRes = "src/main/resources/";
   

    public static void setPath(String apath) {
        
        path = apath + File.separator + pathToRes;
        logger.info("Path for jTagger's resources: {}", path);
    }
    
    private static String[] dirs = {
        "/tmp/jtagger", 
        "/tmp/jtagger/txt_source",
        "/tmp/jtagger/txt",
        "/tmp/jtagger/empty",
        "/tmp/jtagger/hmm.in",
        "/tmp/jtagger/hmm.out",
        "/tmp/jtagger/tagged",
        "/tmp/jtagger/rulebased",
        "/tmp/jtagger/validated",
        "/tmp/jtagger/linked",
        "/tmp/jtagger/merged",
        "/tmp/jtagger/validated2",
        "/tmp/jtagger/linkedZ"
    };

    public static JTaggerResult processFile(String text, String court) throws Exception {
        Process process;

        // Ak neexistuje, zalozim si adresarovu strukturu
        for (int i = 0; i < dirs.length; i++) {
            File handler = new File(dirs[i]);
            if (!handler.isDirectory()) {
                handler.mkdir();
            }
            else {
                for (File file: handler.listFiles()) {
                    file.delete();
                }
            }
        }
        
//         String workingDir = System.getProperty("user.dir");
//	   logger.info("Current working directory : " + workingDir);

        //
        // Ulozim zdrojove TXT
        //

        logger.debug("\n\n***\nUkadam zdrojove subory na disk...\n***\n");

        logger.debug("/tmp/jtagger/txt_source/judikatura.zakon.txt");
        PrintWriter writer = null;
        writer = new PrintWriter("/tmp/jtagger/txt_source/judikatura.zakon.txt", "UTF-8");
        writer.println(text);
        writer.close();
        logger.debug("DONE");

        logger.debug("/tmp/jtagger/txt_source/judikatura.rozhodnuti.txt");
        writer = new PrintWriter("/tmp/jtagger/txt_source/judikatura.rozhodnuti.txt", "UTF-8");
        writer.println(text);
        writer.close();
        logger.debug("DONE");

        logger.debug("/tmp/jtagger/txt_source/judikatura.zkratky.txt");
        writer = new PrintWriter("/tmp/jtagger/txt_source/judikatura.zkratky.txt", "UTF-8");
        writer.println(text);
        writer.close();
        logger.debug("DONE");

        
        String perlIntro = "perl -I "+ path +  " " + path;
        //
        // Tokenizacia
        //

        logger.debug("\n\n***\nTokenizacia\n***\n");

        logger.debug(perlIntro + "jtagger/txt2vxml.pl /tmp/jtagger/txt /tmp/jtagger/txt_source/judikatura.zakon.txt");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/txt_tokenization.pl /tmp/jtagger/txt /tmp/jtagger/txt_source/judikatura.zakon.txt");
               //process = Runtime.getRuntime().exec("perl -I "+ path +  " " + path + "jtagger/txt_tokenization.pl /tmp/jtagger/txt /tmp/jtagger/txt_source/judikatura.zakon.txt");

        printProcessOutput(process);
        logger.debug("DONE");
        
        logger.debug(perlIntro + "jtagger/txt2vxml.pl /tmp/jtagger/txt /tmp/jtagger/txt_source/judikatura.rozhodnuti.txt");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/txt_tokenization.pl /tmp/jtagger/txt /tmp/jtagger/txt_source/judikatura.rozhodnuti.txt");
        printProcessOutput(process);
        logger.debug("DONE");

        logger.debug(perlIntro + "jtagger/txt2vxml.pl /tmp/jtagger/txt /tmp/jtagger/txt_source/judikatura.zkratky.txt");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/txt_tokenization.pl /tmp/jtagger/txt /tmp/jtagger/txt_source/judikatura.zkratky.txt");
        printProcessOutput(process);
        logger.debug("DONE");        
        
        //
        // Prevod TXT -> VXML
        //

        logger.debug("\n\n***\nPrevod TXT -> VXML...\n***\n");

        logger.debug(perlIntro + "jtagger/txt2vxml.pl /tmp/jtagger/empty /tmp/jtagger/txt/judikatura.zakon.txt");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/txt2vxml.pl /tmp/jtagger/empty /tmp/jtagger/txt/judikatura.zakon.txt");
        printProcessOutput(process);
        logger.debug("DONE");
        
        logger.debug(perlIntro + "jtagger/txt2vxml.pl /tmp/jtagger/empty /tmp/jtagger/txt/judikatura.rozhodnuti.txt");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/txt2vxml.pl /tmp/jtagger/empty /tmp/jtagger/txt/judikatura.rozhodnuti.txt");
        printProcessOutput(process);
        logger.debug("DONE");

        logger.debug(perlIntro + "jtagger/txt2vxml.pl /tmp/jtagger/empty /tmp/jtagger/txt/judikatura.zkratky.txt");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/txt2vxml.pl /tmp/jtagger/empty /tmp/jtagger/txt/judikatura.zkratky.txt");
        printProcessOutput(process);
        logger.debug("DONE");

        //
        // Prevod VXML -> HMM
        //

        logger.debug("\n\n***\nPrevod VXML -> HMM...\n***\n");
        
        logger.debug(perlIntro + "jtagger/vxml2hmm.pl /tmp/jtagger/hmm.in test Zakon /tmp/jtagger/empty/judikatura.zakon.vxml");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/vxml2hmm.pl /tmp/jtagger/hmm.in test Zakon /tmp/jtagger/empty/judikatura.zakon.vxml");
        printProcessOutput(process);
        logger.debug("DONE");
        
        logger.debug(perlIntro + "jtagger/vxml2hmm.pl /tmp/jtagger/hmm.in test Zakon /tmp/jtagger/empty/judikatura.rozhodnuti.vxml");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/vxml2hmm.pl /tmp/jtagger/hmm.in test Zakon /tmp/jtagger/empty/judikatura.rozhodnuti.vxml");
        printProcessOutput(process);
        logger.debug("DONE");

        logger.debug(perlIntro + "jtagger/vxml2hmm.pl /tmp/jtagger/hmm.in test Zakon /tmp/jtagger/empty/judikatura.zkratky.vxml");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/vxml2hmm.pl /tmp/jtagger/hmm.in test Zakon /tmp/jtagger/empty/judikatura.zkratky.vxml");
        printProcessOutput(process);
        logger.debug("DONE");

        //
        // Binarka HunPOS
        //

        logger.debug("\n\n***\nVolanie taggeru...\n***\n");

        logger.debug("cat /tmp/jtagger/hmm.in/judikatura.zakon.hmm | " + path + "tagger/hunpos-tag " + path + "models/" + court + "/zakon.mod");
        String[] cmd1 = {"bash", "-c", "cat /tmp/jtagger/hmm.in/judikatura.zakon.hmm | " + path + "tagger/hunpos-tag " + path + "models/" + court + "/zakon.mod > /tmp/jtagger/hmm.out/judikatura.zakon.hmm"};
        process = Runtime.getRuntime().exec(cmd1);
        printProcessOutput(process);
        logger.debug("DONE");

        logger.debug("cat /tmp/jtagger/hmm.in/judikatura.rozhodnuti.hmm | " + path + "tagger/hunpos-tag " + path + "models/" + court + "/rozhodnuti.mod");
        String[] cmd2 = {"bash", "-c", "cat /tmp/jtagger/hmm.in/judikatura.rozhodnuti.hmm | " + path + "tagger/hunpos-tag " + path + "models/" + court + "/rozhodnuti.mod > /tmp/jtagger/hmm.out/judikatura.rozhodnuti.hmm"};
        process = Runtime.getRuntime().exec(cmd2);
        printProcessOutput(process);
        logger.debug("DONE");

        //
        // HMM -> VXML
        //

        logger.debug("\n\n***\nPrevod HMM -> VXML...\n***\n");

        logger.debug(perlIntro + "jtagger/hmm2vxml.pl /tmp/jtagger/hmm.out /tmp/jtagger/tagged /tmp/jtagger/empty/judikatura.zakon.vxml");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/hmm2vxml.pl /tmp/jtagger/hmm.out /tmp/jtagger/tagged /tmp/jtagger/empty/judikatura.zakon.vxml");
        printProcessOutput(process);
        logger.debug("DONE");

        logger.debug(perlIntro + "jtagger/hmm2vxml.pl /tmp/jtagger/hmm.out /tmp/jtagger/tagged /tmp/jtagger/empty/judikatura.rozhodnuti.vxml");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/hmm2vxml.pl /tmp/jtagger/hmm.out /tmp/jtagger/tagged /tmp/jtagger/empty/judikatura.rozhodnuti.vxml");
        printProcessOutput(process);
        logger.debug("DONE");

        //
        // Rulebased modul
        //

        logger.debug("\n\n***\nRulebased modul...\n***\n");

        logger.debug(perlIntro + "jtagger/rulebased_vxml_validation.pl /tmp/jtagger/rulebased /tmp/jtagger/tagged/judikatura.zakon.vxml");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/rulebased_vxml_validation.pl /tmp/jtagger/rulebased /tmp/jtagger/tagged/judikatura.zakon.vxml");
        printProcessOutput(process);
        logger.debug("DONE");

        logger.debug(perlIntro + "jtagger/rulebased_vxml_validation.pl /tmp/jtagger/rulebased /tmp/jtagger/tagged/judikatura.rozhodnuti.vxml");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/rulebased_vxml_validation.pl /tmp/jtagger/rulebased /tmp/jtagger/tagged/judikatura.rozhodnuti.vxml");
        printProcessOutput(process);
        logger.debug("DONE");

        logger.debug(perlIntro + "jtagger/rulebased_vxml_validation.pl /tmp/jtagger/rulebased /tmp/jtagger/tagged/judikatura.zkratky.vxml");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/rulebased_vxml_validation.pl /tmp/jtagger/rulebased /tmp/jtagger/tagged/judikatura.zkratky.vxml");
        printProcessOutput(process);
        logger.debug("DONE");

        //
        // Validation
        //

        logger.debug("\n\n***\nValidace VXML...\n***\n");

        logger.debug(perlIntro + "jtagger/vxml_validation.pl /tmp/jtagger/validated /tmp/jtagger/rulebased/judikatura.zakon.vxml");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/vxml_validation.pl /tmp/jtagger/validated /tmp/jtagger/rulebased/judikatura.zakon.vxml");
        printProcessOutput(process);
        logger.debug("DONE");

        logger.debug(perlIntro + "jtagger/vxml_validation.pl /tmp/jtagger/validated /tmp/jtagger/rulebased/judikatura.rozhodnuti.vxml");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/vxml_validation.pl /tmp/jtagger/validated /tmp/jtagger/rulebased/judikatura.rozhodnuti.vxml");
        printProcessOutput(process);
        logger.debug("DONE");

        logger.debug(perlIntro + "jtagger/vxml_validation.pl /tmp/jtagger/validated /tmp/jtagger/rulebased/judikatura.zkratky.vxml");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/vxml_validation.pl /tmp/jtagger/validated /tmp/jtagger/rulebased/judikatura.zkratky.vxml");
        printProcessOutput(process);
        logger.debug("DONE");

        //
        // Linking
        //

        logger.debug("\n\n***\nLinker...\n***\n");

        logger.debug(perlIntro + "jtagger/vxml_linker.pl /tmp/jtagger/linked /tmp/jtagger/validated/judikatura.zakon.vxml");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/vxml_linker.pl /tmp/jtagger/linked /tmp/jtagger/validated/judikatura.zakon.vxml");
        printProcessOutput(process);
        logger.debug("DONE");

        logger.debug(perlIntro + "jtagger/vxml_linker.pl /tmp/jtagger/linked /tmp/jtagger/validated/judikatura.rozhodnuti.vxml");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/vxml_linker.pl /tmp/jtagger/linked /tmp/jtagger/validated/judikatura.rozhodnuti.vxml");
        printProcessOutput(process);
        logger.debug("DONE");

        logger.debug(perlIntro + "jtagger/vxml_linker.pl /tmp/jtagger/linked /tmp/jtagger/validated/judikatura.zkratky.vxml");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/vxml_linker.pl /tmp/jtagger/linked /tmp/jtagger/validated/judikatura.zkratky.vxml");
        printProcessOutput(process);
        logger.debug("DONE");

        //
        // Merge
        //

        logger.debug("\n\n***\nMerge...\n***\n");

        logger.debug(perlIntro + "jtagger/merge_vxml_files.pl /tmp/jtagger/txt/judikatura.zkratky.txt /tmp/jtagger/merged/final.vxml /tmp/jtagger/linked/judikatura.zakon.vxml /tmp/jtagger/linked/judikatura.rozhodnuti.vxml /tmp/jtagger/linked/judikatura.zkratky.vxml");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/merge_vxml_files.pl /tmp/jtagger/txt/judikatura.zkratky.txt /tmp/jtagger/merged/final.vxml /tmp/jtagger/linked/judikatura.zakon.vxml /tmp/jtagger/linked/judikatura.rozhodnuti.vxml /tmp/jtagger/linked/judikatura.zkratky.vxml");
        printProcessOutput(process);
        logger.debug("DONE");

        //
        // Validation 2
        //

        logger.debug("\n\n***\nValidace VXML...\n***\n");

        logger.debug(perlIntro + "jtagger/vxml_validation.pl /tmp/jtagger/validated2 /tmp/jtagger/merged/final.vxml");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/vxml_validation.pl /tmp/jtagger/validated2 /tmp/jtagger/merged/final.vxml");
        printProcessOutput(process);
        logger.debug("DONE");

        //
        // Linked Z
        //

        logger.debug("\n\n***\nZ-Linker...\n***\n");

        logger.debug(perlIntro + "jtagger/vxml_linkerZ.pl /tmp/jtagger/linkedZ /tmp/jtagger/validated2/final.vxml");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/vxml_linkerZ.pl /tmp/jtagger/linkedZ /tmp/jtagger/validated2/final.vxml");
        printProcessOutput(process);
        logger.debug("DONE");

        //
        // VXML -> XML
        //

        logger.debug("\n\n***\nPrevod VXML -> XML...\n***\n");

        logger.debug(perlIntro + "jtagger/vxml2xml.pl /tmp/jtagger/ /tmp/jtagger/linkedZ/final.vxml");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/vxml2xml.pl /tmp/jtagger/ /tmp/jtagger/linkedZ/final.vxml");
        printProcessOutput(process);
        logger.debug("DONE");

        //
        // VXML -> HTML
        //

        logger.debug("\n\n***\nPrevod VXML -> HTML...\n***\n");

        logger.debug(perlIntro + "jtagger/vxml2html.pl /tmp/jtagger/ /tmp/jtagger/final.vxml");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/vxml2html.pl /tmp/jtagger/ /tmp/jtagger/final.vxml");
        printProcessOutput(process);
        logger.debug("DONE");

        // Nacitam vysledok do stringu a vratim
        logger.debug("\n\n***\nLoading vyslednych suborov...\n***\n");
        logger.debug("/tmp/jtagger/final.xml");
        FileInputStream fis = new FileInputStream("/tmp/jtagger/final.xml"); 
        InputStreamReader inp = new InputStreamReader(fis, "UTF-8");
        BufferedReader reader = new BufferedReader(inp);
        String output_xml = "";
        String line = null;
        while ((line = reader.readLine()) != null) {
            output_xml += line + "\n";
        }
        logger.debug("DONE");

        logger.debug("/tmp/jtagger/final.html");
        FileInputStream fis2 = new FileInputStream("/tmp/jtagger/final.html"); 
        InputStreamReader inp2 = new InputStreamReader(fis2, "UTF-8");
        BufferedReader reader2 = new BufferedReader(inp2);
        String output_html = "";
        while ((line = reader2.readLine()) != null) {
            output_html += line + "\n";
        }
        logger.debug("DONE");

        logger.info("Hotovo :-)");
        JTaggerResult output = new JTaggerResult();
        output.setHtml(output_html);
        output.setXml(output_xml);

        return output;
    }

    private static void printProcessOutput(Process process) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line = "";
            while ((line = in.readLine()) != null) {
              logger.debug(line);
            }
            in.close();

            in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            line = "";
            while ((line = in.readLine()) != null) {
              logger.debug(line);
            }
            in.close();
        }
        catch (Exception e) {
            logger.debug("Vynimka... " + e);
        }
    }
}