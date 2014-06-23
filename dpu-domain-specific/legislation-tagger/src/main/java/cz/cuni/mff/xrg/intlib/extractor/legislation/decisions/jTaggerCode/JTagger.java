package cz.cuni.mff.xrg.intlib.extractor.legislation.decisions.jTaggerCode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author vinc
 */
public class JTagger {
     
    private static final Logger logger = LoggerFactory.getLogger(JTagger.class);

    
    //private static String path = "src/main/resources/";
    private String path;
    private final String pathToRes = "src/main/resources/";
    private  List<String> dirs = new ArrayList<>();
    private  String pathToWorkDir;

    public void setPath(String apath) {
        
        path = apath + File.separator + pathToRes;
        logger.debug("Path for jTagger's resources: {}", path);
    }
    
    public void setWorkingDir(String pathToWorkingDir) {
        
        pathToWorkDir = pathToWorkingDir;
        
         dirs.add(pathToWorkingDir + "/jtagger");
 dirs.add(pathToWorkingDir + "/jtagger/txt_source");
 dirs.add(pathToWorkingDir + "/jtagger/txt");
  dirs.add(pathToWorkingDir + "/jtagger/empty");
   dirs.add(pathToWorkingDir + "/jtagger/hmm.in");
    dirs.add(pathToWorkingDir + "/jtagger/hmm.out");
     dirs.add(pathToWorkingDir + "/jtagger/tagged");
      dirs.add(pathToWorkingDir + "/jtagger/rulebased");
       dirs.add(pathToWorkingDir + "/jtagger/validated");
        dirs.add(pathToWorkingDir + "/jtagger/linked");
         dirs.add(pathToWorkingDir + "/jtagger/merged");
          dirs.add(pathToWorkingDir + "/jtagger/validated2");
                 dirs.add(pathToWorkingDir + "/jtagger/linkedZ");
        

        
    }
    
      public JTaggerResult processFile(String text, String court) throws Exception {
        // 
        // Zistim, ci vstupny text obsahuje <metadata>. 
        // Ak ano, oddelim ich od zvysku textu...
        // Ak ne, postupujem dalej
        //
        String[] texts = text.split("<\\/metadata>");
        System.out.println("===========================================\n");
        System.out.println("Pocet textov na analyzu = " + texts.length + "\n");
        System.out.println("===========================================\n\n");

        // Text neobsahuje XML kod, cely text spracujem narazS
        if (texts.length == 1) {
            return processText(text, court);
        }

        // Text obsahuje XML kod, spracujem ho oddelene
        if (texts.length == 2) {
            System.out.println("===========================================\n");
            System.out.println("TEXT JEDNA = DECISION\n");
            System.out.println("===========================================\n\n");

            System.out.println(texts[1] + "\n");
            JTaggerResult decision = processText(texts[1], court);

            System.out.println("===========================================\n");
            System.out.println("TEXT DVA   = METADATA\n");
            System.out.println("===========================================\n\n");

            System.out.println(texts[0] + "\n");
            String escape_text0 = texts[0].replace("<metadata>", "");
            escape_text0 = escape_text0.replace("<", "\n<");
            escape_text0 = escape_text0.replace(">", ">\n ");
            escape_text0 = escape_text0.replace("&", "&amp;");
            escape_text0 = escape_text0.replace("<", "&lt;");
            escape_text0 = escape_text0.replace(">", "&gt;");
            escape_text0 = escape_text0.replace("\"", "&quot;");
            escape_text0 = escape_text0.replace("\'", "&apos;");

            System.out.println("Upraveny text = \n");
            System.out.println(escape_text0 + "\n");
            JTaggerResult metadata = processText(escape_text0, court);
       
            //adjust metadata XML - again convert escaped entities back to their original chars
            String xmlEscaped = metadata.getXml();
            String xmlWithoutEscaped = xmlEscaped.replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&quot;", "\"").replaceAll("&apos;", "\'");
            metadata.setXml(xmlWithoutEscaped);       
       
      
         
            System.out.println("===========================================\n");
            System.out.println("METADATA\n");
            System.out.println("===========================================\n\n");

            String[] md_lines = metadata.getXml().split("\\n");
            String md_body = "";
            boolean in_body = false;
            for (int i = 0; i < md_lines.length; i++) {
                System.out.println(i + "\t" + md_lines[i]);

                if (md_lines[i].matches("^\\s*<body>\\s*$")) {
                    in_body = true;
                    continue;
                }

                if (md_lines[i].matches("^\\s*<\\/body>\\s*$")) {
                    break;
                }

                if (in_body) {
                    md_body += md_lines[i] + "\n";
                }
            }

            System.out.println("===========================================\n");
            System.out.println("DOCUMENT\n");
            System.out.println("===========================================\n\n");

            String[] lines = decision.getXml().split("\\n");
            for (int i = 0; i < lines.length; i++) {
                System.out.println(i + "\t" + lines[i]);
            }

            // TODO - metadata vlozit do XML dokumentu namiesto povodnych metadat...
            String output = "";
            boolean in_metadata = false;
            for (int i = 0; i < lines.length; i++) {
                System.out.println(i + "\t" + lines[i]);

                if (lines[i].matches("^\\s*<metadata>")) {
                    in_metadata = true;
                    output += lines[i] + "\n";
                    output += md_body + "\n";
                    continue;
                }

                if (lines[i].matches("^\\s*<\\/metadata>")) {
                    in_metadata = false;
                    output += lines[i] + "\n";
                    continue;
                }

                if (in_metadata) {
                    continue;
                }

                output += lines[i] + "\n";
            }

            System.out.println("*** VYSLEDOK: ***\n\n");
            System.out.println(output);
            System.out.println("*** KONIEC VYSLEDKU ***\n\n");

            decision.setXml(output);
            return decision;
        }

