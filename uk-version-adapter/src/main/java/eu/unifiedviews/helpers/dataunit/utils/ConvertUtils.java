package eu.unifiedviews.helpers.dataunit.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ConvertUtils {
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    public static String dateToString(Date date) {
        DateFormat df = new SimpleDateFormat(DATE_FORMAT);
        if (date != null) {
            return df.format(date);
        } else {
            return null;
        }
    }

    public static Date stringToDate(String strDate) throws ParseException {
        DateFormat df = new SimpleDateFormat(DATE_FORMAT);
        Date result = null;
        if (!isBlank(strDate)) {
            result = df.parse(strDate);
        }
        return result;
    }

    public static boolean isBlank(CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((Character.isWhitespace(cs.charAt(i)) == false)) {
                return false;
            }
        }
        return true;
    }
}
