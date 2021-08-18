package dk.kb.provide_dod_info;

import dk.kb.provide_dod_info.config.Configuration;
import dk.kb.provide_dod_info.metadata.AlmaMetadataRetriever;
import dk.kb.provide_dod_info.testutils.TestConfigurations;
import dk.kb.provide_dod_info.testutils.TestFileUtils;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class AlmaMetadataRetrieverTest extends ExtendedTestCase {

    String VALID_ID = "11010200054A";
    String INVALID_ID = UUID.randomUUID().toString();
    Configuration configuration;

    @BeforeMethod
    public void setup()  {
        TestFileUtils.setup();
        configuration = TestConfigurations.getTestConfiguration();
    }

    @AfterClass
    public void tearDown() {
        TestFileUtils.tearDown();
    }

    @Test
    public void testCompleteRetrieval() throws IOException {
        addDescription("Test a complete retrieval of metadata from Alma based on a barcode.");
        HttpClient httpClient = new HttpClient();

        AlmaMetadataRetriever retriever = new AlmaMetadataRetriever(configuration, httpClient);

        File output = new File(TestFileUtils.getTempDir(), UUID.randomUUID().toString());
        retriever.retrieveMetadataForBarcode(VALID_ID, new FileOutputStream(output));

        String extractedMetadata = TestFileUtils.readFile(output);

        System.err.println(extractedMetadata);

        Assert.assertTrue(extractedMetadata.contains("<controlfield tag=\"001\">99122058486905763</controlfield>"));
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testBadRetrieval() throws IOException {
        addDescription("Test a complete retrieval of metadata from Alma based on a ISBN.");
        HttpClient httpClient = new HttpClient();

        AlmaMetadataRetriever retriever = new AlmaMetadataRetriever(configuration, httpClient);

        File output = new File(TestFileUtils.getTempDir(), UUID.randomUUID().toString());
        retriever.retrieveMetadataForBarcode(INVALID_ID, new FileOutputStream(output));
    }
}
