package eu.unifiedviews.helpers.dataunit.resourcehelper;

import java.util.Date;

/**
 * Represents a single resource within a Dataset
 */
public class Resource {
    private Date created;

    private String description;

    private Extras extras = new Extras();

    private String format;

    private String hash;

    private Date last_modified;

    private String mimetype;

    private String mimetype_inner;

    private String name;

    private String resource_type;

    private Long size;

    private String url;

    public Resource() {
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Extras getExtras() {
        return extras;
    }

    public void setExtras(Extras extras) {
        this.extras = extras;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Date getLast_modified() {
        return last_modified;
    }

    public void setLast_modified(Date last_modified) {
        this.last_modified = last_modified;
    }

    public String getMimetype() {
        return mimetype;
    }

    public void setMimetype(String mimetype) {
        this.mimetype = mimetype;
    }

    public String getMimetype_inner() {
        return mimetype_inner;
    }

    public void setMimetype_inner(String mimetype_inner) {
        this.mimetype_inner = mimetype_inner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getResource_type() {
        return resource_type;
    }

    public void setResource_type(String resource_type) {
        this.resource_type = resource_type;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
