package dk.kb.provide_dod_info;

import dk.kb.provide_dod_info.config.Configuration;
import dk.kb.provide_dod_info.metadata.AlmaMetadataRetriever;
import dk.kb.provide_dod_info.utils.ExcelUtils;
import dk.kb.provide_dod_info.utils.FileUtils;
import dk.kb.provide_dod_info.utils.ZipUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.zip.ZipOutputStream;


/**
 * Extracts MARC metadata from alma.
 * Add specific extracted metadata to Excel file
 * Sort output data in directories
 * Zip data to out.zip
 *
 * Usage:
 * dk.kb.provide-dod-info.AlmaExtract /PATH/TO/provide-dod-info.yml
 *
 */
public class AlmaExtract {
    /** The logger.*/
    private static final Logger log = LoggerFactory.getLogger(AlmaExtract.class);

    /** The output directory.*/
    protected static File outputDir = new File(".");

    /**
     * Requires one argument: the configuration file.
     * @param args The arguments.
     */
    public static void main(String[] args) {
        if(args.length < 1) {
            System.err.println("Needs the configuration file 'provide-dod-info.yml' as argument.");
            System.exit(-1);
        }

        String confPath = args[0];
        File confFile = new File(confPath);
        XSSFWorkbook workbook = new XSSFWorkbook();

        try {
            Configuration conf = Configuration.createFromYAMLFile(confFile);
            HttpClient httpClient = new HttpClient();

            AlmaMetadataRetriever almaMetadataRetriever = new AlmaMetadataRetriever(conf, httpClient);
            AlmaRetriever almaRetriever = new AlmaRetriever(conf, almaMetadataRetriever);
            almaRetriever.retrieveAlmaMetadataForFiles(workbook);

            DataHandler dataHandler = new DataHandler(conf);
            String excelFile = conf.getTempDir().getName() + "/"  + conf.getOutFileName();
            File existingFile = FileUtils.getExistingFile(excelFile);
            String absolutePath = existingFile.getAbsolutePath();
            Map<String, String> values;
            if (StringUtils.isNotEmpty(absolutePath)) {
                values = ExcelUtils.getValues(absolutePath);
                dataHandler.sortDirectories(values);
            }

            String sourceFile = conf.getTempDir().getAbsolutePath();
            FileOutputStream fos = new FileOutputStream(conf.getOutDir().getAbsolutePath() + "/out.zip");
            ZipOutputStream zipOut = new ZipOutputStream(fos);
            File fileToZip = new File(sourceFile);

            ZipUtils.zipFile(fileToZip, fileToZip.getName(), zipOut);
            zipOut.close();
            fos.close();
            FileUtils.deleteDirectory( conf.getTempDir());
            log.debug("Output ready");

        } catch (Exception e ) {
            throw new IllegalStateException("Something went wrong. Check log for errors", e);
        }
    }
}
