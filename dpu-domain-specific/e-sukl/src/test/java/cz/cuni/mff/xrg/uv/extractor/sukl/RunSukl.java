package cz.cuni.mff.xrg.uv.extractor.sukl;

import java.io.File;
import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

/**
 *
 * @author Škoda Petr
 */
public class RunSukl {

    @Test
    public void getEffectiveSubstance() throws IOException {
        final Document doc = Jsoup.parse(new File("D:/Temp/03/0011024.htm"),
                null, "view-source:www.sukl.cz");

        final Elements elements = doc.select("div#medicine-box>table.zebra.vertical tbody tr");
        for (Element element : elements) {
            if (element.getElementsByTag("th").first().text().compareTo(
                    "Účinná látka") == 0) {
                final String val = element.getElementsByTag("td").first().html();
                if (val.length() < 2) {
                    // this is just an empty string, we skip this
                    return;
                }

                final String[] substances = val.split("<br />");
                for (String substance : substances) {
                    substance = substance.trim();
                    // get names
                    final String nameCs = substance.substring(0, substance.indexOf("("));
                    final String nameLa = substance.substring(substance.indexOf("(") + 1,
                            substance.indexOf(")"));
                    // add
                    System.out.println("----------");
                    System.out.println("\t" + nameCs);
                    System.out.println("\t" + nameLa);
                }

            }
        }

    }

}
