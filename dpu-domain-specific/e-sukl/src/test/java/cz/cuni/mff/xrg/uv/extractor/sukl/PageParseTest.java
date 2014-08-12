package cz.cuni.mff.xrg.uv.extractor.sukl;

import java.io.File;
import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Škoda Petr
 */
public class PageParseTest {

    private static final Logger LOG = LoggerFactory.getLogger(
            PageParseTest.class);

    @Test
    public void name() throws IOException {

        Document document = Jsoup.parse(new File("d:/Temp/08/0195347.htm"),
                null, "view-source:www.sukl.cz");

        String selector;
        // spaces in class are replaced with dost
        String string = "div#medicine-box>table.zebra.vertical tbody tr";
        String attribute;

        if (string.contains("@")) {
            String[] split = string.split("@");
            selector = split[0].trim();
            attribute = split[1];
        } else {
            selector = string;
        }

        Elements elements = document.select(selector);
        for (Element element : elements) {
            if (element.getElementsByTag("th").first().text().compareTo(
                    "Účinná látka") == 0) {
                String value = element.getElementsByTag("td").first().text();

                String nameCz = value.substring(0, value.indexOf("("));
                String nameLa = value.substring(value.indexOf("(") + 1,
                        value.indexOf(")"));

                LOG.debug("{} - {}", nameCz, nameLa);
            }
        }
    }

    @Test
    public void documents() throws IOException {

        Document document = Jsoup.parse(new File("d:/Temp/08/0195347-documents.htm"),
                null, "view-source:www.sukl.cz");

        String selector;
        // spaces in class are replaced with dost
        String string = "div#medicine-box>table.zebra.vertical tbody tr";
        String attribute;

        if (string.contains("@")) {
            String[] split = string.split("@");
            selector = split[0].trim();
            attribute = split[1];
        } else {
            selector = string;
        }

        Elements elements = document.select(selector);
        for (Element element : elements) {
            Elements withHref = element.select("td a");

            final String name = element.getElementsByTag("th").first().text();
            final String value = !withHref.isEmpty() ?
                    withHref.first().attr("abs:href") : "";

            LOG.info("{} > {}", name, value);
        }
    }

}
