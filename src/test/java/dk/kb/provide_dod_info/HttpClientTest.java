package dk.kb.provide_dod_info;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

public class HttpClientTest extends ExtendedTestCase {

    @Test
    public void testRetrieveUrlContent() throws IOException {
        addDescription("Test the retrieveUrlContent method, when it is successfull");
        String url = "https://raw.githubusercontent.com/Det-Kongelige-Bibliotek/elivagar/master/README.md";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        HttpClient httpClient = new HttpClient();
        httpClient.retrieveUrlContent(url, baos);

        Assert.assertNotNull(baos);
        String page = baos.toString(String.valueOf(Locale.ROOT));
        Assert.assertNotNull(page);
        Assert.assertFalse(page.isEmpty());
    }

    @Test(expectedExceptions = IOException.class)
    public void testRetrieveUrlContentFailure() throws IOException {
        addDescription("Test the retrieveUrlContent method, when it ");
        String url = "http://localhost:1234/" + UUID.randomUUID().toString();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        HttpClient httpClient = new HttpClient();
        httpClient.retrieveUrlContent(url, baos);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testRetrieveUrlContentBadResponse() throws IOException {
        addDescription("Test the retrieveUrlContent method, when it receives a bad response");
        String url = "https://raw.githubusercontent.com/Det-Kongelige-Bibliotek/elivagar/master/README2.md";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        HttpClient httpClient = new HttpClient();
        httpClient.retrieveUrlContent(url, baos);
    }

    @Test
    public void testValidateResponseCode() {
        addDescription("Test the validateResponseCode method");

        HttpClient httpClient = new HttpClient();
        Assert.assertFalse(httpClient.validateResponseCode(101)); // Switching protocol
        Assert.assertTrue(httpClient.validateResponseCode(200)); // OK
        Assert.assertFalse(httpClient.validateResponseCode(300)); // Multiple choice
        Assert.assertFalse(httpClient.validateResponseCode(418)); // I'm a teapot
        Assert.assertFalse(httpClient.validateResponseCode(505)); // HTTP Version Not Supported
    }
}
