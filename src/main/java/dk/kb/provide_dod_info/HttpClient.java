package dk.kb.provide_dod_info;

import dk.kb.provide_dod_info.exception.ArgumentCheck;
import dk.kb.provide_dod_info.utils.StreamUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Http client for downloading stuff (mostly the cover image files).
 */
public class HttpClient {
    /** The logger.*/
    private static final Logger log = LoggerFactory.getLogger(HttpClient.class);

    /**
     * Constructor.
     * TODO: does currently nothing!!!
     */
    public HttpClient() {}

    /**
     * Method for extracting the content of a given URL.
     * It will throw an exception, if the response status code is not in the 200-299 range.
     * @param url The text URL to retrieve.
     * @param out The output stream, where the content from the URL is delivered.
     * @throws IOException If any connection issues occur.
     */
    public void retrieveUrlContent(String url, OutputStream out) throws IOException {
        ArgumentCheck.checkNotNullOrEmpty(url, "String url");
        ArgumentCheck.checkNotNull(out, "OutputStream out");

        log.debug("Retrieving content from URL: " + url);
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet getMethod = new HttpGet(url);

            CloseableHttpResponse response = client.execute(getMethod);
            int statusCode = response.getStatusLine().getStatusCode();
            if(!validateResponseCode(statusCode)) {
                throw new IllegalStateException("Received erroneous status code for url " + url + ", " + statusCode);
            }

            StreamUtils.copyInputStreamToOutputStream(response.getEntity().getContent(), out);
        }
    }

    /**
     * Validate the response code of an HTTP request.
     * @param statusCode The response code.
     * @return Whether or not the status code is valid.
     */
    protected boolean validateResponseCode(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }
}
