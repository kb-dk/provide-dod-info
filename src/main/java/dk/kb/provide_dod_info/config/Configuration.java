package dk.kb.provide_dod_info.config;

import dk.kb.provide_dod_info.exception.ArgumentCheck;
import dk.kb.provide_dod_info.utils.YamlUtils;
import dk.kb.provide_dod_info.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class Configuration {
    /** The logger.*/
    private static final Logger log = LoggerFactory.getLogger(Configuration.class);
    /** The configuration root element for provide-dod-info.*/
    public static final String CONF_PROVIDE_DOD_INFO = "provide-dod-info";
    /** The configuration name for the input directory.*/
    public static final String CONF_CORPUS_FILE_DIR = "corpus_orig_dir";
    /** The configuration name for the output directory. */
    public static final String CONF_OUT_DIR = "out_dir";
    /** The configuration name for Alma records with publish date older than this will be handled */
    public static final String CONF_CUT_YEAR = "cut_year";
    /** The configuration Alma sru search base url.*/
    public static final String CONF_ALMA_SRU_SEARCH = "alma_sru_search";
    /** The configuration name for the output file */
    public static final String CONF_OUT_FILE_NAME = "out_file_name";

    public static final String CONF_IS_TEST = "is_test";

    /** The directory containing the pdf files for which to extract Alma data */
    protected final File corpusOrigDir;
    /** The directory for the output files.*/
    protected final File outDir;

    /** Temporary directory where output files are placed before they are zipped. Deleted after zipping */
    protected final File tmpDir;
    /** Only Alma records with publish date older than this will be handled */
    protected final Integer cutYear;
    /** The configuration for the alma sru search.*/
    protected final String almaSruSearchConfiguration;
    /** The name of the Excel file containing the data extracted from Alma */
    protected final String outFileName;
    /** Check value if it is a test run */
    protected static Boolean isTest;


    /**
     * Constructor.
     * @param confMap The YAML map for the configuration.
     * @throws IOException If the output directory does not exist and cannot be created.
     */
    public Configuration(Map<String, Object> confMap) throws IOException {
        ArgumentCheck.checkNotNullOrEmpty(confMap, "Map<String, Object> confMap");
        ArgumentCheck.checkThatMapContainsKey(confMap, CONF_OUT_DIR, "confMap");
        ArgumentCheck.checkThatMapContainsKey(confMap, CONF_ALMA_SRU_SEARCH, "confMap");
        ArgumentCheck.checkThatMapContainsKey(confMap, CONF_CORPUS_FILE_DIR, "confMap");
        ArgumentCheck.checkThatMapContainsKey(confMap, CONF_CUT_YEAR, "confMap");
        ArgumentCheck.checkThatMapContainsKey(confMap, CONF_OUT_FILE_NAME, "confMap");

        this.outDir = FileUtils.createDirectory((String) confMap.get(CONF_OUT_DIR));
        this.corpusOrigDir = FileUtils.createDirectory((String) confMap.get(CONF_CORPUS_FILE_DIR));
        this.tmpDir = FileUtils.createDirectory("outDir");
        this.almaSruSearchConfiguration = (String) confMap.get(CONF_ALMA_SRU_SEARCH);
        this.cutYear = (Integer) confMap.get(CONF_CUT_YEAR);
        this.outFileName = (String) confMap.get(CONF_OUT_FILE_NAME);
        if(confMap.containsKey(CONF_IS_TEST)) {
            isTest = extractBoolean(confMap.get(CONF_IS_TEST));
        } else isTest = false;
    }

    /** @return The alma sru search base.*/
    public String getAlmaSruSearch() {
        return almaSruSearchConfiguration;
    }
    /** @return The dir containing the DOD pdf-files.*/
    public File getCorpusOrigDir() {
        return corpusOrigDir;
    }
    /** @return The directory for the output statistics.*/
    public File getOutDir() {
        return outDir;
    }
    /** @return The year limit where only older files are included*/
    public Integer getCutYear() {
        return cutYear;
    }
    /** @return Name of the Excel file with metadata*/
    public String getOutFileName() {
        return outFileName;
    }
    /** @return temp dir where files are placed during run. Removed at exit*/
    public File getTempDir() {
        return tmpDir;
    }
    /** @return true if it is a test run*/
    public Boolean getIsTest() {
        return isTest;
    }
    /**
     * Creates a configuration from a file.
     * @param yamlFile The YAML file with the configuration.
     * @return The configuration.
     * @throws IOException If it  to load, or the configured elements cannot be instantiated.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Configuration createFromYAMLFile(File yamlFile) throws IOException {
        ArgumentCheck.checkExistsNormalFile(yamlFile, "File yamlFile");

        log.debug("Loading configuration from file '" + yamlFile.getAbsolutePath() + "'");
        LinkedHashMap<String, LinkedHashMap> map = YamlUtils.loadYamlSettings(yamlFile);
        Map<String, Object> confMap = (Map<String, Object>) map.get(CONF_PROVIDE_DOD_INFO);
        return new Configuration(confMap);
    }

    /**
     * Retrieves the boolean value from an unidentified type of object.
     * @param b The unidentified type of object.
     * @return The boolean value.
     */
    public static Boolean extractBoolean(Object b) {
        if(b instanceof String) {
            return Boolean.parseBoolean((String) b);
        }
        return (Boolean) b;
    }
}
