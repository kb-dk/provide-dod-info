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

/**
 * Configuration for Gjoell.
 *
 * It should have the following YAML format:
 * <ul>
 *   <li>provide-dod-info:</li>
 *   <ul>
 *     <li>ebook_orig_dir: /path/to/orig/book/dir/</li>
 *     <li>license_key: DO_NOT_PUT_LICENSE_IN_GITHUB_FILE</li>
 *     <li>characterization_script: bin/run_fits.sh (optional)</li>
 *     <li>statistics_dir: /path/to/statistics/dir/</li>
 *     <li>ebook_formats:</li>
 *     <ul>
 *       <li>- pdf</li>
 *     </ul>
 *     <li>alma_sru_search: $ALMA_SRU_SEARCH</li>
 *     <li>transfer: (THIS ELEMENT IS NOT REQUIRED)</li>
 *     <ul>
 *       <li>ingest_ebook_path: /transfer/path/root/ingest/ebook/</li>
 *       <li>update_ebook_content_path: /transfer/path/root/content/ebook/</li>
 *       <li>update_ebook_metadata_path: /transfer/path/root/metadata/ebook/</li>
 *       <li>ingest_audio_path: /transfer/path/root/ingest/audio/</li>
 *       <li>update_audio_content_path: /transfer/path/root/content/audio/</li>
 *       <li>update_audio_metadata_path: /transfer/path/root/metadata/audio/</li>
 *       <li>retain_create_date: -1 // TIME IN MILLIS</li>
 *       <li>retain_modify_date: -1 // TIME IN MILLIS</li>
 *       <li>retain_pub_date: -1 </li>
 *       <li>required_formats:</li>
 *       <ul>
 *         <li>- fits.xml</li>
 *         <li>- mods.xml</li>
 *         <li>- pubhub.xml</li>
 *       </ul>
 *     </ul>
 *   </ul>
 * </ul>
 */
public class Configuration {
    /** The logger.*/
    private static final Logger log = LoggerFactory.getLogger(Configuration.class);

    /** The configuration name for the output directory.*/
    public static final String CONF_EBOOK_OUTPUT_DIR = "ebook_output_dir";

    /** The configuration root element for provide-dod-info.*/
    public static final String CONF_PROVIDE_DOD_INFO = "provide-dod-info";
    /** The configuration name for the input directory.*/
    public static final String CONF_CORPUS_FILE_DIR = "corpus_orig_dir";
    /** The configuration name for the list of formats for the ebooks.*/
    public static final String CONF_EBOOK_FORMATS = "ebook_formats";
    /** The configuration name for the output directory. */
    public static final String CONF_OUT_DIR = "out_dir";
    /** The configuration name for Alma records with publish date older than this will be handled */
    public static final String CONF_CUT_YEAR = "cut_year";
    /** The configuration Alma sru search base url.*/
    public static final String CONF_ALMA_SRU_SEARCH = "alma_sru_search";
    /** The configuration name for the output file */
    public static final String CONF_OUT_FILE_NAME = "out_file_name";

    /** The directory containing the pdf files for which to extract Alma data */
    protected final File corpusOrigDir;
    /** The directory for the output files.*/
    protected final File outDir;

    protected final File tmpDir;
    /** Only Alma records with publish date older than this will be handled */
    protected final Integer cutYear;
    /** The configuration for the alma sru search.*/
    protected final String almaSruSearchConfiguration;
    /** The name of the Excel file containing the data extracted from Alma */
    protected final String outFileName;

    /** The output directory for the ebooks.*/
//    protected final File ebookOutputDir;



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

//        ebookOutputDir = FileUtils.createDirectory((String) confMap.get(CONF_EBOOK_OUTPUT_DIR));
//        ArgumentCheck.checkThatMapContainsKey(confMap, CONF_EBOOK_OUTPUT_DIR, "confMap");

        this.outDir = FileUtils.createDirectory((String) confMap.get(CONF_OUT_DIR));
        this.corpusOrigDir = FileUtils.createDirectory ((String) confMap.get(CONF_CORPUS_FILE_DIR));
        this.tmpDir = FileUtils.createDirectory("tempDir");
        this.almaSruSearchConfiguration = (String) confMap.get(CONF_ALMA_SRU_SEARCH);
        this.cutYear = (Integer) confMap.get(CONF_CUT_YEAR);
        this.outFileName = (String) confMap.get(CONF_OUT_FILE_NAME);

    }

    /** @return The alma sru search base.*/
    public String getAlmaSruSearch() {
        return almaSruSearchConfiguration;
    }

    public File getCorpusOrigDir() {
        return corpusOrigDir;
    }
    /** @return The directory for the output statistics.*/
    public File getOutDir() {
        return outDir;
    }

    public Integer getCutYear() {
        return cutYear;
    }

    public String getOutFileName() {
        return outFileName;
    }

    public File getTempDir(){
        return tmpDir;
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
}
