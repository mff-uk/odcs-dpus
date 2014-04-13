package cz.opendata.linked.geocoder.nominatim;

import org.apache.commons.lang3.StringUtils;
import org.openrdf.model.*;
import org.openrdf.model.impl.URIImpl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Address {

    private final ExtractorConfig config;
    private String street;
    private String city;
    private String region;
    private String country;
    private String postalCode;
    
    private static URI streetAddressURI = new URIImpl("http://schema.org/streetAddress");
    private static URI addressRegionURI = new URIImpl("http://schema.org/addressRegion");
    private static URI addressLocalityURI = new URIImpl("http://schema.org/addressLocality");
    private static URI postalCodeURI = new URIImpl("http://schema.org/postalCode");

    public Address(ExtractorConfig config, String street, String city, String region, String country, String postalCode) {
        this.config = config;

        if (street == null || !config.isUseStreet()) {
            street = "";
        }
        if (city == null || !config.isUseLocality()) {
            city = "";
        } else if (config.isStripNumFromLocality()) {
            city = stripNumbersFromCity(city);
        }
        if (region == null || !config.isUseRegion()) {
            region = "";
        }
        if (country == null) {
            country = "";
        }
        if (postalCode == null || !config.isUsePostalCode()) {
            postalCode = "";
        }
        this.street = street;
        this.city = city;
        this.region = region;
        this.country = country;
        this.postalCode = postalCode;
    }

    private String stripNumbersFromCity(String city) {
        return city.replaceAll("([\\s\\W][\\dIVX]+[\\s\\W]|[\\s\\W][\\dIVX]+$)",  " ");
    }

    public static Address buildFromRdf(ExtractorConfig config, Graph graph, Resource address) {

        String street = getAddressPart(address, streetAddressURI, graph);
        String city = getAddressPart(address, addressLocalityURI, graph);
        String region = getAddressPart(address, addressRegionURI, graph);
        String postalCode = getAddressPart(address, postalCodeURI, graph);

        return new Address(config, street, city, region, config.getCountry(), postalCode);
    }

    private static String getAddressPart(Resource address, URI currentPropertyURI, Graph graph) {
        Iterator<Statement> it1 = graph.match(address, currentPropertyURI, null);
        String currentValueString = null;
        if (it1.hasNext())
        {
            Value currentValue = it1.next().getObject();
            if (currentValue != null)
            {
                currentValueString = currentValue.stringValue();
            }
        }
        return currentValueString;
    }

    public String toString() {
        return toString(", ");
    }
    public String toString(String separator) {
        return StringUtils.join(getParts(), separator);
    }

    public String toFilename() {
        String filename = toString().replaceAll("\\s", "_").replaceAll("[^\\d\\p{L}_]", "-");
        if (config.isStructured()) {
            filename = "structured-" + filename;
        }
        return filename;
    }

    private List<String> getParts() {
        List parts = new LinkedList<String>();
        if (street != "") {
            parts.add(street);
        }
        if (city != "") {
            parts.add(city);
        }
        if (region != "") {
            parts.add(region);
        }
        if (country != "") {
            parts.add(country);
        }
        if (postalCode != "") {
            parts.add(postalCode);
        }
        return parts;
    }

    public String buildQuery() throws UnsupportedEncodingException {
        String url = "http://nominatim.openstreetmap.org/search?format=json&";
        if (config.isStructured()) {
            url += "&street=" + encodeForUrl(street);
            url += "&city=" + encodeForUrl(city);
            url += "&county=" + encodeForUrl(region);
            url += "&state=" + encodeForUrl(country);
            url += "&postalcode=" + encodeForUrl(postalCode);
        }
        else {
            url += "&q=" + encodeForUrl(toString(" "));
        }
        return url;
    }

    private String encodeForUrl(String value) throws UnsupportedEncodingException {
        if (value == null) {
            return "";
        } else {
            return URLEncoder.encode(value, "UTF-8");
        }
    }

    public Address getAlternative() {
        Address alternative = null;
        if (cityIsSubstringOfStreet() || streetIsOnlyNumeric()) {
            alternative = new Address(config, city, city, region, country, postalCode);
        }
        return alternative;
    }

    private boolean cityIsSubstringOfStreet() {
        return street != city && (street.matches("^\\d+\\w?(/\\d+\\w?)? " + city) || street.matches(city + " \\d+"));
    }

    private boolean streetIsOnlyNumeric() {
        return street.trim().matches("^\\d+$");
    }
}