        // Toto je chyba
        logger.error("Error if we get here");
        return null;
    }
   
    public JTaggerResult processText(String text, String court) throws Exception {
        Process process;
         logger.debug("Running annotator");
        // Ak neexistuje, zalozim si adresarovu strukturu
        for (int i = 0; i < dirs.size(); i++) {
            File handler = new File(dirs.get(i));
            if (!handler.isDirectory()) {
                handler.mkdir();
            }
            else {
                for (File file: handler.listFiles()) {
                    file.delete();
                }
            }
        }
        

        //
        // Ulozim zdrojove TXT
        //

        logger.debug("\n\n***\nUkadam zdrojove subory na disk...\n***\n");

        logger.debug(""+ pathToWorkDir + "/jtagger/txt_source/judikatura.zakon.txt");
        PrintWriter writer = null;
        writer = new PrintWriter(pathToWorkDir + "/jtagger/txt_source/judikatura.zakon.txt", "UTF-8");
        writer.println(text);
        writer.close();

        logger.debug(""+ pathToWorkDir + "/jtagger/txt_source/judikatura.rozhodnuti.txt");
        writer = new PrintWriter(pathToWorkDir + "/jtagger/txt_source/judikatura.rozhodnuti.txt", "UTF-8");
        writer.println(text);
        writer.close();

        logger.debug(""+ pathToWorkDir + "/jtagger/txt_source/judikatura.zkratky.txt");
        writer = new PrintWriter(pathToWorkDir + "/jtagger/txt_source/judikatura.zkratky.txt", "UTF-8");
        writer.println(text);
        writer.close();

        
        String perlIntro = "perl -I "+ path +  " " + path;
        //
        // Tokenizacia
        //

        logger.debug("\n\n***\nTokenizacia\n***\n");

        logger.debug(perlIntro + "jtagger/txt2vxml.pl "+ pathToWorkDir + "/jtagger/txt "+ pathToWorkDir + "/jtagger/txt_source/judikatura.zakon.txt");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/txt_tokenization.pl "+ pathToWorkDir + "/jtagger/txt "+ pathToWorkDir + "/jtagger/txt_source/judikatura.zakon.txt");
               //process = Runtime.getRuntime().exec("perl -I "+ path +  " " + path + "jtagger/txt_tokenization.pl /tmp/jtagger/txt /tmp/jtagger/txt_source/judikatura.zakon.txt");

        printProcessOutput(process);
        
        logger.debug(perlIntro + "jtagger/txt2vxml.pl "+ pathToWorkDir + "/jtagger/txt "+ pathToWorkDir + "/jtagger/txt_source/judikatura.rozhodnuti.txt");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/txt_tokenization.pl "+ pathToWorkDir + "/jtagger/txt "+ pathToWorkDir + "/jtagger/txt_source/judikatura.rozhodnuti.txt");
        printProcessOutput(process);

        logger.debug(perlIntro + "jtagger/txt2vxml.pl "+ pathToWorkDir + "/jtagger/txt "+ pathToWorkDir + "/jtagger/txt_source/judikatura.zkratky.txt");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/txt_tokenization.pl "+ pathToWorkDir + "/jtagger/txt "+ pathToWorkDir + "/jtagger/txt_source/judikatura.zkratky.txt");
        printProcessOutput(process);
        
        //
        // Prevod TXT -> VXML
        //

        logger.debug("\n\n***\nPrevod TXT -> VXML...\n***\n");

        logger.debug(perlIntro + "jtagger/txt2vxml.pl "+ pathToWorkDir + "/jtagger/empty "+ pathToWorkDir + "/jtagger/txt/judikatura.zakon.txt");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/txt2vxml.pl "+ pathToWorkDir + "/jtagger/empty "+ pathToWorkDir + "/jtagger/txt/judikatura.zakon.txt");
        printProcessOutput(process);
        
        logger.debug(perlIntro + "jtagger/txt2vxml.pl "+ pathToWorkDir + "/jtagger/empty "+ pathToWorkDir + "/jtagger/txt/judikatura.rozhodnuti.txt");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/txt2vxml.pl "+ pathToWorkDir + "/jtagger/empty "+ pathToWorkDir + "/jtagger/txt/judikatura.rozhodnuti.txt");
        printProcessOutput(process);

        logger.debug(perlIntro + "jtagger/txt2vxml.pl "+ pathToWorkDir + "/jtagger/empty "+ pathToWorkDir + "/jtagger/txt/judikatura.zkratky.txt");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/txt2vxml.pl "+ pathToWorkDir + "/jtagger/empty "+ pathToWorkDir + "/jtagger/txt/judikatura.zkratky.txt");
        printProcessOutput(process);

        //
        // Prevod VXML -> HMM
        //

        logger.debug("\n\n***\nPrevod VXML -> HMM...\n***\n");
        
        logger.debug(perlIntro + "jtagger/vxml2hmm.pl "+ pathToWorkDir + "/jtagger/hmm.in test Zakon "+ pathToWorkDir + "/jtagger/empty/judikatura.zakon.vxml");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/vxml2hmm.pl "+ pathToWorkDir + "/jtagger/hmm.in test Zakon "+ pathToWorkDir + "/jtagger/empty/judikatura.zakon.vxml");
        printProcessOutput(process);
        
        logger.debug(perlIntro + "jtagger/vxml2hmm.pl "+ pathToWorkDir + "/jtagger/hmm.in test Zakon "+ pathToWorkDir + "/jtagger/empty/judikatura.rozhodnuti.vxml");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/vxml2hmm.pl "+ pathToWorkDir + "/jtagger/hmm.in test Zakon "+ pathToWorkDir + "/jtagger/empty/judikatura.rozhodnuti.vxml");
        printProcessOutput(process);

        logger.debug(perlIntro + "jtagger/vxml2hmm.pl "+ pathToWorkDir + "/jtagger/hmm.in test Zakon "+ pathToWorkDir + "/jtagger/empty/judikatura.zkratky.vxml");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/vxml2hmm.pl "+ pathToWorkDir + "/jtagger/hmm.in test Zakon "+ pathToWorkDir + "/jtagger/empty/judikatura.zkratky.vxml");
        printProcessOutput(process);

        //
        // Binarka HunPOS
        //

        logger.debug("\n\n***\nVolanie taggeru...\n***\n");

        logger.debug("cat "+ pathToWorkDir + "/jtagger/hmm.in/judikatura.zakon.hmm | " + path + "tagger/hunpos-tag " + path + "models/" + court + "/zakon.mod");
        String[] cmd1 = {"bash", "-c", "cat "+ pathToWorkDir + "/jtagger/hmm.in/judikatura.zakon.hmm | " + path + "tagger/hunpos-tag " + path + "models/" + court + "/zakon.mod > "+ pathToWorkDir + "/jtagger/hmm.out/judikatura.zakon.hmm"};
        process = Runtime.getRuntime().exec(cmd1);
        printProcessOutput(process);

        logger.debug("cat "+ pathToWorkDir + "/jtagger/hmm.in/judikatura.rozhodnuti.hmm | " + path + "tagger/hunpos-tag " + path + "models/" + court + "/rozhodnuti.mod");
        String[] cmd2 = {"bash", "-c", "cat "+ pathToWorkDir + "/jtagger/hmm.in/judikatura.rozhodnuti.hmm | " + path + "tagger/hunpos-tag " + path + "models/" + court + "/rozhodnuti.mod > "+ pathToWorkDir + "/jtagger/hmm.out/judikatura.rozhodnuti.hmm"};
        process = Runtime.getRuntime().exec(cmd2);
        printProcessOutput(process);

        //
        // HMM -> VXML
        //

        logger.debug("\n\n***\nPrevod HMM -> VXML...\n***\n");

        logger.debug(perlIntro + "jtagger/hmm2vxml.pl "+ pathToWorkDir + "/jtagger/hmm.out "+ pathToWorkDir + "/jtagger/tagged "+ pathToWorkDir + "/jtagger/empty/judikatura.zakon.vxml");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/hmm2vxml.pl "+ pathToWorkDir + "/jtagger/hmm.out "+ pathToWorkDir + "/jtagger/tagged "+ pathToWorkDir + "/jtagger/empty/judikatura.zakon.vxml");
        printProcessOutput(process);

        logger.debug(perlIntro + "jtagger/hmm2vxml.pl "+ pathToWorkDir + "/jtagger/hmm.out "+ pathToWorkDir + "/jtagger/tagged "+ pathToWorkDir + "/jtagger/empty/judikatura.rozhodnuti.vxml");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/hmm2vxml.pl "+ pathToWorkDir + "/jtagger/hmm.out "+ pathToWorkDir + "/jtagger/tagged "+ pathToWorkDir + "/jtagger/empty/judikatura.rozhodnuti.vxml");
        printProcessOutput(process);

        //
        // Rulebased modul
        //

        logger.debug("\n\n***\nRulebased modul...\n***\n");

        logger.debug(perlIntro + "jtagger/rulebased_vxml_validation.pl "+ pathToWorkDir + "/jtagger/rulebased "+ pathToWorkDir + "/jtagger/tagged/judikatura.zakon.vxml");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/rulebased_vxml_validation.pl "+ pathToWorkDir + "/jtagger/rulebased "+ pathToWorkDir + "/jtagger/tagged/judikatura.zakon.vxml");
        printProcessOutput(process);

        logger.debug(perlIntro + "jtagger/rulebased_vxml_validation.pl "+ pathToWorkDir + "/jtagger/rulebased "+ pathToWorkDir + "/jtagger/tagged/judikatura.rozhodnuti.vxml");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/rulebased_vxml_validation.pl "+ pathToWorkDir + "/jtagger/rulebased "+ pathToWorkDir + "/jtagger/tagged/judikatura.rozhodnuti.vxml");
        printProcessOutput(process);

        logger.debug(perlIntro + "jtagger/rulebased_vxml_validation.pl "+ pathToWorkDir + "/jtagger/rulebased "+ pathToWorkDir + "/jtagger/tagged/judikatura.zkratky.vxml");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/rulebased_vxml_validation.pl "+ pathToWorkDir + "/jtagger/rulebased "+ pathToWorkDir + "/jtagger/tagged/judikatura.zkratky.vxml");
        printProcessOutput(process);

        //
        // Validation
        //

        logger.debug("\n\n***\nValidace VXML...\n***\n");

        logger.debug(perlIntro + "jtagger/vxml_validation.pl "+ pathToWorkDir + "/jtagger/validated "+ pathToWorkDir + "/jtagger/rulebased/judikatura.zakon.vxml");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/vxml_validation.pl "+ pathToWorkDir + "/jtagger/validated "+ pathToWorkDir + "/jtagger/rulebased/judikatura.zakon.vxml");
        printProcessOutput(process);

        logger.debug(perlIntro + "jtagger/vxml_validation.pl "+ pathToWorkDir + "/jtagger/validated "+ pathToWorkDir + "/jtagger/rulebased/judikatura.rozhodnuti.vxml");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/vxml_validation.pl "+ pathToWorkDir + "/jtagger/validated "+ pathToWorkDir + "/jtagger/rulebased/judikatura.rozhodnuti.vxml");
        printProcessOutput(process);

        logger.debug(perlIntro + "jtagger/vxml_validation.pl "+ pathToWorkDir + "/jtagger/validated "+ pathToWorkDir + "/jtagger/rulebased/judikatura.zkratky.vxml");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/vxml_validation.pl "+ pathToWorkDir + "/jtagger/validated "+ pathToWorkDir + "/jtagger/rulebased/judikatura.zkratky.vxml");
        printProcessOutput(process);

        //
        // Linking
        //

        logger.debug("\n\n***\nLinker...\n***\n");

        logger.debug(perlIntro + "jtagger/vxml_linker.pl "+ pathToWorkDir + "/jtagger/linked "+ pathToWorkDir + "/jtagger/validated/judikatura.zakon.vxml");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/vxml_linker.pl "+ pathToWorkDir + "/jtagger/linked "+ pathToWorkDir + "/jtagger/validated/judikatura.zakon.vxml");
        printProcessOutput(process);

        logger.debug(perlIntro + "jtagger/vxml_linker.pl "+ pathToWorkDir + "/jtagger/linked "+ pathToWorkDir + "/jtagger/validated/judikatura.rozhodnuti.vxml");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/vxml_linker.pl "+ pathToWorkDir + "/jtagger/linked "+ pathToWorkDir + "/jtagger/validated/judikatura.rozhodnuti.vxml");
        printProcessOutput(process);

        logger.debug(perlIntro + "jtagger/vxml_linker.pl "+ pathToWorkDir + "/jtagger/linked "+ pathToWorkDir + "/jtagger/validated/judikatura.zkratky.vxml");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/vxml_linker.pl "+ pathToWorkDir + "/jtagger/linked "+ pathToWorkDir + "/jtagger/validated/judikatura.zkratky.vxml");
        printProcessOutput(process);

        //
        // Merge
        //

        logger.debug("\n\n***\nMerge...\n***\n");

        logger.debug(perlIntro + "jtagger/merge_vxml_files.pl "+ pathToWorkDir + "/jtagger/txt/judikatura.zkratky.txt "+ pathToWorkDir + "/jtagger/merged/final.vxml "+ pathToWorkDir + "/jtagger/linked/judikatura.zakon.vxml "+ pathToWorkDir + "/jtagger/linked/judikatura.rozhodnuti.vxml "+ pathToWorkDir + "/jtagger/linked/judikatura.zkratky.vxml");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/merge_vxml_files.pl "+ pathToWorkDir + "/jtagger/txt/judikatura.zkratky.txt "+ pathToWorkDir + "/jtagger/merged/final.vxml "+ pathToWorkDir + "/jtagger/linked/judikatura.zakon.vxml "+ pathToWorkDir + "/jtagger/linked/judikatura.rozhodnuti.vxml "+ pathToWorkDir + "/jtagger/linked/judikatura.zkratky.vxml");
        printProcessOutput(process);

        //
        // Validation 2
        //

        logger.debug("\n\n***\nValidace VXML...\n***\n");

        logger.debug(perlIntro + "jtagger/vxml_validation.pl "+ pathToWorkDir + "/jtagger/validated2 "+ pathToWorkDir + "/jtagger/merged/final.vxml");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/vxml_validation.pl "+ pathToWorkDir + "/jtagger/validated2 "+ pathToWorkDir + "/jtagger/merged/final.vxml");
        printProcessOutput(process);

        //
        // Linked Z
        //

        logger.debug("\n\n***\nZ-Linker...\n***\n");

        logger.debug(perlIntro + "jtagger/vxml_linkerZ.pl "+ pathToWorkDir + "/jtagger/linkedZ "+ pathToWorkDir + "/jtagger/validated2/final.vxml");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/vxml_linkerZ.pl "+ pathToWorkDir + "/jtagger/linkedZ "+ pathToWorkDir + "/jtagger/validated2/final.vxml");
        printProcessOutput(process);

        //
        // VXML -> XML
        //

        logger.debug("\n\n***\nPrevod VXML -> XML...\n***\n");

        logger.debug(perlIntro + "jtagger/vxml2xml.pl "+ pathToWorkDir + "/jtagger/ "+ pathToWorkDir + "/jtagger/linkedZ/final.vxml");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/vxml2xml.pl "+ pathToWorkDir + "/jtagger/ "+ pathToWorkDir + "/jtagger/linkedZ/final.vxml");
        printProcessOutput(process);

        //
        // VXML -> HTML
        //

        logger.debug("\n\n***\nPrevod VXML -> HTML...\n***\n");

        logger.debug(perlIntro + "jtagger/vxml2html.pl "+ pathToWorkDir + "/jtagger/ "+ pathToWorkDir + "/jtagger/final.vxml");
        process = Runtime.getRuntime().exec(perlIntro + "jtagger/vxml2html.pl "+ pathToWorkDir + "/jtagger/ "+ pathToWorkDir + "/jtagger/final.vxml");
        printProcessOutput(process);

        // Nacitam vysledok do stringu a vratim
        logger.debug("\n\n***\nLoading vyslednych suborov...\n***\n");
        logger.debug(pathToWorkDir + "/jtagger/final.xml");
        FileInputStream fis = new FileInputStream(""+ pathToWorkDir + "/jtagger/final.xml"); 
        InputStreamReader inp = new InputStreamReader(fis, "UTF-8");
        BufferedReader reader = new BufferedReader(inp);
        String output_xml = "";
        String line = null;
        while ((line = reader.readLine()) != null) {
            output_xml += line + "\n";
        }

        logger.debug(pathToWorkDir + "/jtagger/final.html");
        FileInputStream fis2 = new FileInputStream(""+ pathToWorkDir + "/jtagger/final.html"); 
        InputStreamReader inp2 = new InputStreamReader(fis2, "UTF-8");
        BufferedReader reader2 = new BufferedReader(inp2);
        String output_html = "";
        while ((line = reader2.readLine()) != null) {
            output_html += line + "\n";
        }

        JTaggerResult output = new JTaggerResult();
        output.setHtml(output_html);
        output.setXml(output_xml);

        return output;
    }

    private void printProcessOutput(Process process) {
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
            logger.debug("Vyjimka... " + e);
        }
    }

  
}