package dk.kb.provide_dod_info.testutils;

import dk.kb.provide_dod_info.config.Configuration;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * To test against a proper system, you need the two files: pubhub-license.txt in the root of the project folder.
 *
 * Test pubhub-license.txt must have a single line with the given license.
 */
public class TestConfigurations {

    public static Configuration getConfigurationForTest(){
        try {
            File baseDir = TestFileUtils.createEmptyDirectory(TestFileUtils.getTempDir().getAbsolutePath());
            File outDir = TestFileUtils.createEmptyDirectory(baseDir.getAbsolutePath() + "/out");

            Map<String, Object> confMap = new HashMap<>();
            confMap.put(Configuration.CONF_EBOOK_FORMATS, Arrays.asList("pdf"));
            confMap.put(Configuration.CONF_OUT_DIR, outDir.getAbsolutePath());

            confMap.put(Configuration.CONF_ALMA_SRU_SEARCH, "https://kbdk-kgl.alma.exlibrisgroup.com/view/sru/45KBDK_KGL?version=1.2&operation=searchRetrieve&");


            return new Configuration(confMap);
        } catch (Exception e) {
            throw new RuntimeException("", e);
        }
    }


    public static Configuration getTestConfiguration(){
        try {
            File baseDir = TestFileUtils.createEmptyDirectory(TestFileUtils.getTempDir().getAbsolutePath());
            File corpusOrigDir = TestFileUtils.createEmptyDirectory(baseDir.getAbsolutePath() + "/testfiler");
            File outDir = TestFileUtils.createEmptyDirectory(baseDir.getAbsolutePath() + "/out");

            Map<String, Object> confMap = new HashMap<>();
            confMap.put(Configuration.CONF_CORPUS_FILE_DIR, corpusOrigDir.getAbsolutePath());
            confMap.put(Configuration.CONF_OUT_DIR, outDir.getAbsolutePath());
            confMap.put(Configuration.CONF_CUT_YEAR, 1881);
            confMap.put(Configuration.CONF_OUT_FILE_NAME, "/AlmaExtractResult.xlsx");
            confMap.put(Configuration.CONF_ALMA_SRU_SEARCH, "https://kbdk-kgl.alma.exlibrisgroup.com/view/sru/45KBDK_KGL?version=1.2&operation=searchRetrieve&");
            confMap.put(Configuration.IS_TEST, true);

            return new Configuration(confMap);
        } catch (Exception e) {
            throw new RuntimeException("", e);
        }
    }
}
