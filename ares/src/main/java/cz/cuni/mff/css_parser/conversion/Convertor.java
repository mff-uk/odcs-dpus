package cz.cuni.mff.css_parser.conversion;

import cz.mff.cuni.scraper.lib.selector.Selector;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jakub Starka
 */
public class Convertor {

    public static String getStreet(Selector id) {
	if (id == null || id.getValue() == null || id.getValue().equals("")) {
	    return null;
	} else {
	    String street = id.getValue();
            if (street.contains(",")) {
                String[] parts = street.split(",");
                for (String part: parts) {
                    if (part.matches(".* [0-9]+")) {
                        return part;
                    }
                }
            }
            return street;
	}
    }
    
    public static URL getHttp(Selector url) {
        if (url == null || url.getValue() == null || url.getValue().isEmpty()) {
            return null;
        }
        String urlAdd = url.getValue();
        if (urlAdd.startsWith("http://") || urlAdd.startsWith("https://")) {
            try {
                return new URL(urlAdd);
            } catch (MalformedURLException ex) {
                Logger.getLogger(Convertor.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                return new URL("http://" + urlAdd);
            } catch (MalformedURLException ex) {
                Logger.getLogger(Convertor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }
    
    public static String getDatetime(Selector date, Selector time) {
        String dateValue = getDate(date);
        if (dateValue == null) {
            return dateValue;
        }
        
        String timeValue = time.getValue();
        if (timeValue == null || timeValue.equals("")) {
            return dateValue + "T00:00:00";
        } else {
            return dateValue + "T" + timeValue + ":00";
        }
    }
    
    public static URL getEmail(Selector mail) throws MalformedURLException {
        if (mail == null || mail.getValue() == null || mail.getValue().equals("")) {
	    return null;
	}
        String m = mail.getValue();
        return new URL("mailto:" + m);
    }
    
    public static String getPrice(Selector price) {
        if (price == null || price.getValue() == null || price.getValue().equals("")) {
	    return null;
	}
        return price.getValue().replace(" ", "").replace(",", ".");
    }
    
    public static String getDuration(Selector months, Selector days, Selector years) {
        String str1 = null;
        String str2 = null;
        String str3 = null;
        if (months != null) { str1 = months.getValue(); }
        if (days!= null)    { str2 = days.getValue(); }
        if (years!= null)    { str3 = years.getValue(); }
        if (str1 != null && !str1.isEmpty()) {
            return "P" + str1 + "M";
        } else if (str2 != null && !str2.isEmpty()) {
            return "P" + str2 + "D";
        } else if (str3 != null && !str3.isEmpty()) {
            return "P" + str3 + "Y";
        } else {
            return null;
        }
    }

    public static String getDate(String dValue) {
	String[] parts = dValue.split("/");
	if (parts.length != 3) {
	    parts = dValue.split("\\.");
	    if (parts.length != 3) {
                if (dValue.length() == 8) {
                    parts = new String[3];
                    parts[0] = dValue.substring(0, 2);
                    parts[1] = dValue.substring(2, 4);
                    parts[2] = dValue.substring(4);
                } else {
                    System.out.println("Unexpected date: " + dValue);
                    return null;
                }
	    }
	}
	if (parts[1].length() == 1) {
	    parts[1] = '0' + parts[1];
	}

	if (parts[0].length() == 1) {
	    parts[0] = '0' + parts[0];
	}
	return parts[2] + "-" + parts[1] + "-" + parts[0];
    }
    
    public static String getDate(Selector date) {
	if (date == null || date.getValue() == null || date.getValue().equals("")) {
	    return null;
	}
        String dValue = date.getValue();
        return getDate(dValue);
    }
    
}
