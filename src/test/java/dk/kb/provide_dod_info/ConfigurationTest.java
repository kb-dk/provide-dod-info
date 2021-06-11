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
        FileUtils.createDirectory("tempDir/transfer/ebook/ingest");
        FileUtils.createDirectory("tempDir/transfer/ebook/content");
        FileUtils.createDirectory("tempDir/transfer/ebook/metadata");
        FileUtils.createDirectory("tempDir/transfer/audio/ingest");
        FileUtils.createDirectory("tempDir/transfer/audio/content");
        FileUtils.createDirectory("tempDir/transfer/audio/metadata");

        Configuration conf = Configuration.createFromYAMLFile(new File("src/test/resources/provide-dod-info.yml"));
        Assert.assertNotNull(conf);
//        Assert.assertNotNull(conf.getAudioOutputDir());
//        Assert.assertNotNull(conf.getEbookOutputDir());
        Assert.assertNotNull(conf.getCorpusOrigDir());
        Assert.assertNotNull(conf.getOutDir());

//        Assert.assertTrue(conf.getAudioOutputDir().isDirectory());
//        Assert.assertTrue(conf.getEbookOutputDir().isDirectory());
        Assert.assertTrue(conf.getOutDir().isDirectory());

        Assert.assertTrue(conf.getCorpusOrigDir().isDirectory());
        Assert.assertNotNull(conf.getAlmaSruSearch());


    }

    @Test
    public void testConfigurationWithoutTransfer() {
        Configuration conf = TestConfigurations.getConfigurationForTestWithoutTransfer();
//        Assert.assertNull(conf.getTransferConfiguration());
    }
}
