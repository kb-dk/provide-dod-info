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

    /** The configuration root element for provide-dod-info.*/
    public static final String CONF_ALMA_META_EXTRACT = "provide-dod-info";
    /** The configuration name for the output directory.*/
    public static final String CONF_EBOOK_OUTPUT_DIR = "ebook_output_dir";
    /** The configuration name for the output directory.*/
//    public static final String CONF_AUDIO_OUTPUT_DIR = "audio_output_dir";
    /** The configuration name for the ebook file directory.*/
    public static final String CONF_CORPUS_FILE_DIR = "corpus_orig_dir"; //CONF_EBOOK_FILE_DIR
    /** The configuration name for the audio book file directory.*/
//    public static final String CONF_AUDIO_FILE_DIR = "audio_orig_dir";
    /** The configuration name for the list of formats for the ebooks.*/
    public static final String CONF_EBOOK_FORMATS = "ebook_formats";
    /** The configuration name for the list of formats for the audio books.*/
//    public static final String CONF_AUDIO_FORMATS = "audio_formats";
    /** The directory where the output statistics will be placed.*/
    public static final String CONF_OUT_DIR = "out_dir";

    /** The configuration Alma sru search base url.*/
    public static final String CONF_ALMA_SRU_SEARCH = "alma_sru_search";

    /** The output directory for the ebooks.*/
    protected final File ebookOutputDir;

    protected final File corpusOrigDir;
    /** The output directory for the audio-books.*/
//    protected final File abookOutputDir;
    /** The directory for the output  files.*/
    protected final File outDir;


    /** The configuration for the alma sru search.*/
    protected final String almaSruSearchConfiguration;

    /**
     * Constructor.
     * @param confMap The YAML map for the configuration.
     * @throws IOException If the output directory does not exist and cannot be created.
     */
    public Configuration(Map<String, Object> confMap) throws IOException {
        ArgumentCheck.checkNotNullOrEmpty(confMap, "Map<String, Object> confMap");

        ArgumentCheck.checkThatMapContainsKey(confMap, CONF_EBOOK_OUTPUT_DIR, "confMap");
//        ArgumentCheck.checkThatMapContainsKey(confMap, CONF_AUDIO_OUTPUT_DIR, "confMap");
        ArgumentCheck.checkThatMapContainsKey(confMap, CONF_OUT_DIR, "confMap");
        ArgumentCheck.checkThatMapContainsKey(confMap, CONF_ALMA_SRU_SEARCH, "confMap");

        ebookOutputDir = FileUtils.createDirectory((String) confMap.get(CONF_EBOOK_OUTPUT_DIR));
//        abookOutputDir = FileUtils.createDirectory((String) confMap.get(CONF_AUDIO_OUTPUT_DIR));
        ArgumentCheck.checkThatMapContainsKey(confMap, CONF_CORPUS_FILE_DIR, "confMap");
        this.outDir = FileUtils.createDirectory((String) confMap.get(CONF_OUT_DIR));

        this.corpusOrigDir = FileUtils.createDirectory ((String) confMap.get(CONF_CORPUS_FILE_DIR));

        this.almaSruSearchConfiguration = (String) confMap.get(CONF_ALMA_SRU_SEARCH);

    }

    /** @return The alma sru search base.*/
    public String getAlmaSruSearch() {
        return almaSruSearchConfiguration;
    }

    /** @return The output directory for the ebook directories. */
//    public File getEbookOutputDir() {
//        return ebookOutputDir;
//    }

    /** @return The output directory for the audio book directories. */
//    public File getAudioOutputDir() {
//        return abookOutputDir;
//    }

    public File getCorpusOrigDir() {
        return corpusOrigDir;
    }
    /** @return The directory for the output statistics.*/
    public File getOutDir() {
        return outDir;
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
        Map<String, Object> confMap = (Map<String, Object>) map.get(CONF_ALMA_META_EXTRACT);
        return new Configuration(confMap);
    }
}
