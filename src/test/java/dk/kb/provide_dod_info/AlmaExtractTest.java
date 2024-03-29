package dk.kb.provide_dod_info;

import dk.kb.provide_dod_info.config.Configuration;
import dk.kb.provide_dod_info.metadata.AlmaMetadataRetriever;
import dk.kb.provide_dod_info.testutils.PreventSystemExit;
import dk.kb.provide_dod_info.testutils.TestConfigurations;
import dk.kb.provide_dod_info.testutils.TestFileUtils;
import org.jaccept.structure.ExtendedTestCase;
import org.mockito.stubbing.Answer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class AlmaExtractTest extends ExtendedTestCase {

    String ID = "9788711436981";
    File testConfFile = new File("src/test/resources/provide-dod-info.yml");

    Configuration conf;

    @BeforeMethod
    public void setup() {
        TestFileUtils.setup();
        conf = TestConfigurations.getTestConfiguration();
        AlmaExtract.outputDir = TestFileUtils.getTempDir();
    }

    @AfterClass
    public void tearDown() {
        TestFileUtils.tearDown();
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testNotEnoughArguments() {
        addDescription("Test the case, when not enough argument are given.");
        try {
            PreventSystemExit.forbidSystemExitCall();
            AlmaExtract.main(new String[]{""});
        } finally {
            PreventSystemExit.enableSystemExitCall();
        }
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testFailToConnect() {
        addDescription("Test the case, when we cannot connect to the Alma server.");
        try {
            PreventSystemExit.forbidSystemExitCall();
            AlmaExtract.main(new String[]{testConfFile.getAbsolutePath(), ID});
        } finally {
            PreventSystemExit.enableSystemExitCall();
        }
    }

    @Test
    public void testRetrieveMetadataForBarcode() throws IOException {
        addDescription("Test the retrieveMetadataForBarcode method.");

        AlmaMetadataRetriever retriever = mock(AlmaMetadataRetriever.class);
        String barcode = UUID.randomUUID().toString();

        doAnswer((Answer<Void>) invocation -> {
            OutputStream out = (OutputStream) invocation.getArguments()[1];
            out.write("THIS IS A TEST".getBytes(StandardCharsets.UTF_8));
            out.flush();
            return null;
        }).when(retriever).retrieveMetadataForBarcode(eq(barcode), any(OutputStream.class));

        AlmaExtract.retrieveMetadataForBarcode(retriever, barcode);

        verify(retriever).retrieveMetadataForBarcode(eq(barcode), any(OutputStream.class));

        verifyNoMoreInteractions(retriever);
    }
}
