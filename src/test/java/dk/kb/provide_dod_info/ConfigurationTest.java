package dk.kb.provide_dod_info;

import dk.kb.provide_dod_info.testutils.TestConfigurations;
import dk.kb.provide_dod_info.testutils.TestFileUtils;
import dk.kb.provide_dod_info.utils.FileUtils;
import dk.kb.provide_dod_info.config.Configuration;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

public class ConfigurationTest extends ExtendedTestCase {

    @BeforeClass
    public void setup() {
        TestFileUtils.setup();
    }

    @AfterClass
    public void tearDown() {
        TestFileUtils.tearDown();
    }

    @Test
    public void testLoadingFromFile() throws IOException {
        FileUtils.createDirectory("tempDir/testfiler");
        FileUtils.createDirectory("tempDir/out");

        Configuration conf = Configuration.createFromYAMLFile(new File("src/test/resources/provide-dod-info.yml"));
        Assert.assertNotNull(conf);

        Assert.assertNotNull(conf.getCorpusOrigDir());
        Assert.assertTrue(conf.getCorpusOrigDir().isDirectory());

        Assert.assertNotNull(conf.getOutDir());
        Assert.assertTrue(conf.getOutDir().isDirectory());


        Assert.assertNotNull(conf.getAlmaSruSearch());
        Assert.assertNotNull(conf.getCutYear());
        Assert.assertNotNull(conf.getOutFileName());


    }

    @Test
    public void testConfigurationWithoutTransfer() {
        Configuration conf = TestConfigurations.getTestConfiguration();
//        Assert.assertNull(conf.getTransferConfiguration());
    }
}
