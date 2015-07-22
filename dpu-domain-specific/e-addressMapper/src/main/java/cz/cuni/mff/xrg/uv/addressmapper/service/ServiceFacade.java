package cz.cuni.mff.xrg.uv.addressmapper.service;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.context.UserContext;

/**
 *
 * @author Škoda Petr
 */
public class ServiceFacade {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceFacade.class);

    private final HttpClient client = new HttpClient();

    private final UserContext ctx;

    public ServiceFacade(UserContext ctx) {
        this.ctx = ctx;
        //
    }

    /**
     *
     * @param address
     * @param serviceUri
     * @return Never return null.
     * @throws DPUException
     */
    public List<Response> resolve(PostalAddress address, String serviceUri) throws DPUException {
        LOG.info("Resolving: {}", address.getEntity());
        // Prepare query.
        StringBuilder body = new StringBuilder();
        body.append("{\"region\":\"");
        body.append(address.getAddressRegion());
        body.append("\",");
        body.append("\"postalCode\":\"");
        body.append(address.getPostalCode());
        body.append("\",");
        body.append("\"street\":\"");
        body.append(address.getStreetAddress());
        body.append("\",");
        body.append("\"locality\":\"");
        body.append(address.getAddressLocality());
        body.append("\"}");
        LOG.debug("Query object:\n{}", body.toString());
        //
        final List<Response> results = new LinkedList<>();
        final PostMethod method = new PostMethod(serviceUri);
        method.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
        method.setRequestBody(body.toString());

        try {
            int returnCode = client.executeMethod(method);
            if (returnCode != HttpStatus.SC_OK) {
                throw ContextUtils.dpuException(ctx, "Response code: {0}", returnCode);
            }
            // Read and parse response.
            final String fullResponse = IOUtils.toString(method.getResponseBodyAsStream());

            if (!fullResponse.contains("queryNumber")) {
                if (fullResponse.equals("[]")) {
                    // Empty result.
                    return Collections.EMPTY_LIST;
                }
                LOG.error("Error response: {}", fullResponse);
                throw ContextUtils.dpuException(ctx, "Error see logs for more details.");
            }

            final String[] entities = fullResponse.split("queryNumber");

            if (entities.length < 2) {
                return Collections.EMPTY_LIST;
            }
            // For each response entity.
            for (int i = 1; i < entities.length; i++) {
                final String entity = entities[i];
                final String url = getValue(entity, "\"url\":");
                final String confidenceAsStr = getValue(entity, "\"confidence\":");
                final String completenesssAsStr = getValue(entity, "\"completeness\":");
                try {
                    results.add(new Response(url,
                            Double.parseDouble(confidenceAsStr),
                            Double.parseDouble(completenesssAsStr)));
                } catch (NumberFormatException ex) {
                    // Invalid answer.
                    LOG.error("Invalid entity: {}", entity);
                }
            }
        } catch (IOException ex) {
            throw ContextUtils.dpuException(ctx, "", ex);

        }
        // Log in special form in no output is given.
        if (results.isEmpty()) {
            LOG.info("No result given for:\n{}", body.toString());
        }
        return results;
    }

    /**
     * Return first value for given key.
     * 
     * @param fullString
     * @param attributeName
     * @return
     * @throws DPUException
     */
    private String getValue(String fullString, String attributeName) throws DPUException {
        int position = fullString.indexOf(attributeName);
        if (position == -1) {
            LOG.error("Missing property: {}\n{}", attributeName, fullString);
            throw ContextUtils.dpuException(ctx, "Missing property.");
        }
        position += attributeName.length();

        int end;
        if (fullString.charAt(position) == '"') {                        
            // It's string.
            ++position;
            end = fullString.indexOf("\"", position);
        } else {
            // It's double.
            end = fullString.indexOf(",", position);
        }
        return fullString.substring(position, end);
    }

}
