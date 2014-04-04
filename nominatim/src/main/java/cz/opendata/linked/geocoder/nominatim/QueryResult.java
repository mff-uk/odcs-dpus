package cz.opendata.linked.geocoder.nominatim;

import java.math.BigDecimal;

public class QueryResult {

    private int length = 0;
    private BigDecimal latitude = new BigDecimal(0);
    private BigDecimal longitude = new BigDecimal(0);

    public QueryResult(BigDecimal lat, BigDecimal lon, int length) {
        this.latitude = lat;
        this.longitude = lon;
        this.length = length;
    }

    public int getLength() {
        return length;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }


    public BigDecimal getLongitude() {
        return longitude;
    }
}
