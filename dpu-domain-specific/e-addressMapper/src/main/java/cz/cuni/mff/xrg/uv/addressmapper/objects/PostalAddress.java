package cz.cuni.mff.xrg.uv.addressmapper.objects;

import org.openrdf.model.URI;

import eu.unifiedviews.helpers.dpu.ontology.EntityDescription;

/**
 * Represent a http://schema.org/PostalAddress entity enriched for reports.
 *
 * @author Škoda Petr
 */
@EntityDescription.Entity(type = "http://schema.org/PostalAddress")
public class PostalAddress {

    private final URI entity;

    @EntityDescription.Property(uri = "http://schema.org/addressLocality")
    private String addressLocality;

    @EntityDescription.Property(uri = "http://schema.org/addressRegion")
    private String addressRegion;

    @EntityDescription.Property(uri = "http://schema.org/postalCode")
    private String postalCode;

    @EntityDescription.Property(uri = "http://schema.org/streetAddress")
    private String streetAddress;

    public PostalAddress(URI entity) {
        this.entity = entity;
    }

    public URI getEntity() {
        return entity;
    }

    public String getAddressLocality() {
        return addressLocality;
    }

    public void setAddressLocality(String addressLocality) {
        this.addressLocality = addressLocality;
    }

    public String getAddressRegion() {
        return addressRegion;
    }

    public void setAddressRegion(String addressRegion) {
        this.addressRegion = addressRegion;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

}
